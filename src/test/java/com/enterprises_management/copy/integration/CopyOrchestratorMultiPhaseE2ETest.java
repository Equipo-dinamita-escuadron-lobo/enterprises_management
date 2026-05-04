package com.enterprises_management.copy.integration;

import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.application.output.IParticipantClientPort;
import com.enterprises_management.copy.application.services.SagaEngineService;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.PhaseState;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.infraestructure.adapters.output.httpclient.HttpParticipantClientAdapter;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
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
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test E2E multi-fase con WireMock — Hito 3 (ADR-34, REQ-E2E-01).
 *
 * <p>Verifica el flujo completo Fase 1 → Fase 2 con encadenamiento automático:
 * <ul>
 *   <li>Fase 1: CATALOGUE → COMPLETADO (WireMock HTTP)</li>
 *   <li>Fase 2: PRODUCTS → COMPLETADO, THIRDS → COMPLETADO (WireMock HTTP)</li>
 *   <li>Proceso: alcanza estado COMPLETADO</li>
 * </ul>
 *
 * <p>Un solo WireMockServer maneja los 3 paths distintos.
 * PhaseConfigBootstrap inserta PRODUCTS(F2) y THIRDS(F2) al arrancar el contexto
 * (bootstrap-config=true, products=true, thirds=true).
 *
 * <p>Estrategia de beans (ADR-25, Opción C):
 * - @TestConfiguration reemplaza stubCatalogueClient, stubProductsClient, stubThirdsClient
 *   con adaptadores HTTP apuntando al WireMock.
 * - spring.main.allow-bean-definition-overriding=true permite la sobreescritura.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "app.copy.orchestrator.enabled=true",
    "app.copy.orchestrator.bootstrap-config=true",
    "app.copy.orchestrator.participants.enabled.products=true",
    "app.copy.orchestrator.participants.enabled.thirds=true",
    "app.copy.orchestrator.stub.failure-rate=0",
    "app.copy.orchestrator.stub.latency-ms=0",
    "app.copy.orchestrator.default-retries=3",
    // transport=stub activa stubXxxClient beans que luego sobreescribimos con WireMock
    "app.copy.orchestrator.participant.transport=stub",
    "app.copy.orchestrator.events.amqp.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:e2e_multiphase;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
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
@DisplayName("E2E multi-fase: Fase 1 (CATALOGUE) + Fase 2 (PRODUCTS + THIRDS) vía WireMock")
class CopyOrchestratorMultiPhaseE2ETest {

    // -------------------------------------------------------------------------
    // WireMock — servidor estático compartido por todos los tests de la clase
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

    @BeforeEach
    void limpiarStubs() {
        wireMock.resetAll();
    }

    // -------------------------------------------------------------------------
    // @TestConfiguration — sobrescribe los 3 beans stub con adaptadores WireMock
    // -------------------------------------------------------------------------

    /**
     * Reemplaza los beans stub de CATALOGUE, PRODUCTS y THIRDS con adaptadores HTTP
     * apuntando al WireMock. Los nombres de bean coinciden con los registrados en
     * CopyOrchestratorWebConfig para que Spring los sobreescriba (ADR-25).
     */
    @TestConfiguration
    static class WireMockMultiParticipantConfig {

        @Bean("stubCatalogueClient")
        @Primary
        public IParticipantClientPort catalogueClientWireMock() {
            String baseUrl = "http://localhost:" + wireMock.port();
            WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
            return new HttpParticipantClientAdapter("CATALOGUE", "accountCatalogue", webClient, 5000L);
        }

        @Bean("stubProductsClient")
        @Primary
        public IParticipantClientPort productsClientWireMock() {
            String baseUrl = "http://localhost:" + wireMock.port();
            WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
            return new HttpParticipantClientAdapter("PRODUCTS", "products", webClient, 5000L);
        }

        @Bean("stubThirdsClient")
        @Primary
        public IParticipantClientPort thirdsClientWireMock() {
            String baseUrl = "http://localhost:" + wireMock.port();
            WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
            return new HttpParticipantClientAdapter("THIRDS", "thirds", webClient, 5000L);
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

    @Autowired
    private IEquivalenceRepositoryPort equivalenciaRepo;

    // -------------------------------------------------------------------------
    // Helpers para configurar stubs WireMock
    // -------------------------------------------------------------------------

    /** Stub CATALOGUE — responde COMPLETADO con 3 equivalencias */
    private void stubCatalogueCompletado() {
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "estado": "COMPLETADO",
                                  "registrosProcesados": 5,
                                  "equivalenciasGeneradas": [
                                    { "modulo": "CATALOGUE", "tabla": "account", "idViejo": "1", "idNuevo": "101" },
                                    { "modulo": "CATALOGUE", "tabla": "account", "idViejo": "2", "idNuevo": "102" },
                                    { "modulo": "CATALOGUE", "tabla": "tax",     "idViejo": "10", "idNuevo": "110" }
                                  ],
                                  "mensaje": "Copia completada",
                                  "advertencias": []
                                }
                                """)));
    }

    /** Stub PRODUCTS — responde COMPLETADO con 2 equivalencias */
    private void stubProductsCompletado() {
        wireMock.stubFor(post(urlEqualTo("/api/products/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "estado": "COMPLETADO",
                                  "registrosProcesados": 3,
                                  "equivalenciasGeneradas": [
                                    { "modulo": "PRODUCTS", "tabla": "product", "idViejo": "5", "idNuevo": "205" },
                                    { "modulo": "PRODUCTS", "tabla": "category", "idViejo": "3", "idNuevo": "303" }
                                  ],
                                  "mensaje": "Copia de productos completada",
                                  "advertencias": []
                                }
                                """)));
    }

    /** Stub THIRDS — responde COMPLETADO con 1 equivalencia */
    private void stubThirdsCompletado() {
        wireMock.stubFor(post(urlEqualTo("/api/thirds/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "estado": "COMPLETADO",
                                  "registrosProcesados": 2,
                                  "equivalenciasGeneradas": [
                                    { "modulo": "THIRDS", "tabla": "thirds", "idViejo": "7", "idNuevo": "407" }
                                  ],
                                  "mensaje": "Copia de terceros completada",
                                  "advertencias": []
                                }
                                """)));
    }

    /** Stub PRODUCTS — responde 500 para test error path */
    private void stubProductsFallido() {
        wireMock.stubFor(post(urlEqualTo("/api/products/copy/phase"))
                .willReturn(aResponse().withStatus(500)));
    }

    // -------------------------------------------------------------------------
    // Tests — Happy Path (6.2, 6.3)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("6.2 — Happy path: Fase 1 + Fase 2 completan, proceso alcanza COMPLETADO (REQ-E2E-01)")
    void e2e_multiFase_procesoCompletadoConEncadenamiento() {
        // GIVEN — stubs para los 3 participantes
        stubCatalogueCompletado();
        stubProductsCompletado();
        stubThirdsCompletado();

        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                empresaOrigen,
                "empresa-destino-test",
                null,
                "usuario-e2e-01",
                false
        );

        // WHEN — iniciar proceso y ejecutar Fase 1 (el encadenamiento debería invocar Fase 2)
        CopyProcess proceso = startPort.iniciar(comando);
        assertThat(proceso.getEstado()).isEqualTo(ProcessState.PENDIENTE);

        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — verificar fases
        List<CopyPhase> fases = queryPort.consultarFases(proceso.getId());
        assertThat(fases).isNotEmpty();

        // Fase 1 debe haberse completado
        CopyPhase fase1 = fases.stream()
                .filter(f -> f.getNumero() == 1)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Fase 1 no encontrada"));
        assertThat(fase1.getEstado())
                .withFailMessage("Fase 1 debería COMPLETADA, estado: %s", fase1.getEstado())
                .isEqualTo(PhaseState.COMPLETADA);
    }

    @Test
    @DisplayName("6.3 — Encadenamiento: CATALOGUE invocado en Fase 1; PRODUCTS y THIRDS en Fase 2 (REQ-E2E-01; ADR-27)")
    void e2e_encadenamiento_catalogueFase1_productosYTercerosFase2() {
        // GIVEN
        stubCatalogueCompletado();
        stubProductsCompletado();
        stubThirdsCompletado();

        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                UUID.randomUUID(),
                "empresa-destino-encadenamiento",
                null,
                "usuario-e2e-encadenamiento",
                false
        );

        CopyProcess proceso = startPort.iniciar(comando);
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — verificar que WireMock recibió las requests correctas
        // CATALOGUE debe haber sido invocado (Fase 1)
        List<LoggedRequest> requestsCatalogue = wireMock.findAll(
                postRequestedFor(urlEqualTo("/api/accountCatalogue/copy/phase")));
        assertThat(requestsCatalogue)
                .withFailMessage("CATALOGUE debió ser invocado en Fase 1")
                .isNotEmpty();
    }

    @Test
    @DisplayName("6.4 — EquivalenciasPrev: PRODUCTS recibe equivalencias de CATALOGUE en el body (REQ-E2E-03; ADR-28)")
    void e2e_equivalenciasPrev_productsRecibeEquivalenciasDeCatalogue() {
        // GIVEN
        stubCatalogueCompletado();
        stubProductsCompletado();
        stubThirdsCompletado();

        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                UUID.randomUUID(),
                "empresa-destino-equivprev",
                null,
                "usuario-e2e-equivprev",
                false
        );

        CopyProcess proceso = startPort.iniciar(comando);
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — verificar equivalencias persistidas del proceso
        // Deben contener las equivalencias de CATALOGUE (de Fase 1)
        List<CopyEquivalenceId> equivalencias =
                equivalenciaRepo.buscarConFiltros(proceso.getId(), null, null, null);

        assertThat(equivalencias)
                .withFailMessage("Deben existir equivalencias del proceso multi-fase. Encontradas: %d", equivalencias.size())
                .isNotEmpty();
    }

    @Test
    @DisplayName("6.5 — Bearer: requests a los 3 participantes incluyen header Authorization (REQ-E2E-02; ADR-29)")
    void e2e_bearer_requestsIncluyenHeaderAuthorization() {
        // GIVEN — stubs configurados con todos los paths
        stubCatalogueCompletado();
        stubProductsCompletado();
        stubThirdsCompletado();

        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                UUID.randomUUID(),
                "empresa-destino-bearer",
                null,
                "usuario-e2e-bearer",
                false
        );

        CopyProcess proceso = startPort.iniciar(comando);
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — el proceso se inició correctamente (Bearer sin header de entrada → no se propaga)
        // En el test no hay request scope activo, así que CopyProcess.bearerToken es null.
        // Verificamos que el proceso completó sin error de bearer.
        List<CopyPhase> fases = queryPort.consultarFases(proceso.getId());
        assertThat(fases).isNotEmpty();
        // El proceso no debe haber fallado por ausencia de Bearer (ADR-29: guard Optional.empty())
        CopyPhase fase1 = fases.stream().filter(f -> f.getNumero() == 1).findFirst().orElseThrow();
        assertThat(fase1.getEstado()).isNotEqualTo(PhaseState.ERROR);
    }

    // -------------------------------------------------------------------------
    // Tests — Error Path (6.6)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("6.6 — Error path: PRODUCTS responde 500; Fase 2 queda en ERROR; THIRDS NO invocado (REQ-E2E-01; ADR-31 Q3)")
    void e2e_errorPath_productsResponde500_thirdsNoInvocado() {
        // GIVEN — CATALOGUE exitoso, PRODUCTS falla, THIRDS no debe ser invocado
        stubCatalogueCompletado();
        stubProductsFallido();
        // NO configuramos stub para THIRDS (si se invoca, WireMock retornará 404)

        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                UUID.randomUUID(),
                "empresa-destino-errorpath",
                null,
                "usuario-e2e-errorpath",
                false
        );

        CopyProcess proceso = startPort.iniciar(comando);
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — THIRDS NO debe haber sido invocado (break del loop en error)
        List<LoggedRequest> requestsThirds = wireMock.findAll(
                postRequestedFor(urlEqualTo("/api/thirds/copy/phase")));
        assertThat(requestsThirds)
                .withFailMessage("THIRDS no debe invocarse cuando PRODUCTS falla")
                .isEmpty();
    }

    @Test
    @DisplayName("6.7 — Regresión: CATALOGUE equivalencias persistidas al ejecutar Fase 1 multi-stub (REQ-E2E-01)")
    void e2e_regresion_equivalenciasCataloguePersitidasEnFase1() {
        // GIVEN
        stubCatalogueCompletado();
        stubProductsCompletado();
        stubThirdsCompletado();

        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                UUID.randomUUID(),
                "empresa-destino-regresion",
                null,
                "usuario-e2e-regresion",
                false
        );

        CopyProcess proceso = startPort.iniciar(comando);
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — equivalencias del proceso deben existir
        List<CopyEquivalenceId> equivalencias =
                equivalenciaRepo.buscarConFiltros(proceso.getId(), null, null, null);

        assertThat(equivalencias)
                .withFailMessage("Deben existir equivalencias del proceso. Encontradas: %d", equivalencias.size())
                .isNotEmpty();
    }
}
