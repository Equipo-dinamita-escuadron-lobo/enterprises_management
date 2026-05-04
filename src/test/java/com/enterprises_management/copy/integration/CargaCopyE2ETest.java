package com.enterprises_management.copy.integration;

import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.services.SagaEngineService;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyProcess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de carga E2E: verifica throughput del orquestador con stub transport.
 * N procesos completos (F1+F2+F3 = 9 participantes) deben completar en < 30s.
 * REQ-E2E-H4-02, ADR-42. Excluido de CI normal con @Tag("loadtest").
 */
@Tag("loadtest")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "app.copy.orchestrator.enabled=true",
    "app.copy.orchestrator.bootstrap-config=true",
    "app.copy.orchestrator.participants.enabled.products=true",
    "app.copy.orchestrator.participants.enabled.thirds=true",
    "app.copy.orchestrator.participants.enabled.treasury=true",
    "app.copy.orchestrator.participants.enabled.stock=true",
    "app.copy.orchestrator.participants.enabled.kardex=true",
    "app.copy.orchestrator.participants.enabled.factures=true",
    "app.copy.orchestrator.participants.enabled.inventorypeps=true",
    "app.copy.orchestrator.participants.enabled.auxiliarybook=true",
    "app.copy.orchestrator.stub.failure-rate=0",
    "app.copy.orchestrator.stub.latency-ms=0",
    "app.copy.orchestrator.default-retries=3",
    "app.copy.orchestrator.participant.transport=stub",
    "app.copy.orchestrator.events.amqp.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:carga_e2e;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.data-locations=",
    "jwt.auth.converter.principle-attribute=preferred_username",
    "jwt.auth.converter.resource-id=microservices_client",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false",
    "spring.main.allow-bean-definition-overriding=true"
})
@DisplayName("Carga E2E: N procesos F1+F2+F3 con stub transport < 30s (REQ-E2E-H4-02)")
class CargaCopyE2ETest {

    private static final int N_PROCESOS = 50;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ICopyProcessStartPort startPort;

    @Autowired
    private SagaEngineService sagaEngineService;

    @Autowired
    private ICopyProcessQueryPort queryPort;

    @Test
    @DisplayName("50 procesos F1+F2+F3 completan con stub en < 30s y promedio < 200ms por proceso")
    void carga_nProcesosCompletanEnTiempo() {
        long inicio = System.currentTimeMillis();

        for (int i = 0; i < N_PROCESOS; i++) {
            IniciarProcesoCommand cmd = new IniciarProcesoCommand(
                    CopyProcessType.DUPLICATE,
                    UUID.randomUUID(),
                    "empresa-destino-carga-" + i,
                    null,
                    "usuario-carga-" + i,
                    false
            );
            CopyProcess proceso = startPort.iniciar(cmd);
            sagaEngineService.avanzarFase(proceso.getId(), 1);

            CopyProcess final_ = queryPort.consultarProceso(proceso.getId());
            assertThat(final_.getEstado())
                    .withFailMessage("Proceso %d debería COMPLETADO, estado: %s", i, final_.getEstado())
                    .isEqualTo(ProcessState.COMPLETADO);
        }

        long duracionMs = System.currentTimeMillis() - inicio;
        assertThat(duracionMs)
                .withFailMessage("%d procesos tardaron %dms, límite 30s", N_PROCESOS, duracionMs)
                .isLessThan(30_000L);
        long promedioMs = duracionMs / N_PROCESOS;
        assertThat(promedioMs)
                .withFailMessage("%d procesos promedio %dms por proceso, límite 200ms", N_PROCESOS, promedioMs)
                .isLessThan(200L);
    }
}
