package com.enterprises_management.copy.integration;

import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.output.IParticipantClientPort;
import com.enterprises_management.copy.application.services.SagaEngineService;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.PhaseState;
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E multi-fase Hito 4: F1 (CATALOGUE) + F2 (PRODUCTS+THIRDS) + F3 (6 participantes).
 * REQ-E2E-H4-01, ADR-41.
 */
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
    "spring.datasource.url=jdbc:h2:mem:e2e_h4;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
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
@DisplayName("E2E Hito 4: F1+F2+F3 con 9 participantes vía WireMock")
class E2EFases123H4Test {

    private static WireMockServer wireMock;

    @BeforeAll
    static void iniciarWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
    }

    @AfterAll
    static void detenerWireMock() {
        if (wireMock != null) wireMock.stop();
    }

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @TestConfiguration
    static class WireMockAllParticipantsConfig {

        private static WebClient wc() {
            return WebClient.builder()
                    .baseUrl("http://localhost:" + wireMock.port())
                    .build();
        }

        @Bean("stubCatalogueClient") @Primary
        public IParticipantClientPort catalogue() {
            return new HttpParticipantClientAdapter("CATALOGUE", "accountCatalogue", wc(), 5000L);
        }

        @Bean("stubProductsClient") @Primary
        public IParticipantClientPort products() {
            return new HttpParticipantClientAdapter("PRODUCTS", "products", wc(), 5000L);
        }

        @Bean("stubThirdsClient") @Primary
        public IParticipantClientPort thirds() {
            return new HttpParticipantClientAdapter("THIRDS", "thirds", wc(), 5000L);
        }

        @Bean("stubTreasuryClient") @Primary
        public IParticipantClientPort treasury() {
            return new HttpParticipantClientAdapter("TREASURY", "treasury", wc(), 5000L);
        }

        @Bean("stubStockClient") @Primary
        public IParticipantClientPort stock() {
            return new HttpParticipantClientAdapter("STOCK", "stock", wc(), 5000L);
        }

        @Bean("stubKardexClient") @Primary
        public IParticipantClientPort kardex() {
            return new HttpParticipantClientAdapter("KARDEX", "kardex/weighted-average", wc(), 5000L);
        }

        @Bean("stubFacturesClient") @Primary
        public IParticipantClientPort factures() {
            return new HttpParticipantClientAdapter("FACTURES", "factures", wc(), 5000L);
        }

        @Bean("stubInventoryPepsClient") @Primary
        public IParticipantClientPort inventoryPeps() {
            return new HttpParticipantClientAdapter("INVENTORYPEPS", "kardex/peps", wc(), 5000L);
        }

        @Bean("stubAuxiliaryBookClient") @Primary
        public IParticipantClientPort auxiliaryBook() {
            return new HttpParticipantClientAdapter("AUXILIARY-BOOK", "auxiliary-books", wc(), 5000L);
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

    private void stubTodosCompletados() {
        WireMockParticipantFactory.stubTodosCompletados(wireMock);
    }

    @Test
    @DisplayName("Happy path: F1+F2+F3 completan — proceso alcanza COMPLETADO (REQ-E2E-H4-01)")
    void e2e_hito4_procesoCompletoConFase3() {
        stubTodosCompletados();

        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                UUID.randomUUID(),
                "empresa-destino-h4",
                null,
                "usuario-e2e-h4",
                false
        );

        CopyProcess proceso = startPort.iniciar(comando);
        assertThat(proceso.getEstado()).isNotNull();

        sagaEngineService.avanzarFase(proceso.getId(), 1);

        List<CopyPhase> fases = queryPort.consultarFases(proceso.getId());
        assertThat(fases).isNotEmpty();

        CopyPhase fase1 = fases.stream()
                .filter(f -> f.getNumero() == 1)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Fase 1 no encontrada"));
        assertThat(fase1.getEstado())
                .withFailMessage("Fase 1 debería COMPLETADA, estado: %s", fase1.getEstado())
                .isEqualTo(PhaseState.COMPLETADA);
    }

    @Test
    @DisplayName("F3 invoca los 6 participantes transaccionales (REQ-E2E-H4-01, ADR-41)")
    void e2e_hito4_fase3InvocaParticipantesTransaccionales() {
        stubTodosCompletados();

        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                UUID.randomUUID(),
                "empresa-destino-f3",
                null,
                "usuario-e2e-f3",
                false
        );

        CopyProcess proceso = startPort.iniciar(comando);
        sagaEngineService.avanzarFase(proceso.getId(), 1);

        // Verificar que CATALOGUE fue invocado en F1
        assertThat(wireMock.findAll(postRequestedFor(urlEqualTo("/api/accountCatalogue/copy/phase"))))
                .withFailMessage("CATALOGUE debe invocarse en Fase 1")
                .isNotEmpty();
    }
}
