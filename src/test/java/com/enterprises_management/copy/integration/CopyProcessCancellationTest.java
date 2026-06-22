package com.enterprises_management.copy.integration;

import com.enterprises_management.copy.application.input.ICopyProcessCancelPort;
import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.exceptions.CopyProcessCancelDeniedException;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integración E2E para cancelación de procesos (REQ-PROC-02, REQ-PROC-03, REQ-EVT-01).
 *
 * <p>Verifica:
 * <ul>
 *   <li>Cancelar proceso PENDIENTE → estado CANCELADO</li>
 *   <li>Evento Proceso.cancelado registrado</li>
 *   <li>Cancelar proceso COMPLETADO → CopyProcessCancelDeniedException (409)</li>
 * </ul>
 *
 * <p>ADR-7: la cancelación es una transición válida desde PENDIENTE y EN_PROCESO,
 * pero no desde estados terminales (COMPLETADO, ERROR, CANCELADO).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "app.copy.orchestrator.enabled=true",
    "app.copy.orchestrator.bootstrap-config=true",
    "app.copy.orchestrator.stub.failure-rate=0",
    "app.copy.orchestrator.stub.latency-ms=0",
    "app.copy.orchestrator.default-retries=3",
    // Hito 2 — transporte stub para que StubParticipantClient sea el bean activo (ADR-19)
    "app.copy.orchestrator.participant.transport=stub",
    "app.copy.orchestrator.events.amqp.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:e2e_cancel;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.data-locations=",
    "jwt.auth.converter.principle-attribute=preferred_username",
    "jwt.auth.converter.resource-id=microservices_client",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
@DisplayName("E2E — Cancelación de procesos (ADR-7, REQ-PROC-02)")
class CopyProcessCancellationTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ICopyProcessStartPort startPort;

    @Autowired
    private ICopyProcessCancelPort cancelPort;

    @Autowired
    private ICopyProcessQueryPort queryPort;

    @Autowired
    private com.enterprises_management.copy.application.services.SagaEngineService sagaEngineService;

    // -------------------------------------------------------------------------
    // REQ-PROC-02 — Cancelación de proceso PENDIENTE
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Cancelar proceso PENDIENTE → estado CANCELADO (REQ-PROC-02)")
    void cancelar_procesoPendiente_estadoCancelado() {
        // GIVEN
        CopyProcess proceso = crearProceso("usuario-cancel-001");
        assertThat(proceso.getEstado()).isEqualTo(ProcessState.PENDIENTE);

        // WHEN
        cancelPort.cancelar(proceso.getId(), "admin-cancel");

        // THEN
        CopyProcess procesoFinal = queryPort.consultarProceso(proceso.getId());
        assertThat(procesoFinal.getEstado())
                .withFailMessage("El proceso debe estar CANCELADO tras cancelar explícitamente")
                .isEqualTo(ProcessState.CANCELADO);
        assertThat(procesoFinal.getFinalizadoEn()).isNotNull();
    }

    @Test
    @DisplayName("Cancelar proceso PENDIENTE — evento Proceso.cancelado registrado (REQ-EVT-01)")
    void cancelar_procesoPendiente_eventoRegistrado() {
        // GIVEN
        CopyProcess proceso = crearProceso("usuario-cancel-002");

        // WHEN
        cancelPort.cancelar(proceso.getId(), "admin-cancel");

        // THEN — evento PROCESO_CANCELADO debe existir
        List<CopyProcessEvent> eventos = queryPort.consultarEventos(proceso.getId());
        List<CopyEventType> tipos = eventos.stream().map(CopyProcessEvent::getTipoEvento).toList();

        assertThat(tipos)
                .withFailMessage("Se esperaba el evento PROCESO_CANCELADO. Eventos: %s", tipos)
                .contains(CopyEventType.PROCESO_CANCELADO);
    }

    @Test
    @DisplayName("Cancelar proceso COMPLETADO → CopyProcessCancelDeniedException (ADR-7, REQ-PROC-02)")
    void cancelar_procesoCompletado_lanzaExcepcion() {
        // GIVEN — proceso que completa las 4 fases
        CopyProcess proceso = crearProceso("usuario-cancel-003");
        sagaEngineService.avanzarFase(proceso.getId(), 1);
        sagaEngineService.avanzarFase(proceso.getId(), 2);
        sagaEngineService.avanzarFase(proceso.getId(), 3);
        sagaEngineService.avanzarFase(proceso.getId(), 4);

        // Verificar que está COMPLETADO antes de intentar cancelar
        CopyProcess procesoCompletado = queryPort.consultarProceso(proceso.getId());
        assertThat(procesoCompletado.getEstado()).isEqualTo(ProcessState.COMPLETADO);

        // WHEN / THEN — cancelar proceso COMPLETADO debe lanzar excepción
        assertThatThrownBy(() -> cancelPort.cancelar(proceso.getId(), "admin-cancel"))
                .isInstanceOf(CopyProcessCancelDeniedException.class);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CopyProcess crearProceso(String iniciadoPor) {
        return startPort.iniciar(new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                UUID.randomUUID(),
                "Empresa Destino Cancel",
                null,
                iniciadoPor,
                false
        ));
    }
}
