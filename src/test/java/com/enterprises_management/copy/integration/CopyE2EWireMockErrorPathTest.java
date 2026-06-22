package com.enterprises_management.copy.integration;

import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.output.IParticipantClientPort;
import com.enterprises_management.copy.application.services.SagaEngineService;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.PhaseState;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.infraestructure.adapters.output.httpclient.HttpParticipantClientAdapter;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test E2E WireMock — Error Path (7.1.2, ADR-25).
 *
 * <p>Cubre los escenarios de error del adaptador HTTP integrado con el SagaEngine:
 * <ol>
 *   <li>Participante responde 5xx → SagaEngine clasifica como errorReintentable,
 *       reintenta (default-retries=2) y eventualmente marca la fase como ERROR.</li>
 *   <li>Participante responde 422 → errorNoReintentable → fase ERROR inmediata (sin reintentos).</li>
 * </ol>
 *
 * <p>Nota: el escenario de timeout no se incluye en este test E2E de integración porque
 * un stub WireMock con delay lento puede interferir con tests consecutivos que comparten
 * el mismo ApplicationContext. El timeout está cubierto a nivel unitario en
 * {@link HttpParticipantClientAdapterTest} (escenario 3.1.6) y a nivel de integración
 * básica en la clase {@link CopyE2EWireMockTimeoutTest}.
 *
 * <p>Cubre REQ-CLIENT-03, ADR-25, ADR-26.
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
    "spring.datasource.url=jdbc:h2:mem:e2e_wiremock_error;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
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
    // Permite sobreescribir el bean stubCatalogueClient (ADR-25)
    "spring.main.allow-bean-definition-overriding=true"
})
@DisplayName("E2E WireMock — Error Path: 5xx reintentable, 4xx no-reintentable")
class CopyE2EWireMockErrorPathTest {

    // -------------------------------------------------------------------------
    // WireMock lifecycle
    // -------------------------------------------------------------------------

    static WireMockServer wireMock;

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

    @BeforeEach
    void limpiarStubs() {
        wireMock.resetAll();
    }

    // -------------------------------------------------------------------------
    // TestConfiguration — adapter HTTP con timeout suficiente para respuestas HTTP
    // El timeout de 5000ms garantiza que respuestas 5xx/4xx de WireMock
    // llegan dentro del límite, sin interferencia entre tests.
    // -------------------------------------------------------------------------

    @TestConfiguration
    static class WireMockErrorParticipantConfig {

        @Bean("stubCatalogueClient")
        @Primary
        public IParticipantClientPort catalogueClientWireMockError() {
            String baseUrl = "http://localhost:" + wireMock.port();
            WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
            // 5000ms timeout — suficiente para WireMock local. El timeout unitario
            // (3.1.6) cubre el escenario de timeout real.
            return new HttpParticipantClientAdapter(
                    "CATALOGUE",
                    "accountCatalogue",
                    webClient,
                    5000L
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
    // Helper
    // -------------------------------------------------------------------------

    private CopyProcess iniciarProceso(String usuario) {
        return startPort.iniciar(new IniciarProcesoCommand(
                CopyProcessType.BACKUP,
                UUID.randomUUID(),
                null,
                "backup-ref",
                usuario,
                false
        ));
    }

    // -------------------------------------------------------------------------
    // 7.1.2 — Participante 5xx → SagaEngine reintenta y eventualmente marca ERROR
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("7.1.2 — HTTP 500 (5xx): SagaEngine reintenta y marca fase ERROR tras agotar intentos")
    void e2e_wireMock500_sagaEngineReintentaYMarcaError() {
        // GIVEN — WireMock siempre responde 500
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "error": "error interno del servidor participante" }
                                """)));

        // WHEN
        CopyProcess proceso = iniciarProceso("usuario-error-500");
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — el proceso debe haber terminado en ERROR
        CopyProcess procesoFinal = queryPort.consultarProceso(proceso.getId());
        assertThat(procesoFinal.getEstado())
                .withFailMessage("Con participante HTTP 500, el proceso debe quedar en ERROR")
                .isEqualTo(ProcessState.ERROR);

        // La fase 1 también debe estar en ERROR
        List<CopyPhase> fases = queryPort.consultarFases(proceso.getId());
        CopyPhase fase1 = fases.stream()
                .filter(f -> f.getNumero() == 1)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Fase 1 no encontrada"));
        assertThat(fase1.getEstado())
                .withFailMessage("Fase 1 debe estar en ERROR tras agotar reintentos")
                .isEqualTo(PhaseState.ERROR);
    }

    @Test
    @DisplayName("7.1.2 — HTTP 500: WireMock recibió = default-retries llamadas (reintentos agotados)")
    void e2e_wireMock500_wireMockRecibioNumeroDeReintentos() {
        // GIVEN — default-retries=2, WireMock siempre 500
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("error")));

        // WHEN
        CopyProcess proceso = iniciarProceso("usuario-reintentos-500");
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — WireMock debería haber recibido exactamente 2 llamadas (default-retries=2)
        List<com.github.tomakehurst.wiremock.verification.LoggedRequest> solicitudes =
                wireMock.findAll(com.github.tomakehurst.wiremock.client.WireMock
                        .postRequestedFor(urlEqualTo("/api/accountCatalogue/copy/phase")));

        assertThat(solicitudes)
                .withFailMessage("Con default-retries=2 y errores 5xx, WireMock debe recibir 2 llamadas. Recibidas: %d", solicitudes.size())
                .hasSize(2);
    }

    // -------------------------------------------------------------------------
    // 7.1.2 — HTTP 422 → errorNoReintentable → ERROR inmediato (sin reintentos)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("7.1.2 — HTTP 422 (no reintentable): SagaEngine no reintenta y marca ERROR inmediato")
    void e2e_wireMock422_sinReintentosErrorInmediato() {
        // GIVEN — WireMock responde 422 (error estructural, no reintentable)
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "error": "entOrigen y entDestino no pueden ser iguales" }
                                """)));

        // WHEN
        CopyProcess proceso = iniciarProceso("usuario-422");
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — proceso en ERROR
        CopyProcess procesoFinal = queryPort.consultarProceso(proceso.getId());
        assertThat(procesoFinal.getEstado())
                .withFailMessage("Con participante HTTP 422, el proceso debe quedar en ERROR")
                .isEqualTo(ProcessState.ERROR);

        // Con error no-reintentable, WireMock debe haber recibido exactamente 1 llamada
        List<com.github.tomakehurst.wiremock.verification.LoggedRequest> solicitudes =
                wireMock.findAll(com.github.tomakehurst.wiremock.client.WireMock
                        .postRequestedFor(urlEqualTo("/api/accountCatalogue/copy/phase")));

        assertThat(solicitudes)
                .withFailMessage("Con HTTP 422 (no reintentable), WireMock debe recibir exactamente 1 llamada. Recibidas: %d", solicitudes.size())
                .hasSize(1);
    }
}
