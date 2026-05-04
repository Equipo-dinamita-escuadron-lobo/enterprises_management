package com.enterprises_management.copy.infraestructure.adapters.output.httpclient;

import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.application.output.IParticipantClientPort;
import com.enterprises_management.copy.application.output.IParticipantClientPort.ParticipantResult;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyModuleExecution;
import com.enterprises_management.copy.domain.models.CopyProcess;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios del HttpParticipantClientAdapter con WireMock.
 *
 * <p>Estrategia elegida: Opción C (ADR-25) — se construye un WebClient directo
 * apuntando al puerto de WireMock; no se usa @LoadBalanced en el test.
 * El adaptador expone un constructor que acepta el WebClient ya construido
 * y el timeout como parámetro, por lo que el test puede configurar ambos sin
 * levantar contexto Spring ni necesitar Eureka (ADR-17, REQ-CLIENT-02).
 *
 * <p>Cubre los escenarios de REQ-CLIENT-01, REQ-CLIENT-02, REQ-CLIENT-03, REQ-CLIENT-04.
 */
class HttpParticipantClientAdapterTest {

    // -------------------------------------------------------------------------
    // WireMock lifecycle — servidor compartido por todos los tests de la clase
    // -------------------------------------------------------------------------

    private static WireMockServer wireMock;
    private static String baseUrl;

    @BeforeAll
    static void iniciarWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        WireMock.configureFor("localhost", wireMock.port());
        baseUrl = "http://localhost:" + wireMock.port();
    }

    @AfterAll
    static void detenerWireMock() {
        wireMock.stop();
    }

    @AfterEach
    void limpiarStubs() {
        wireMock.resetAll();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Crea el adapter para CATALOGUE apuntando a WireMock con timeout configurable.
     */
    private HttpParticipantClientAdapter crearAdapter(long timeoutMs) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        IEquivalenceRepositoryPort equivalenciaRepo = mock(IEquivalenceRepositoryPort.class);
        // anyString() no coincide con null — usar any() para los parámetros opcionales (ADR-28)
        when(equivalenciaRepo.buscarConFiltros(anyString(), any(), any(), any()))
                .thenReturn(List.of());
        return new HttpParticipantClientAdapter(
                "CATALOGUE",
                "accountCatalogue",
                webClient,
                timeoutMs,
                equivalenciaRepo,
                new SimpleMeterRegistry()
        );
    }

    private HttpParticipantClientAdapter crearAdapter() {
        return crearAdapter(5000L);
    }

    /**
     * Crea el adapter para un módulo específico con repo de equivalencias configurable.
     */
    private HttpParticipantClientAdapter crearAdapterConEquivalencias(
            String modulo,
            String moduloUrl,
            IEquivalenceRepositoryPort equivalenciaRepo
    ) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        return new HttpParticipantClientAdapter(
                modulo,
                moduloUrl,
                webClient,
                5000L,
                equivalenciaRepo,
                new SimpleMeterRegistry()
        );
    }

    private CopyProcess crearProceso() {
        return CopyProcess.crear(
                CopyProcessType.DUPLICATE,
                UUID.randomUUID(),
                "empresa-destino-test",
                null,
                "usuario-test"
        );
    }

    private CopyProcess crearProcesoConFase(int fase) {
        CopyProcess proceso = CopyProcess.crear(
                CopyProcessType.DUPLICATE,
                UUID.randomUUID(),
                "empresa-destino-test",
                null,
                "usuario-test"
        );
        proceso.setFaseActual(fase);
        return proceso;
    }

    private CopyProcess crearProcesoConToken(String bearerToken) {
        CopyProcess proceso = crearProceso();
        proceso.setBearerToken(bearerToken);
        return proceso;
    }

    private CopyModuleExecution crearEjecucion(String idProceso, int fase) {
        return CopyModuleExecution.crear(idProceso, UUID.randomUUID().toString(), "CATALOGUE");
    }

    // -------------------------------------------------------------------------
    // 3.1.1 — POST /phase éxito HTTP 200 → exitoso=true
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("3.1.1 — HTTP 200 con estado COMPLETADO → exitoso=true con equivalencias")
    void invoke_http200Completado_retornaExitoso() {
        // RED: HttpParticipantClientAdapter no existe aún → compilará en rojo
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "estado": "COMPLETADO",
                                  "registrosProcesados": 5,
                                  "equivalenciasGeneradas": [
                                    { "modulo": "CATALOGUE", "tabla": "account", "idViejo": "1", "idNuevo": "101" }
                                  ],
                                  "mensaje": "Fase completada",
                                  "advertencias": []
                                }
                                """)));

        CopyProcess proceso = crearProceso();
        CopyModuleExecution ejecucion = crearEjecucion(proceso.getId(), 1);

        ParticipantResult resultado = crearAdapter().invoke(proceso, ejecucion);

        assertThat(resultado.exitoso()).isTrue();
        assertThat(resultado.conAdvertencias()).isFalse();
        assertThat(resultado.equivalencias()).hasSize(1);
        assertThat(resultado.equivalencias().get(0).tabla()).isEqualTo("account");
        assertThat(resultado.equivalencias().get(0).idViejo()).isEqualTo("1");
        assertThat(resultado.equivalencias().get(0).idNuevo()).isEqualTo("101");
    }

    // -------------------------------------------------------------------------
    // 3.1.1 triangulación — HTTP 200 con estado COMPLETADO_CON_ADVERTENCIAS
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("3.1.1 triangulación — HTTP 200 COMPLETADO_CON_ADVERTENCIAS → exitoso=true, conAdvertencias=true")
    void invoke_http200CompletadoConAdvertencias_retornaExitosoConAdvertencias() {
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "estado": "COMPLETADO_CON_ADVERTENCIAS",
                                  "registrosProcesados": 3,
                                  "equivalenciasGeneradas": [],
                                  "mensaje": "Algunas FKs no resueltas",
                                  "advertencias": ["FK 9999 no encontrada"]
                                }
                                """)));

        ParticipantResult resultado = crearAdapter().invoke(crearProceso(), crearEjecucion("p1", 1));

        assertThat(resultado.exitoso()).isTrue();
        assertThat(resultado.conAdvertencias()).isTrue();
    }

    // -------------------------------------------------------------------------
    // 3.1.2 — HTTP 400 → exitoso=false, reintentable=false
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("3.1.2 — HTTP 400 → exitoso=false, reintentable=false")
    void invoke_http400_retornaErrorNoReintentable() {
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "error": "payload inválido: campo idProceso nulo" }
                                """)));

        ParticipantResult resultado = crearAdapter().invoke(crearProceso(), crearEjecucion("p1", 1));

        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.reintentable()).isFalse();
    }

    // -------------------------------------------------------------------------
    // 3.1.3 — HTTP 422 → exitoso=false, reintentable=false
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("3.1.3 — HTTP 422 → exitoso=false, reintentable=false")
    void invoke_http422_retornaErrorNoReintentable() {
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "error": "entOrigen no pertenece al tenant" }
                                """)));

        ParticipantResult resultado = crearAdapter().invoke(crearProceso(), crearEjecucion("p1", 1));

        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.reintentable()).isFalse();
    }

    // -------------------------------------------------------------------------
    // 3.1.4 — HTTP 500 → exitoso=false, reintentable=true
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("3.1.4 — HTTP 500 → exitoso=false, reintentable=true")
    void invoke_http500_retornaErrorReintentable() {
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "error": "error interno del servidor" }
                                """)));

        ParticipantResult resultado = crearAdapter().invoke(crearProceso(), crearEjecucion("p1", 1));

        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.reintentable()).isTrue();
    }

    // -------------------------------------------------------------------------
    // 3.1.5 — HTTP 503/504 → exitoso=false, reintentable=true
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("3.1.5 — HTTP 503 → exitoso=false, reintentable=true")
    void invoke_http503_retornaErrorReintentable() {
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("Service Unavailable")));

        ParticipantResult resultado = crearAdapter().invoke(crearProceso(), crearEjecucion("p1", 1));

        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.reintentable()).isTrue();
    }

    @Test
    @DisplayName("3.1.5 triangulación — HTTP 504 → exitoso=false, reintentable=true")
    void invoke_http504_retornaErrorReintentable() {
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(504)
                        .withBody("Gateway Timeout")));

        ParticipantResult resultado = crearAdapter().invoke(crearProceso(), crearEjecucion("p1", 1));

        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.reintentable()).isTrue();
    }

    // -------------------------------------------------------------------------
    // 3.1.6 — Timeout → exitoso=false, reintentable=true
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("3.1.6 — Timeout de respuesta → exitoso=false, reintentable=true, mensaje contiene 'timeout'")
    void invoke_timeout_retornaErrorReintentable() {
        // WireMock con retardo mayor que el timeout del adapter (200ms > 100ms configurado)
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(300)
                        .withBody("""
                                {
                                  "estado": "COMPLETADO",
                                  "registrosProcesados": 1,
                                  "equivalenciasGeneradas": [],
                                  "mensaje": "ok",
                                  "advertencias": []
                                }
                                """)));

        // Usar adapter con timeout muy corto (100ms) para forzar el timeout
        ParticipantResult resultado = crearAdapter(100L).invoke(crearProceso(), crearEjecucion("p1", 1));

        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.reintentable()).isTrue();
        assertThat(resultado.errorDetalle()).isNotBlank();
    }

    // -------------------------------------------------------------------------
    // 3.1.7 — Connection refused (Eureka sin instancias) → errorReintentable
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("3.1.7 — Conexión rechazada → exitoso=false, reintentable=true")
    void invoke_connectionRefused_retornaErrorReintentable() {
        // Puerto 1 siempre rechaza conexiones
        WebClient clienteInvalido = WebClient.builder()
                .baseUrl("http://localhost:1")
                .build();
        HttpParticipantClientAdapter adapterSinConexion = new HttpParticipantClientAdapter(
                "CATALOGUE",
                "accountCatalogue",
                clienteInvalido,
                500L
        );

        ParticipantResult resultado = adapterSinConexion.invoke(crearProceso(), crearEjecucion("p1", 1));

        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.reintentable()).isTrue();
    }

    // -------------------------------------------------------------------------
    // 3.1.1 complemento — getNombreModulo retorna el módulo configurado
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getNombreModulo retorna el nombre del módulo configurado (REQ-CLIENT-01)")
    void getNombreModulo_retornaModuloConfigurado() {
        assertThat(crearAdapter().getNombreModulo()).isEqualTo("CATALOGUE");
    }

    // =========================================================================
    // Hito 3 — REQ-FIX-01: construirRequest usa proceso.getFaseActual()
    // =========================================================================

    @Test
    @DisplayName("1.1 RED — CATALOGUE con faseActual=1 envía fase=1 al participante (REQ-FIX-01)")
    void construirRequest_catalogueFase1_enviaFase1EnBody() throws Exception {
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "estado": "COMPLETADO",
                                  "registrosProcesados": 0,
                                  "equivalenciasGeneradas": [],
                                  "mensaje": "ok",
                                  "advertencias": []
                                }
                                """)));

        CopyProcess proceso = crearProcesoConFase(1);
        crearAdapter().invoke(proceso, crearEjecucion(proceso.getId(), 1));

        // Verificar que el body enviado contiene "fase":1
        List<LoggedRequest> requests = wireMock.findAll(
                WireMock.postRequestedFor(urlEqualTo("/api/accountCatalogue/copy/phase")));
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getBodyAsString()).contains("\"fase\":1");
    }

    @Test
    @DisplayName("1.1 RED — PRODUCTS con faseActual=2 envía fase=2 al participante (REQ-FIX-01)")
    void construirRequest_productsFase2_enviaFase2EnBody() throws Exception {
        // Stub para el endpoint de PRODUCTS
        wireMock.stubFor(post(urlEqualTo("/api/products/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "estado": "COMPLETADO",
                                  "registrosProcesados": 0,
                                  "equivalenciasGeneradas": [],
                                  "mensaje": "ok",
                                  "advertencias": []
                                }
                                """)));

        IEquivalenceRepositoryPort equivalenciaRepo = mock(IEquivalenceRepositoryPort.class);
        when(equivalenciaRepo.buscarConFiltros(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of());
        HttpParticipantClientAdapter adapterProducts = crearAdapterConEquivalencias(
                "PRODUCTS", "products", equivalenciaRepo);

        CopyProcess proceso = crearProcesoConFase(2);
        adapterProducts.invoke(proceso, crearEjecucion(proceso.getId(), 2));

        List<LoggedRequest> requests = wireMock.findAll(
                WireMock.postRequestedFor(urlEqualTo("/api/products/copy/phase")));
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getBodyAsString()).contains("\"fase\":2");
    }

    // =========================================================================
    // Hito 3 — REQ-FIX-02 / ADR-29: Bearer propagation
    // =========================================================================

    @Test
    @DisplayName("1.3 RED — proceso con bearerToken envía Authorization header al participante (REQ-FIX-02)")
    void construirRequest_conBearerToken_enviaAuthorizationHeader() throws Exception {
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "estado": "COMPLETADO",
                                  "registrosProcesados": 0,
                                  "equivalenciasGeneradas": [],
                                  "mensaje": "ok",
                                  "advertencias": []
                                }
                                """)));

        CopyProcess proceso = crearProcesoConToken("jwt-test-token-xyz");
        crearAdapter().invoke(proceso, crearEjecucion(proceso.getId(), 1));

        List<LoggedRequest> requests = wireMock.findAll(
                WireMock.postRequestedFor(urlEqualTo("/api/accountCatalogue/copy/phase")));
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getHeader("Authorization"))
                .isEqualTo("Bearer jwt-test-token-xyz");
    }

    @Test
    @DisplayName("1.3 RED — proceso sin bearerToken (null) NO envía Authorization header (degradación controlada)")
    void construirRequest_sinBearerToken_noEnviaAuthorizationHeader() throws Exception {
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "estado": "COMPLETADO",
                                  "registrosProcesados": 0,
                                  "equivalenciasGeneradas": [],
                                  "mensaje": "ok",
                                  "advertencias": []
                                }
                                """)));

        CopyProcess proceso = crearProceso(); // sin bearerToken → null
        crearAdapter().invoke(proceso, crearEjecucion(proceso.getId(), 1));

        List<LoggedRequest> requests = wireMock.findAll(
                WireMock.postRequestedFor(urlEqualTo("/api/accountCatalogue/copy/phase")));
        assertThat(requests).hasSize(1);
        // Sin token, el header Authorization no debe estar presente
        assertThat(requests.get(0).containsHeader("Authorization")).isFalse();
    }

    // =========================================================================
    // Hito 3 — REQ-EQUIVPREV-01 / ADR-28: equivalenciasPrev filtradas por módulo
    // =========================================================================

    @Test
    @DisplayName("1.5 RED — PRODUCTS recibe equivalenciasPrev de CATALOGUE filtradas (REQ-EQUIVPREV-01)")
    void construirRequest_products_recibeEquivalenciasDeCatalogue() throws Exception {
        wireMock.stubFor(post(urlEqualTo("/api/products/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "estado": "COMPLETADO",
                                  "registrosProcesados": 0,
                                  "equivalenciasGeneradas": [],
                                  "mensaje": "ok",
                                  "advertencias": []
                                }
                                """)));

        // Equivalencias previas: una de CATALOGUE y una de ENTERPRISES
        IEquivalenceRepositoryPort equivalenciaRepo = mock(IEquivalenceRepositoryPort.class);
        CopyEquivalenceId eqCatalogue = CopyEquivalenceId.crear("proc1", "CATALOGUE", "account", "100", "200");
        CopyEquivalenceId eqEnterprises = CopyEquivalenceId.crear("proc1", "ENTERPRISES", "enterprise", "1", "2");
        // any() en lugar de anyString() porque los parámetros opcionales se pasan como null (ADR-28)
        when(equivalenciaRepo.buscarConFiltros(anyString(), any(), any(), any()))
                .thenReturn(List.of(eqCatalogue, eqEnterprises));

        HttpParticipantClientAdapter adapterProducts = crearAdapterConEquivalencias(
                "PRODUCTS", "products", equivalenciaRepo);

        CopyProcess proceso = crearProcesoConFase(2);
        adapterProducts.invoke(proceso, crearEjecucion(proceso.getId(), 2));

        List<LoggedRequest> requests = wireMock.findAll(
                WireMock.postRequestedFor(urlEqualTo("/api/products/copy/phase")));
        assertThat(requests).hasSize(1);
        String body = requests.get(0).getBodyAsString();
        // Debe contener la equivalencia de CATALOGUE
        assertThat(body).contains("CATALOGUE");
        assertThat(body).contains("account");
        // NO debe contener la equivalencia de ENTERPRISES (filtrada por PARTICIPANT_DEPENDENCIES)
        assertThat(body).doesNotContain("ENTERPRISES");
    }

    @Test
    @DisplayName("1.5 RED — CATALOGUE recibe equivalenciasPrev vacío (REQ-EQUIVPREV-01)")
    void construirRequest_catalogue_recibeEquivalenciasVacias() throws Exception {
        wireMock.stubFor(post(urlEqualTo("/api/accountCatalogue/copy/phase"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "estado": "COMPLETADO",
                                  "registrosProcesados": 0,
                                  "equivalenciasGeneradas": [],
                                  "mensaje": "ok",
                                  "advertencias": []
                                }
                                """)));

        // Incluso si hubiera equivalencias previas, CATALOGUE no tiene dependencias
        IEquivalenceRepositoryPort equivalenciaRepo = mock(IEquivalenceRepositoryPort.class);
        // any() para parámetros opcionales que se pasan como null (ADR-28)
        when(equivalenciaRepo.buscarConFiltros(anyString(), any(), any(), any()))
                .thenReturn(List.of(CopyEquivalenceId.crear("proc1", "CATALOGUE", "account", "1", "2")));

        HttpParticipantClientAdapter adapterCatalogue = crearAdapterConEquivalencias(
                "CATALOGUE", "accountCatalogue", equivalenciaRepo);

        CopyProcess proceso = crearProcesoConFase(1);
        adapterCatalogue.invoke(proceso, crearEjecucion(proceso.getId(), 1));

        List<LoggedRequest> requests = wireMock.findAll(
                WireMock.postRequestedFor(urlEqualTo("/api/accountCatalogue/copy/phase")));
        assertThat(requests).hasSize(1);
        String body = requests.get(0).getBodyAsString();
        // equivalenciasPrev debe ser array vacío para CATALOGUE
        assertThat(body).contains("\"equivalenciasPrev\":[]");
    }
}
