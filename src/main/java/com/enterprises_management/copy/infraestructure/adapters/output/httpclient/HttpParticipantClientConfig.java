package com.enterprises_management.copy.infraestructure.adapters.output.httpclient;

import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración de beans HTTP para el adaptador de participantes.
 *
 * <p>Solo se activa cuando AMBAS condiciones se cumplen (ADR-19):
 * <ol>
 *   <li>{@code app.copy.orchestrator.enabled=true} — feature flag principal activo</li>
 *   <li>{@code app.copy.orchestrator.participant.transport=http} — transporte HTTP seleccionado</li>
 * </ol>
 *
 * <p>El WebClient se construye con {@code lb://SERVICIO} para resolución via Eureka (ADR-17).
 * En tests de Fase 3 se usa un WebClient directo (sin @LoadBalanced) apuntando a WireMock (ADR-25).
 */
@Configuration
@Conditional(HttpParticipantClientConfig.OrchestratorEnabledAndHttpTransport.class)
public class HttpParticipantClientConfig {

    /**
     * Condición compuesta: orchestrator.enabled=true AND transport=http.
     * Se necesita AllNestedConditions porque @ConditionalOnProperty no es repeatable
     * en Spring Boot 3.4.x (se evaluaría solo la última si se repitiera).
     */
    static class OrchestratorEnabledAndHttpTransport extends AllNestedConditions {

        OrchestratorEnabledAndHttpTransport() {
            // ConfigurationPhase.REGISTER_BEAN — se evalúa al registrar beans (no al parsear @Configuration)
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        /** Condición 1: el orquestador debe estar habilitado. */
        @ConditionalOnProperty(name = "app.copy.orchestrator.enabled", havingValue = "true")
        static class OrchestradorHabilitado {}

        /** Condición 2: el transporte debe ser HTTP. */
        @ConditionalOnProperty(name = "app.copy.orchestrator.participant.transport", havingValue = "http")
        static class TransporteHttp {}
    }

    /** Timeout en ms para cada invocación HTTP al participante (REQ-CLIENT-04, ADR-26). */
    @Value("${app.copy.orchestrator.participant.timeout-ms:30000}")
    private long timeoutMs;

    /**
     * Adaptador HTTP para el servicio CATALOGUE (account-catalogue).
     *
     * <p>URI base: {@code lb://CATALOGUE} — Eureka resuelve el host al servicio registrado
     * con {@code spring.application.name=CATALOGUE} (REQ-CLIENT-02, ADR-17).
     */
    @Bean("httpCatalogueClient")
    public HttpParticipantClientAdapter httpCatalogueClient(
            WebClient.Builder loadBalancedWebClientBuilder,
            IEquivalenceRepositoryPort equivalenciaRepo,
            MeterRegistry meterRegistry
    ) {
        WebClient webClient = loadBalancedWebClientBuilder
                .baseUrl("lb://CATALOGUE")
                .build();
        return new HttpParticipantClientAdapter(
                "CATALOGUE",
                "accountCatalogue",
                webClient,
                timeoutMs,
                equivalenciaRepo,
                meterRegistry
        );
    }

    /**
     * Adaptador HTTP para el servicio PRODUCTS (products-management).
     *
     * <p>URI base: {@code lb://PRODUCTS} — Eureka resuelve el host al servicio registrado
     * con {@code spring.application.name=PRODUCTS} (REQ-PRODUCTS-01, REQ-CLIENT-02, ADR-17, ADR-32).
     * Habilitado junto con el resto de la config HTTP (ADR-19).
     */
    @Bean("httpProductsClient")
    public HttpParticipantClientAdapter httpProductsClient(
            WebClient.Builder loadBalancedWebClientBuilder,
            IEquivalenceRepositoryPort equivalenciaRepo,
            MeterRegistry meterRegistry
    ) {
        WebClient webClient = loadBalancedWebClientBuilder
                .baseUrl("lb://PRODUCTS")
                .build();
        return new HttpParticipantClientAdapter(
                "PRODUCTS",
                "products",
                webClient,
                timeoutMs,
                equivalenciaRepo,
                meterRegistry
        );
    }

    /**
     * Adaptador HTTP para el servicio THIRDS (thirds-management).
     *
     * <p>URI base: {@code lb://THIRDS} — Eureka resuelve el host al servicio registrado
     * con {@code spring.application.name=THIRDS} (REQ-THIRDS-04, REQ-CLIENT-02, ADR-17, ADR-32).
     * Habilitado junto con el resto de la config HTTP (ADR-19).
     */
    @Bean("httpThirdsClient")
    public HttpParticipantClientAdapter httpThirdsClient(
            WebClient.Builder loadBalancedWebClientBuilder,
            IEquivalenceRepositoryPort equivalenciaRepo,
            MeterRegistry meterRegistry
    ) {
        WebClient webClient = loadBalancedWebClientBuilder
                .baseUrl("lb://THIRDS")
                .build();
        return new HttpParticipantClientAdapter(
                "THIRDS",
                "thirds",
                webClient,
                timeoutMs,
                equivalenciaRepo,
                meterRegistry
        );
    }

    @Bean("httpTreasuryClient")
    public HttpParticipantClientAdapter httpTreasuryClient(
            WebClient.Builder loadBalancedWebClientBuilder,
            IEquivalenceRepositoryPort equivalenciaRepo,
            MeterRegistry meterRegistry
    ) {
        WebClient webClient = loadBalancedWebClientBuilder
                .baseUrl("lb://TREASURY")
                .build();
        return new HttpParticipantClientAdapter(
                "TREASURY",
                "treasury",
                webClient,
                timeoutMs,
                equivalenciaRepo,
                meterRegistry
        );
    }

    @Bean("httpStockClient")
    public HttpParticipantClientAdapter httpStockClient(
            WebClient.Builder loadBalancedWebClientBuilder,
            IEquivalenceRepositoryPort equivalenciaRepo,
            MeterRegistry meterRegistry
    ) {
        WebClient webClient = loadBalancedWebClientBuilder
                .baseUrl("lb://STOCK")
                .build();
        return new HttpParticipantClientAdapter(
                "STOCK",
                "stock",
                webClient,
                timeoutMs,
                equivalenciaRepo,
                meterRegistry
        );
    }

    @Bean("httpKardexWaClient")
    public HttpParticipantClientAdapter httpKardexWaClient(
            WebClient.Builder loadBalancedWebClientBuilder,
            IEquivalenceRepositoryPort equivalenciaRepo,
            MeterRegistry meterRegistry
    ) {
        WebClient webClient = loadBalancedWebClientBuilder
                .baseUrl("lb://KARDEX")
                .build();
        return new HttpParticipantClientAdapter(
                "KARDEX",
                "kardex/weighted-average",
                webClient,
                timeoutMs,
                equivalenciaRepo,
                meterRegistry
        );
    }

    @Bean("httpFacturesClient")
    public HttpParticipantClientAdapter httpFacturesClient(
            WebClient.Builder loadBalancedWebClientBuilder,
            IEquivalenceRepositoryPort equivalenciaRepo,
            MeterRegistry meterRegistry
    ) {
        WebClient webClient = loadBalancedWebClientBuilder
                .baseUrl("lb://FACTURES")
                .build();
        return new HttpParticipantClientAdapter(
                "FACTURES",
                "factures",
                webClient,
                timeoutMs,
                equivalenciaRepo,
                meterRegistry
        );
    }

    @Bean("httpInventoryPepsClient")
    public HttpParticipantClientAdapter httpInventoryPepsClient(
            WebClient.Builder loadBalancedWebClientBuilder,
            IEquivalenceRepositoryPort equivalenciaRepo,
            MeterRegistry meterRegistry
    ) {
        WebClient webClient = loadBalancedWebClientBuilder
                .baseUrl("lb://INVENTORYPEPS")
                .build();
        return new HttpParticipantClientAdapter(
                "INVENTORYPEPS",
                "kardex/peps",
                webClient,
                timeoutMs,
                equivalenciaRepo,
                meterRegistry
        );
    }

    @Bean("httpAuxiliaryBookClient")
    public HttpParticipantClientAdapter httpAuxiliaryBookClient(
            WebClient.Builder loadBalancedWebClientBuilder,
            IEquivalenceRepositoryPort equivalenciaRepo,
            MeterRegistry meterRegistry
    ) {
        WebClient webClient = loadBalancedWebClientBuilder
                .baseUrl("lb://AUXILIARY-BOOK")
                .build();
        return new HttpParticipantClientAdapter(
                "AUXILIARY-BOOK",
                "auxiliary-books",
                webClient,
                timeoutMs,
                equivalenciaRepo,
                meterRegistry
        );
    }
}
