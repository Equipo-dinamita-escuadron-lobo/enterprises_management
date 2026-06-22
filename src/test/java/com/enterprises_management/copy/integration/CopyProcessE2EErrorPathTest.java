package com.enterprises_management.copy.integration;

import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.services.SagaEngineService;
import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.enums.PhaseState;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
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

/**
 * Test de integración E2E del flujo de error del orquestador de saga (REQ-PROC-03, REQ-MOD-02).
 *
 * <p>Estrategia: stub.failure-rate=1 → todos los módulos fallan con error reintentable.
 * Con default-retries=3, el motor agota los 3 intentos y marca el módulo ERROR_NO_REINTENTABLE.
 * Verifica que la fase 1 queda en ERROR y el proceso queda en ERROR.
 *
 * <p>ADR-12: el motor de saga no inicia la fase 2 si la fase 1 terminó en ERROR.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    // Activar orquestador con failure-rate=1 (todos los módulos fallan)
    "app.copy.orchestrator.enabled=true",
    "app.copy.orchestrator.bootstrap-config=true",
    "app.copy.orchestrator.stub.failure-rate=1",
    "app.copy.orchestrator.stub.latency-ms=0",
    "app.copy.orchestrator.default-retries=3",
    // Hito 2 — transporte stub para que StubParticipantClient sea el bean activo (ADR-19)
    "app.copy.orchestrator.participant.transport=stub",
    "app.copy.orchestrator.events.amqp.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:e2e_error;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
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
@DisplayName("E2E — Flujo de error: failure-rate=1, 3 reintentos, proceso ERROR")
class CopyProcessE2EErrorPathTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ICopyProcessStartPort startPort;

    @Autowired
    private SagaEngineService sagaEngineService;

    @Autowired
    private ICopyProcessQueryPort queryPort;

    // -------------------------------------------------------------------------
    // REQ-PROC-03, REQ-MOD-02, REQ-FASE-02
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("failure-rate=1: fase 1 queda en ERROR, proceso queda en ERROR (REQ-PROC-03)")
    void errorPath_fase1EnError_procesoEnError() {
        // GIVEN
        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                empresaOrigen,
                "Empresa Destino Error",
                null,
                "usuario-error-001",
                false
        );

        // WHEN — iniciar y ejecutar fase 1 (todos los módulos fallan)
        CopyProcess proceso = startPort.iniciar(comando);
        assertThat(proceso.getEstado()).isEqualTo(ProcessState.PENDIENTE);

        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — proceso en ERROR
        CopyProcess procesoFinal = queryPort.consultarProceso(proceso.getId());
        assertThat(procesoFinal.getEstado())
                .withFailMessage("Con failure-rate=1, el proceso debe quedar en ERROR")
                .isEqualTo(ProcessState.ERROR);
        assertThat(procesoFinal.getFinalizadoEn()).isNotNull();
    }

    @Test
    @DisplayName("failure-rate=1: fase 1 queda en estado ERROR (REQ-FASE-02)")
    void errorPath_fase1EnEstadoError() {
        // GIVEN
        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.BACKUP,
                empresaOrigen,
                null,
                null,
                "usuario-error-002",
                false
        );

        // WHEN
        CopyProcess proceso = startPort.iniciar(comando);
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — fase 1 en ERROR
        List<CopyPhase> fases = queryPort.consultarFases(proceso.getId());
        CopyPhase fase1 = fases.stream()
                .filter(f -> f.getNumero() == 1)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Fase 1 no encontrada"));

        assertThat(fase1.getEstado())
                .withFailMessage("Con failure-rate=1, la fase 1 debe quedar en ERROR")
                .isEqualTo(PhaseState.ERROR);
    }

    @Test
    @DisplayName("failure-rate=1: evento Proceso.error registrado (REQ-EVT-01)")
    void errorPath_eventoProcesoErrorRegistrado() {
        // GIVEN
        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                empresaOrigen,
                "Empresa Destino Error Eventos",
                null,
                "usuario-error-003",
                false
        );

        // WHEN
        CopyProcess proceso = startPort.iniciar(comando);
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — debe existir evento PROCESO_ERROR
        List<CopyProcessEvent> eventos = queryPort.consultarEventos(proceso.getId());
        List<CopyEventType> tiposEvento = eventos.stream()
                .map(CopyProcessEvent::getTipoEvento)
                .toList();

        assertThat(tiposEvento)
                .withFailMessage("Se esperaba el evento PROCESO_ERROR. Eventos registrados: %s", tiposEvento)
                .contains(CopyEventType.PROCESO_ERROR);
    }
}
