package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.adapter;

import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.domain.exceptions.EquivalenceConflictException;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC para el puerto de repositorio de equivalencias.
 * Implementa lógica de upsert idempotente (ADR-9):
 * - mismo (clave + idNuevo) → idempotente, sin error
 * - mismo (clave) pero distinto idNuevo → EquivalenceConflictException (409)
 */
public class CopyEquivalenceIdJpaAdapter implements IEquivalenceRepositoryPort {

    private final JdbcTemplate jdbc;

    public CopyEquivalenceIdJpaAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public CopyEquivalenceId guardar(CopyEquivalenceId equivalencia) {
        jdbc.update(
            "INSERT INTO copy_equivalence_id (id, id_proceso, modulo, tabla, id_viejo, id_nuevo, registrado_en) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)",
            equivalencia.getId(),
            equivalencia.getIdProceso(),
            equivalencia.getModulo(),
            equivalencia.getTabla(),
            equivalencia.getIdViejo(),
            equivalencia.getIdNuevo(),
            Timestamp.valueOf(equivalencia.getRegistradoEn())
        );
        return equivalencia;
    }

    @Override
    public CopyEquivalenceId actualizar(CopyEquivalenceId equivalencia) {
        jdbc.update(
            "UPDATE copy_equivalence_id SET id_nuevo=? WHERE id_proceso=? AND modulo=? AND tabla=? AND id_viejo=?",
            equivalencia.getIdNuevo(),
            equivalencia.getIdProceso(),
            equivalencia.getModulo(),
            equivalencia.getTabla(),
            equivalencia.getIdViejo()
        );
        return equivalencia;
    }

    @Override
    public Optional<CopyEquivalenceId> buscarPorClave(String idProceso, String modulo, String tabla, String idViejo) {
        List<CopyEquivalenceId> resultado = jdbc.query(
            "SELECT * FROM copy_equivalence_id WHERE id_proceso=? AND modulo=? AND tabla=? AND id_viejo=?",
            EQUIVALENCE_ROW_MAPPER,
            idProceso, modulo, tabla, idViejo
        );
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public List<CopyEquivalenceId> buscarConFiltros(String idProceso, String modulo, String tabla, String idViejo) {
        StringBuilder sql = new StringBuilder("SELECT * FROM copy_equivalence_id WHERE id_proceso=?");
        List<Object> params = new ArrayList<>();
        params.add(idProceso);

        if (modulo != null) {
            sql.append(" AND modulo=?");
            params.add(modulo);
        }
        if (tabla != null) {
            sql.append(" AND tabla=?");
            params.add(tabla);
        }
        if (idViejo != null) {
            sql.append(" AND id_viejo=?");
            params.add(idViejo);
        }

        return jdbc.query(sql.toString(), EQUIVALENCE_ROW_MAPPER, params.toArray());
    }

    /**
     * Upsert idempotente (ADR-9):
     * - Si no existe → inserta.
     * - Si existe con mismo idNuevo → retorna la existente (idempotente).
     * - Si existe con distinto idNuevo → lanza EquivalenceConflictException.
     *
     * @param equivalencia equivalencia a guardar o verificar
     * @return equivalencia guardada o existente
     * @throws EquivalenceConflictException si el idNuevo registrado difiere del solicitado
     */
    public CopyEquivalenceId guardarOActualizar(CopyEquivalenceId equivalencia) {
        Optional<CopyEquivalenceId> existente = buscarPorClave(
            equivalencia.getIdProceso(),
            equivalencia.getModulo(),
            equivalencia.getTabla(),
            equivalencia.getIdViejo()
        );

        if (existente.isEmpty()) {
            return guardar(equivalencia);
        }

        CopyEquivalenceId eq = existente.get();
        if (eq.getIdNuevo().equals(equivalencia.getIdNuevo())) {
            // Idempotente: mismo mapeo, no hacer nada
            return eq;
        }

        // Conflicto: distinto idNuevo para la misma clave (ADR-9)
        throw new EquivalenceConflictException(
            equivalencia.getIdProceso(),
            equivalencia.getModulo(),
            equivalencia.getTabla(),
            equivalencia.getIdViejo()
        );
    }

    private static final RowMapper<CopyEquivalenceId> EQUIVALENCE_ROW_MAPPER = (rs, rowNum) ->
        new CopyEquivalenceId(
            rs.getString("id"),
            rs.getString("id_proceso"),
            rs.getString("modulo"),
            rs.getString("tabla"),
            rs.getString("id_viejo"),
            rs.getString("id_nuevo"),
            rs.getTimestamp("registrado_en").toLocalDateTime()
        );
}
