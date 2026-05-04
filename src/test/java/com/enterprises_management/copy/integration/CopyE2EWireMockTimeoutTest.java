package com.enterprises_management.copy.integration;

import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.output.IParticipantClientPort;
import com.enterprises_management.copy.application.services.SagaEngineService;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.infraestructure.adapters.output.httpclient.HttpParticipantClientAdapter;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test E2E WireMock — Timeout scenario (7.1.2, REQ-CLIENT-04, ADR-25).
 *
 * <p>Clase dedicada para el escenario de timeout para evitar interferencia
 * con otros tests de error path que comparten el mismo ApplicationContext.
 * Un stub WireMock con delay > timeout del adapter puede hacer que las
 * respuestas en vuelo interfieran con tests consecutivos.
 *
 * <p>Verifica que cuando un participante no responde dentro del timeout,
 * el SagaEngine clasifica el error como errorReintentable y eventualmente
 * marca la fase como ERROR tras agotar reintentos (REQ-CLIENT-04, ADR-26).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "app.copy.orchestrator.enabled=true",
    "app.copy.orchestrator.bootstrap-config=true",
    "app.copy.orchestrator.stub.failure-rate=0",
    "app.copy.orchestrator.stub.latency-ms=0",
    "app.copy.orchestrator.default-retries=2",
    "app.copy.orchestrator.participant.transport=stub",
    "app.copy.orchestrator.events.amqp.enabled=false",
    // Base de datos separada para aislar este test del resto (ADR-25)
    "spring.datasource.url=jdbc:h2:mem:e2e_wiremock_timeout;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
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
@DisplayName("E2E WireMock — Timeout: participante no responde en tiempo → errorReintentable")
class CopyE2EWireMockTimeoutTest {

    // -------------------------------------------------------------------------
    // WireMock lifecycle — servidor dedicado para este test
    // -------------------------------------------------------------------------

    private static WireMockServer wireMock;

    @BeforeAll
    static void iniciarWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
    }

    @AfterAll
    static void detenerWireMock() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    // -------------------------------------------------------------------------
    // TestConfiguration — adapter con timeout muy corto (100ms)
    // WireMock responde en 300ms > 100ms → timeout garantizado
    // -------------------------------------------------------------------------

    @TestConfiguration
    static class WireMockTimeoutParticipantConfig {

        @Bean("stubCatalogueClient")
        @Primary
        public IParticipantClientPort catalogueClientWireMockTimeout() {
            String baseUrl = "http://localhost:" + wireMock.port();
            WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
            // 100ms timeout — el stub responde en 300ms, lo que garantiza timeout (REQ-CLIENT-04)
            return new HttpParticipantClientAdapter(
                    "CATALOGUE",
                    "accountCatalogue",
                    webClient,
                    100L
            );
        }
    }

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ICopyProcessStartPort startPort;

    @Autowired
    private SagaEngineService sagaEngineService;

    @Autowired
    private ICopyProcessQueryPort queryPort;

    // -------------------------------------------------------------------------
    // 7.1.2 — Timeout: SagaEngine clasifica como errorReintentable y agota reintentos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("7.1.2 — Timeout (respuesta > 100ms): SagaEngine reintenta y marca ERROR")
    void e2e_wireMockTimeout_sagaEngineClasificaComoErrorReintentable() {
        // GIVEN — WireMock responde con 300ms de delay (> 100ms timeout del adapter)
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(300)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "estado": "COMPLETADO",
                                  "registrosProcesados": 1,
                                  "equivalenciasGeneradas": [],
                                  "mensaje": "ok (nunca llegará a tiempo)",
                                  "advertencias": []
                                }
                                """)));

        // WHEN — iniciar proceso y avanzar fase 1
        CopyProcess proceso = startPort.iniciar(new IniciarProcesoCommand(
                CopyProcessType.BACKUP,
                UUID.randomUUID(),
                null,
                "backup-ref-timeout",
                "usuario-timeout",
                false
        ));
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — el proceso debe terminar en ERROR:
        // timeout (100ms) → errorReintentable → 2 intentos agotados → ERROR
        CopyProcess procesoFinal = queryPort.consultarProceso(proceso.getId());
        assertThat(procesoFinal.getEstado())
                .withFailMessage("Con timeout en participante HTTP, el proceso debe quedar en ERROR. " +
                        "Estado actual: %s", procesoFinal.getEstado())
                .isEqualTo(ProcessState.ERROR);
    }
}
