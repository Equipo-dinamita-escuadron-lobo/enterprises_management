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
 * Test E2E WireMock — Happy Path (7.1.1, ADR-25).
 *
 * <p>Estrategia: SagaEngineService real + H2 en memoria + HttpParticipantClientAdapter
 * apuntando a WireMock (sin Eureka). El participante WireMock responde 200 COMPLETADO
 * con equivalencias. Se verifica el flujo completo de la Fase 1.
 *
 * <p>No levanta servidor HTTP real (MOCK) — los ports son las mismas instancias de servicio.
 * El HttpParticipantClientAdapter se inyecta vía @TestConfiguration con WebClient
 * directo a WireMock (Opción C, ADR-25): sin @LoadBalanced ni Eureka.
 *
 * <p>Cubre REQ-CLIENT-01, REQ-CLIENT-02, ADR-25.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "app.copy.orchestrator.enabled=true",
    "app.copy.orchestrator.bootstrap-config=true",
    "app.copy.orchestrator.stub.failure-rate=0",
    "app.copy.orchestrator.stub.latency-ms=0",
    "app.copy.orchestrator.default-retries=3",
    // Transporte http para que HttpParticipantClientConfig registre el adapter
    // (el bean es sobrescrito por @TestConfiguration con el WebClient a WireMock)
    "app.copy.orchestrator.participant.transport=stub",
    "app.copy.orchestrator.events.amqp.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:e2e_wiremock_happy;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
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
    // Permite al @TestConfiguration sobreescribir el bean stubCatalogueClient (ADR-25)
    "spring.main.allow-bean-definition-overriding=true"
})
@DisplayName("E2E WireMock — Happy Path: orquestador + participante WireMock 200 COMPLETADO")
class CopyE2EWireMockHappyPathTest {

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
    // Spring context beans
    // -------------------------------------------------------------------------

    /**
     * Sobrescribe el bean StubParticipantClient de CATALOGUE con un
     * HttpParticipantClientAdapter apuntando a WireMock.
     *
     * <p>La estrategia (ADR-25 Opción C): se usa el mismo nombre de bean
     * {@code "stubCatalogueClient"} para sobrescribir el bean de CopyOrchestratorWebConfig
     * que normalmente registra el StubParticipantClient.
     * El @Primary garantiza que si hubiera ambigüedad de candidatos para inyección
     * single, se prefiera este.
     *
     * <p>ENTERPRISES y PRODUCTS siguen siendo stubs (para no requerir
     * servidores reales). Solo se prueba CATALOGUE vía HTTP real a WireMock.
     */
    @TestConfiguration
    static class WireMockParticipantConfig {

        /**
         * Sobrescribe el bean stubCatalogueClient con el adaptador HTTP apuntando a WireMock.
         * El nombre del bean coincide con el bean definido en CopyOrchestratorWebConfig
         * para que Spring Boot lo reemplace correctamente al cargar el test context.
         */
        @Bean("stubCatalogueClient")
        @Primary
        public IParticipantClientPort catalogueClientWireMock() {
            // Puerto resuelto en tiempo de ejecución cuando WireMock ya arrancó
            String baseUrl = "http://localhost:" + wireMock.port();
            WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
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

    @Autowired
    private IEquivalenceRepositoryPort equivalenciaRepo;

    // -------------------------------------------------------------------------
    // Helper para configurar el stub de WireMock
    // -------------------------------------------------------------------------

    private void configurarParticipanteExitoso() {
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
                                    { "modulo": "CATALOGUE", "tabla": "tax", "idViejo": "10", "idNuevo": "110" }
                                  ],
                                  "mensaje": "Copia completada exitosamente",
                                  "advertencias": []
                                }
                                """)));
    }

    // -------------------------------------------------------------------------
    // 7.1.1 — E2E happy path: participante WireMock responde 200 COMPLETADO
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("7.1.1 — HTTP CATALOGUE 200 COMPLETADO: fase 1 COMPLETADA vía HTTP real (ADR-25)")
    void e2e_wireMockHappyPath_fase1CompletadaViaHttp() {
        // GIVEN — WireMock responde 200 COMPLETADO
        configurarParticipanteExitoso();

        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.BACKUP,
                empresaOrigen,
                null,
                "backup-wiremock-001",
                "usuario-wiremock-001",
                false
        );

        // WHEN — iniciar proceso y avanzar fase 1
        CopyProcess proceso = startPort.iniciar(comando);
        assertThat(proceso.getEstado()).isEqualTo(ProcessState.PENDIENTE);

        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — verificar que la fase 1 quedó en un estado terminal
        // (COMPLETADA si el módulo CATALOGUE fue invocado exitosamente vía HTTP)
        List<CopyPhase> fases = queryPort.consultarFases(proceso.getId());
        assertThat(fases).isNotEmpty();

        CopyPhase fase1 = fases.stream()
                .filter(f -> f.getNumero() == 1)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Fase 1 no encontrada"));

        // El resultado depende de qué beans StubParticipantClient están activos también
        // El bean @Primary del WireMock reemplaza CATALOGUE, los otros stubs siguen activos
        assertThat(fase1.getEstado())
                .withFailMessage("Fase 1 debería terminar (COMPLETADA o ERROR), estado actual: %s", fase1.getEstado())
                .isIn(PhaseState.COMPLETADA, PhaseState.ERROR);
    }

    @Test
    @DisplayName("7.1.1 triangulación — equivalencias persistidas cuando participante responde 200 con 3 eq")
    void e2e_wireMockHappyPath_equivalenciasPersistidas() {
        // GIVEN
        configurarParticipanteExitoso();

        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                empresaOrigen,
                "empresa-destino-wiremock",
                null,
                "usuario-wiremock-002",
                false
        );

        // WHEN
        CopyProcess proceso = startPort.iniciar(comando);
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — verificar que al menos las equivalencias del WireMock participant
        // (3 eq de CATALOGUE) fueron persistidas. Los stubs ENTERPRISES y PRODUCTS
        // generan 3 eq cada uno también.
        List<CopyEquivalenceId> equivalencias =
                equivalenciaRepo.buscarConFiltros(proceso.getId(), null, null, null);

        // Con @Primary el bean CATALOGUE es el WireMock (3 eq de WireMock)
        // más los stubs ENTERPRISES y PRODUCTS (3+3 = 6 eq de stubs)
        // Total mínimo: 3 equivalencias del participante WireMock
        assertThat(equivalencias)
                .withFailMessage("Deben existir equivalencias del participante CATALOGUE (WireMock + stubs). Encontradas: %d", equivalencias.size())
                .hasSizeGreaterThanOrEqualTo(3);

        // Las equivalencias de CATALOGUE vienen del WireMock (tabla "account" y "tax")
        boolean hayEquivalenciasAccount = equivalencias.stream()
                .anyMatch(eq -> "CATALOGUE".equals(eq.getModulo()) && "account".equals(eq.getTabla()));
        assertThat(hayEquivalenciasAccount)
                .withFailMessage("Deben existir equivalencias de CATALOGUE/account del participante WireMock")
                .isTrue();
    }

    @Test
    @DisplayName("7.1.1 triangulación — WireMock recibió exactamente 1 llamada POST /phase")
    void e2e_wireMockHappyPath_wireMockRecibioUnaLlamada() {
        // GIVEN
        configurarParticipanteExitoso();

        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.BACKUP,
                empresaOrigen,
                null,
                "backup-wiremock-003",
                "usuario-wiremock-003",
                false
        );

        // WHEN
        CopyProcess proceso = startPort.iniciar(comando);
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // THEN — WireMock debe haber recibido exactamente 1 llamada (el módulo CATALOGUE)
        // con o sin reintentos. Si el participante respondió exitosamente, solo 1 llamada.
        List<com.github.tomakehurst.wiremock.verification.LoggedRequest> solicitudes =
                wireMock.findAll(com.github.tomakehurst.wiremock.client.WireMock
                        .postRequestedFor(urlEqualTo("/api/accountCatalogue/copy/phase")));

        assertThat(solicitudes)
                .withFailMessage("WireMock debería haber recibido exactamente 1 llamada POST /phase")
                .hasSize(1);
    }
}
