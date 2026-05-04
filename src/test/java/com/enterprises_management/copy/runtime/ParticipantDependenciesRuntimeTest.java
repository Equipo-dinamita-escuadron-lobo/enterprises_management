package com.enterprises_management.copy.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test de runtime: verifica que {@code PhaseConfigBootstrap} registra
 * exactamente los 9 participantes externos en {@code copy_phase_config}
 * cuando todos los feature flags están habilitados (REQ-PHASE3-01, REQ-PHASE3-02).
 *
 * <p>Distribución esperada:
 * <ul>
 *   <li>Fase 1: ENTERPRISES, CATALOGUE, PRODUCTS (3 entradas)</li>
 *   <li>Fase 2: PRODUCTS, THIRDS (2 entradas)</li>
 *   <li>Fase 3: TREASURY, STOCK, KARDEX, FACTURES, INVENTORYPEPS, AUXILIARY-BOOK (6 entradas)</li>
 * </ul>
 * Total: 11 filas en {@code copy_phase_config}.
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
    "spring.datasource.url=jdbc:h2:mem:runtime_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
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
@DisplayName("Runtime: PhaseConfigBootstrap registra todos los participantes (REQ-PHASE3-01)")
class ParticipantDependenciesRuntimeTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Bootstrap registra 11 entradas totales en copy_phase_config (3+2+6)")
    void bootstrap_registraOnceEntradas() {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM copy_phase_config WHERE activo = TRUE",
                Integer.class);
        assertThat(total)
                .withFailMessage("Se esperaban 11 entradas totales (F1:3 + F2:2 + F3:6), encontradas: %d", total)
                .isEqualTo(11);
    }

    @Test
    @DisplayName("Fase 1 tiene exactamente 3 módulos: ENTERPRISES, CATALOGUE, PRODUCTS")
    void fase1_tieneExactamenteTresModulos() {
        List<Map<String, Object>> fase1 = jdbcTemplate.queryForList(
                "SELECT modulo FROM copy_phase_config WHERE numero_fase = 1 AND activo = TRUE ORDER BY orden");
        assertThat(fase1).hasSize(3);
        assertThat(fase1.stream().map(r -> r.get("MODULO")).toList())
                .containsExactly("ENTERPRISES", "CATALOGUE", "PRODUCTS");
    }

    @Test
    @DisplayName("Fase 2 tiene exactamente 2 módulos: PRODUCTS, THIRDS")
    void fase2_tieneExactamenteDosModulos() {
        List<Map<String, Object>> fase2 = jdbcTemplate.queryForList(
                "SELECT modulo FROM copy_phase_config WHERE numero_fase = 2 AND activo = TRUE ORDER BY orden");
        assertThat(fase2).hasSize(2);
        assertThat(fase2.stream().map(r -> r.get("MODULO")).toList())
                .containsExactly("PRODUCTS", "THIRDS");
    }

    @Test
    @DisplayName("Fase 3 tiene exactamente 6 participantes transaccionales")
    void fase3_tieneExactamenteSeisParts() {
        List<Map<String, Object>> fase3 = jdbcTemplate.queryForList(
                "SELECT modulo FROM copy_phase_config WHERE numero_fase = 3 AND activo = TRUE ORDER BY orden");
        assertThat(fase3).hasSize(6);
        assertThat(fase3.stream().map(r -> r.get("MODULO")).toList())
                .containsExactly("TREASURY", "STOCK", "KARDEX", "FACTURES", "INVENTORYPEPS", "AUXILIARY-BOOK");
    }

    @Test
    @DisplayName("Todos los participantes Fase 3 tienen orden correlativo 1-6")
    void fase3_ordenCorrelativo() {
        List<Map<String, Object>> fase3 = jdbcTemplate.queryForList(
                "SELECT orden FROM copy_phase_config WHERE numero_fase = 3 AND activo = TRUE ORDER BY orden");
        assertThat(fase3.stream().map(r -> ((Number) r.get("ORDEN")).intValue()).toList())
                .containsExactly(1, 2, 3, 4, 5, 6);
    }
}
