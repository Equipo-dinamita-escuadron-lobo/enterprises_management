package com.enterprises_management.copy.infraestructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para PhaseConfigBootstrap — verifica que PRODUCTS(fase=2) y THIRDS(fase=2)
 * se insertan al arrancar con los feature flags habilitados (REQ-BOOTSTRAP-01; ADR-27).
 *
 * Estrategia:
 * - @JdbcTest provee H2 + JdbcTemplate sin contexto Spring completo.
 * - schema-copy.sql se ejecuta en @BeforeEach con ScriptUtils (mismo patrón que CopySchemaBootstrapTest).
 * - PhaseConfigBootstrap se crea manualmente con JdbcTemplate inyectado y se llama run() directamente.
 *   Esto evita que Spring intente ejecutarlo como ApplicationRunner antes de que existan las tablas.
 * - @Rollback(false) necesario porque ScriptUtils ejecuta DDL fuera de la transacción de test.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "app.copy.orchestrator.participants.enabled.products=true",
    "app.copy.orchestrator.participants.enabled.thirds=true"
})
class PhaseConfigBootstrapTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** Instancia creada manualmente para evitar que Spring la ejecute antes del schema DDL */
    private PhaseConfigBootstrap bootstrap;

    @BeforeEach
    void crearSchemaYBootstrap() throws Exception {
        // Crear tablas copy_* en H2 antes de invocar el bootstrap
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema-copy.sql"));
        }
        // Crear instancia con feature flags: products=true, thirds=true
        bootstrap = new PhaseConfigBootstrap(jdbcTemplate);
        bootstrap.setProductsHabilitado(true);
        bootstrap.setThirdsHabilitado(true);
    }

    @Test
    @Rollback(false)
    @DisplayName("Bootstrap inserta PRODUCTS(fase=2) en copy_phase_config (REQ-BOOTSTRAP-01)")
    void bootstrapDebeInsertarProductsFase2() throws Exception {
        bootstrap.run(null);

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM copy_phase_config WHERE modulo = 'PRODUCTS' AND numero_fase = 2",
            Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Rollback(false)
    @DisplayName("Bootstrap inserta THIRDS(fase=2) en copy_phase_config (REQ-BOOTSTRAP-01)")
    void bootstrapDebeInsertarThirdsFase2() throws Exception {
        bootstrap.run(null);

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM copy_phase_config WHERE modulo = 'THIRDS' AND numero_fase = 2",
            Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Rollback(false)
    @DisplayName("Bootstrap es idempotente — doble ejecución no duplica filas (REQ-BOOTSTRAP-01)")
    void bootstrapEsIdempotente() throws Exception {
        bootstrap.run(null);
        bootstrap.run(null);

        Integer countProducts = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM copy_phase_config WHERE modulo = 'PRODUCTS' AND numero_fase = 2",
            Integer.class
        );
        Integer countThirds = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM copy_phase_config WHERE modulo = 'THIRDS' AND numero_fase = 2",
            Integer.class
        );
        assertThat(countProducts).isEqualTo(1);
        assertThat(countThirds).isEqualTo(1);
    }

    @Test
    @Rollback(false)
    @DisplayName("Bootstrap mantiene las filas de Fase 1 originales sin eliminarlas")
    void bootstrapMantieneFase1() throws Exception {
        bootstrap.run(null);

        Integer countFase1 = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM copy_phase_config WHERE numero_fase = 1",
            Integer.class
        );
        // ENTERPRISES, CATALOGUE, PRODUCTS en Fase 1
        assertThat(countFase1).isEqualTo(3);
    }
}
