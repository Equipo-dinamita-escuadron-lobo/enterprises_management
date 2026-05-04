package com.enterprises_management.copy.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración @SpringBootTest con flag OFF (REQ-FLAG-01, ADR-14).
 *
 * <p>Verifica que con app.copy.orchestrator.enabled=false:
 * <ul>
 *   <li>El bean CopyProcessStartService NO está registrado en el contexto</li>
 *   <li>PhaseConfigBootstrap NO ejecuta (ninguna fila en copy_phase_config)</li>
 * </ul>
 *
 * <p>Nota: La verificación de 404 HTTP ya está cubierta en FeatureFlagOffTest (@WebMvcTest).
 * Este test complementario verifica el comportamiento del contexto de Spring.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    // Flag desactivado
    "app.copy.orchestrator.enabled=false",
    "app.copy.orchestrator.bootstrap-config=false",
    // H2 propia para este test
    "spring.datasource.url=jdbc:h2:mem:e2e_flag_off;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.data-locations=",
    "jwt.auth.converter.principle-attribute=preferred_username",
    "jwt.auth.converter.resource-id=microservices_client",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
@DisplayName("E2E — Feature flag OFF: beans del orquestador no registrados (ADR-14, REQ-FLAG-01)")
class CopyFeatureFlagOffIntegrationTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // -------------------------------------------------------------------------
    // REQ-FLAG-01 — Beans no registrados cuando flag es false
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Con flag OFF: CopyProcessStartService no está registrado en el contexto (ADR-14)")
    void flagOff_copyProcessStartServiceNoRegistrado() {
        assertThat(applicationContext.containsBean("copyProcessStartService"))
                .withFailMessage("Con flag=false, copyProcessStartService NO debe estar en el contexto")
                .isFalse();
    }

    @Test
    @DisplayName("Con flag OFF: SagaEngineService no está registrado en el contexto (ADR-14)")
    void flagOff_sagaEngineServiceNoRegistrado() {
        assertThat(applicationContext.containsBean("sagaEngineService"))
                .withFailMessage("Con flag=false, sagaEngineService NO debe estar en el contexto")
                .isFalse();
    }

    @Test
    @DisplayName("Con flag OFF y bootstrap-config=false: copy_phase_config no tiene filas (ADR-14)")
    void flagOff_bootstrapConfigNoEjecutado_sinFilasEnPhaseConfig() {
        // PhaseConfigBootstrap solo corre cuando bootstrap-config=true
        // Con bootstrap-config=false, la tabla debe estar vacía
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM copy_phase_config",
                Integer.class
        );
        assertThat(count)
                .withFailMessage("Con bootstrap-config=false, copy_phase_config debe estar vacía")
                .isZero();
    }
}
