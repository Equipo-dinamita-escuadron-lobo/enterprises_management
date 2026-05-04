package com.enterprises_management.copy.infraestructure.adapters.output.persistence;

import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ModuleExecutionState;
import com.enterprises_management.copy.domain.models.CopyModuleExecution;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.adapter.CopyModuleExecutionJpaAdapter;
import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.adapter.CopyPhaseJpaAdapter;
import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.adapter.CopyProcessJpaAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integración JDBC para CopyModuleExecutionJpaAdapter.
 * Verifica constraint UNIQUE (id_proceso, id_fase, modulo) — REQ-IDEM-01.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
class CopyModuleExecutionJpaAdapterTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private CopyProcessJpaAdapter procesoAdapter;
    private CopyPhaseJpaAdapter faseAdapter;
    private CopyModuleExecutionJpaAdapter moduloAdapter;

    private String idProceso;
    private String idFase;

    @BeforeEach
    void setUp() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema-copy.sql"));
        }
        procesoAdapter = new CopyProcessJpaAdapter(jdbcTemplate);
        faseAdapter = new CopyPhaseJpaAdapter(jdbcTemplate);
        moduloAdapter = new CopyModuleExecutionJpaAdapter(jdbcTemplate);

        // Crear proceso y fase padre
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "user-test");
        procesoAdapter.guardar(proceso);
        idProceso = proceso.getId();

        CopyPhase fase = CopyPhase.crear(idProceso, 1, "BASE");
        faseAdapter.guardar(fase);
        idFase = fase.getId();
    }

    @Test
    @DisplayName("guardar persiste ejecución de módulo en BD")
    void guardar_debePersistirEjecucion() {
        CopyModuleExecution ejecucion = CopyModuleExecution.crear(idProceso, idFase, "CATALOGUE");

        CopyModuleExecution resultado = moduloAdapter.guardar(ejecucion);

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getModulo()).isEqualTo("CATALOGUE");
        assertThat(resultado.getEstado()).isEqualTo(ModuleExecutionState.PENDIENTE);
    }

    @Test
    @DisplayName("buscarPorFase retorna ejecuciones de la fase dada")
    void buscarPorFase_retornaEjecucionesDeLaFase() {
        moduloAdapter.guardar(CopyModuleExecution.crear(idProceso, idFase, "CATALOGUE"));
        moduloAdapter.guardar(CopyModuleExecution.crear(idProceso, idFase, "PRODUCTS"));

        List<CopyModuleExecution> resultado = moduloAdapter.buscarPorFase(idFase);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(CopyModuleExecution::getModulo)
                .containsExactlyInAnyOrder("CATALOGUE", "PRODUCTS");
    }

    @Test
    @DisplayName("insertar duplicado (id_proceso, id_fase, modulo) lanza DataIntegrityViolationException — REQ-IDEM-01")
    void guardar_duplicado_lanzaDataIntegrityViolationException() {
        moduloAdapter.guardar(CopyModuleExecution.crear(idProceso, idFase, "ENTERPRISES"));

        CopyModuleExecution duplicado = CopyModuleExecution.crear(idProceso, idFase, "ENTERPRISES");

        assertThatThrownBy(() -> moduloAdapter.guardar(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("buscarPorClaveIdempotencia retorna la ejecución existente")
    void buscarPorClaveIdempotencia_cuandoExiste_retornaOptional() {
        CopyModuleExecution ejecucion = CopyModuleExecution.crear(idProceso, idFase, "PRODUCTS");
        moduloAdapter.guardar(ejecucion);

        Optional<CopyModuleExecution> resultado = moduloAdapter.buscarPorClaveIdempotencia(idProceso, idFase, "PRODUCTS");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getModulo()).isEqualTo("PRODUCTS");
    }
}
