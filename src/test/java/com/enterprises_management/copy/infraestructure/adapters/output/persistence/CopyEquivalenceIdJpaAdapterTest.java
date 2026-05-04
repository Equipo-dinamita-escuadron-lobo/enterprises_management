package com.enterprises_management.copy.infraestructure.adapters.output.persistence;

import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.exceptions.EquivalenceConflictException;
import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.adapter.CopyEquivalenceIdJpaAdapter;
import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.adapter.CopyProcessJpaAdapter;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integración JDBC para CopyEquivalenceIdJpaAdapter.
 * Verifica: upsert idempotente (mismo idNuevo ok, distinto idNuevo → 409),
 * y consulta con filtros opcionales — ADR-9, REQ-EQ-01, REQ-EQ-02.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
class CopyEquivalenceIdJpaAdapterTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private CopyProcessJpaAdapter procesoAdapter;
    private CopyEquivalenceIdJpaAdapter equivalenceAdapter;

    private String idProceso;

    @BeforeEach
    void setUp() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema-copy.sql"));
        }
        procesoAdapter = new CopyProcessJpaAdapter(jdbcTemplate);
        equivalenceAdapter = new CopyEquivalenceIdJpaAdapter(jdbcTemplate);

        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "user-eq");
        procesoAdapter.guardar(proceso);
        idProceso = proceso.getId();
    }

    @Test
    @DisplayName("guardar persiste equivalencia nueva correctamente")
    void guardar_debePersistirEquivalencia() {
        CopyEquivalenceId equivalencia = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "1042");

        CopyEquivalenceId resultado = equivalenceAdapter.guardar(equivalencia);

        assertThat(resultado.getIdViejo()).isEqualTo("42");
        assertThat(resultado.getIdNuevo()).isEqualTo("1042");
    }

    @Test
    @DisplayName("upsert con mismo idNuevo es idempotente — no duplica — ADR-9")
    void guardar_duplicadoMismoIdNuevo_esIdempotente() {
        CopyEquivalenceId primera = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "1042");
        equivalenceAdapter.guardar(primera);

        // Segunda llamada con mismo idViejo y mismo idNuevo
        CopyEquivalenceId segunda = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "1042");
        CopyEquivalenceId resultado = equivalenceAdapter.guardarOActualizar(segunda);

        assertThat(resultado.getIdNuevo()).isEqualTo("1042");

        List<CopyEquivalenceId> lista = equivalenceAdapter.buscarConFiltros(idProceso, "PRODUCTS", "producto", "42");
        assertThat(lista).hasSize(1);
    }

    @Test
    @DisplayName("upsert con distinto idNuevo lanza EquivalenceConflictException — ADR-9")
    void guardar_duplicadoDistintoIdNuevo_lanzaEquivalenceConflictException() {
        CopyEquivalenceId primera = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "1042");
        equivalenceAdapter.guardar(primera);

        CopyEquivalenceId conflicto = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "9999");

        assertThatThrownBy(() -> equivalenceAdapter.guardarOActualizar(conflicto))
                .isInstanceOf(EquivalenceConflictException.class);
    }

    @Test
    @DisplayName("buscarConFiltros con filtros parciales retorna solo los que coinciden")
    void buscarConFiltros_conFiltrosParciales_retornaSoloCoincidentes() {
        equivalenceAdapter.guardar(CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "1", "1001"));
        equivalenceAdapter.guardar(CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "2", "1002"));
        equivalenceAdapter.guardar(CopyEquivalenceId.crear(idProceso, "CATALOGUE", "cuenta", "10", "1010"));

        List<CopyEquivalenceId> resultado = equivalenceAdapter.buscarConFiltros(idProceso, "PRODUCTS", null, null);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(CopyEquivalenceId::getModulo)
                .containsOnly("PRODUCTS");
    }

    @Test
    @DisplayName("buscarConFiltros sin filtros opcionales retorna todas las equivalencias del proceso")
    void buscarConFiltros_sinFiltros_retornaTodasLasEquivalenciasDelProceso() {
        equivalenceAdapter.guardar(CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "1", "1001"));
        equivalenceAdapter.guardar(CopyEquivalenceId.crear(idProceso, "CATALOGUE", "cuenta", "10", "1010"));

        List<CopyEquivalenceId> resultado = equivalenceAdapter.buscarConFiltros(idProceso, null, null, null);

        assertThat(resultado).hasSize(2);
    }
}
