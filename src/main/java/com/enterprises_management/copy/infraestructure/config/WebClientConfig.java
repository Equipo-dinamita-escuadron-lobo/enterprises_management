package com.enterprises_management.copy.infraestructure.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración del bean WebClient con @LoadBalanced para resolución via Eureka.
 *
 * <p>El bean {@code WebClient.Builder} decorado con @LoadBalanced permite usar
 * URIs del tipo {@code lb://SERVICIO/...} que Eureka resuelve dinámicamente
 * al nombre de instancia registrado (ADR-17, REQ-CLIENT-02).
 *
 * <p>Los adaptadores HTTP usan este builder inyectado y construyen instancias
 * por cliente con configuración de timeout adicional.
 */
@Configuration
public class WebClientConfig {

    /**
     * Builder de WebClient con @LoadBalanced habilitado para resolución Eureka.
     *
     * <p>Anotado con @LoadBalanced en lugar de la instancia final porque Spring Cloud
     * Load Balancer requiere decorar el builder, no el WebClient ya construido.
     *
     * @return builder configurado con load balancer
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
