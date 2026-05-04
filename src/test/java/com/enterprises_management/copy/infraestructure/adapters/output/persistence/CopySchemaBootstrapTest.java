package com.enterprises_management.copy.infraestructure.adapters.output.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que las 6 tablas del bounded context copy existen en H2.
 * Ejecuta schema-copy.sql manualmente con ScriptUtils para evitar problemas
 * de transacciones con @Sql y el modo no-transaccional de DDL en H2.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
class CopySchemaBootstrapTest {

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void ejecutarSchema() throws Exception {
        // Ejecutar DDL fuera de la transacción de test — H2 DDL es auto-commit implícito
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema-copy.sql"));
        }
    }

    @Test
    @Rollback(false)
    @DisplayName("Las 6 tablas copy_* deben existir en H2 tras ejecutar schema-copy.sql")
    void tablasCopyDebenExistir() throws Exception {
        Set<String> tablas = obtenerTablas();

        assertThat(tablas)
            .withFailMessage("Tablas encontradas: %s", tablas)
            .contains(
                "COPY_PROCESS",
                "COPY_PHASE",
                "COPY_MODULE_EXECUTION",
                "COPY_EQUIVALENCE_ID",
                "COPY_PHASE_CONFIG",
                "COPY_PROCESS_EVENT"
            );
    }

    private Set<String> obtenerTablas() throws Exception {
        Set<String> tablas = new HashSet<>();
        // H2 2.x usa TABLE_SCHEMA = 'PUBLIC' y TABLE_TYPE = 'TABLE' o 'BASE TABLE'
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT UPPER(TABLE_NAME) FROM INFORMATION_SCHEMA.TABLES"
             )) {
            while (rs.next()) {
                tablas.add(rs.getString(1));
            }
        }
        return tablas;
    }
}
