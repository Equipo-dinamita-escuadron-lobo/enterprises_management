package com.enterprises_management.copy.infraestructure.adapters.output.persistence;

import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.adapter.CopyProcessJpaAdapter;
import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.entity.CopyProcessEntity;
import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.repository.CopyProcessJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración JDBC para CopyProcessJpaAdapter.
 * Usa @JdbcTest + ScriptUtils para evitar conflicto con multi-tenancy (igual que CopySchemaBootstrapTest).
 * Verifica: guardar, buscar por ID, existeProcesoActivoPara con estados PENDIENTE/EN_PROCESO y COMPLETADO.
 * ADR-4: la verificación de proceso activo es aplicativa (sin índice único parcial en H2).
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
class CopyProcessJpaAdapterTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private CopyProcessJpaAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        // DDL fuera de transacción para H2
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema-copy.sql"));
        }
        adapter = new CopyProcessJpaAdapter(jdbcTemplate);
    }

    @Test
    @DisplayName("guardar persiste proceso y retorna dominio con ID asignado")
    void guardar_debePersisteYRetornarDominio() {
        CopyProcess proceso = CopyProcess.crear(
                CopyProcessType.DUPLICATE,
                UUID.randomUUID(),
                "EmpresaDestino",
                null,
                "user-sub-123"
        );

        CopyProcess resultado = adapter.guardar(proceso);

        assertThat(resultado.getId()).isEqualTo(proceso.getId());
        assertThat(resultado.getEstado()).isEqualTo(ProcessState.PENDIENTE);
        assertThat(resultado.getIniciadoPor()).isEqualTo("user-sub-123");
    }

    @Test
    @DisplayName("buscarPorId retorna proceso cuando existe")
    void buscarPorId_cuandoExiste_retornaOptionalConProceso() {
        CopyProcess proceso = CopyProcess.crear(
                CopyProcessType.BACKUP,
                UUID.randomUUID(),
                null,
                null,
                "user-abc"
        );
        adapter.guardar(proceso);

        Optional<CopyProcess> resultado = adapter.buscarPorId(proceso.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getTipo()).isEqualTo(CopyProcessType.BACKUP);
    }

    @Test
    @DisplayName("buscarPorId retorna vacío cuando no existe")
    void buscarPorId_cuandoNoExiste_retornaEmpty() {
        Optional<CopyProcess> resultado = adapter.buscarPorId(UUID.randomUUID().toString());

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("existeProcesoActivoPara retorna true cuando hay proceso PENDIENTE para empresa origen")
    void existeProcesoActivoPara_conProcesoPendiente_retornaTrue() {
        UUID empresaOrigen = UUID.randomUUID();
        CopyProcess proceso = CopyProcess.crear(
                CopyProcessType.DUPLICATE,
                empresaOrigen,
                "Destino",
                null,
                "user-xyz"
        );
        adapter.guardar(proceso);

        boolean resultado = adapter.existeProcesoActivoPara(empresaOrigen);

        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("existeProcesoActivoPara retorna false cuando solo hay procesos COMPLETADO")
    void existeProcesoActivoPara_soloCompletados_retornaFalse() {
        UUID empresaOrigen = UUID.randomUUID();
        // Insertar proceso COMPLETADO directamente en BD
        jdbcTemplate.update(
            "INSERT INTO copy_process (id, tipo, estado, empresa_origen, iniciado_por, snapshot_corte, version_proceso) " +
            "VALUES (?, 'DUPLICATE', 'COMPLETADO', ?, 'user-test', CURRENT_TIMESTAMP, 0)",
            UUID.randomUUID().toString(), empresaOrigen.toString()
        );

        boolean resultado = adapter.existeProcesoActivoPara(empresaOrigen);

        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("actualizar persiste cambio de estado en BD")
    void actualizar_debeActualizarEstadoEnBd() {
        CopyProcess proceso = CopyProcess.crear(
                CopyProcessType.DUPLICATE,
                UUID.randomUUID(),
                "Destino",
                null,
                "user-upd"
        );
        adapter.guardar(proceso);

        proceso.setEstado(ProcessState.EN_PROCESO);
        adapter.actualizar(proceso);

        Optional<CopyProcess> resultado = adapter.buscarPorId(proceso.getId());
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEstado()).isEqualTo(ProcessState.EN_PROCESO);
    }
}
