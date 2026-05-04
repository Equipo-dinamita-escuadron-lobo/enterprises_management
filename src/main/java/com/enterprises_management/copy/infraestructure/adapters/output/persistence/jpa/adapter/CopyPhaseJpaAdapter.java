package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.adapter;

import com.enterprises_management.copy.application.output.ICopyPhaseRepositoryPort;
import com.enterprises_management.copy.domain.enums.PhaseState;
import com.enterprises_management.copy.domain.models.CopyPhase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC para el puerto de repositorio de CopyPhase.
 * Implementa ICopyPhaseRepositoryPort usando JdbcTemplate para compatibilidad con @JdbcTest.
 */
public class CopyPhaseJpaAdapter implements ICopyPhaseRepositoryPort {

    private final JdbcTemplate jdbc;

    public CopyPhaseJpaAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public CopyPhase guardar(CopyPhase fase) {
        jdbc.update(
            "INSERT INTO copy_phase (id, id_proceso, numero, nombre, estado, iniciada_en, finalizada_en, error_detalle) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            fase.getId(),
            fase.getIdProceso(),
            fase.getNumero(),
            fase.getNombre(),
            fase.getEstado().name(),
            fase.getIniciadaEn() != null ? Timestamp.valueOf(fase.getIniciadaEn()) : null,
            fase.getFinalizadaEn() != null ? Timestamp.valueOf(fase.getFinalizadaEn()) : null,
            fase.getErrorDetalle()
        );
        return fase;
    }

    @Override
    public CopyPhase actualizar(CopyPhase fase) {
        jdbc.update(
            "UPDATE copy_phase SET estado=?, iniciada_en=?, finalizada_en=?, error_detalle=? WHERE id=?",
            fase.getEstado().name(),
            fase.getIniciadaEn() != null ? Timestamp.valueOf(fase.getIniciadaEn()) : null,
            fase.getFinalizadaEn() != null ? Timestamp.valueOf(fase.getFinalizadaEn()) : null,
            fase.getErrorDetalle(),
            fase.getId()
        );
        return fase;
    }

    @Override
    public List<CopyPhase> buscarPorProceso(String idProceso) {
        return jdbc.query(
            "SELECT * FROM copy_phase WHERE id_proceso = ? ORDER BY numero ASC",
            COPY_PHASE_ROW_MAPPER,
            idProceso
        );
    }

    @Override
    public Optional<CopyPhase> buscarPorProcesoYNumero(String idProceso, int numeroFase) {
        List<CopyPhase> resultado = jdbc.query(
            "SELECT * FROM copy_phase WHERE id_proceso = ? AND numero = ?",
            COPY_PHASE_ROW_MAPPER,
            idProceso, numeroFase
        );
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    private static final RowMapper<CopyPhase> COPY_PHASE_ROW_MAPPER = (rs, rowNum) -> {
        // Usar constructor directo con id de la BD (el factory crea UUID nuevo que no corresponde)
        CopyPhase fase = new CopyPhase(
            rs.getString("id"),
            rs.getString("id_proceso"),
            rs.getInt("numero"),
            rs.getString("nombre")
        );
        fase.setEstado(PhaseState.valueOf(rs.getString("estado")));
        Timestamp iniciadaEn = rs.getTimestamp("iniciada_en");
        if (iniciadaEn != null) fase.setIniciadaEn(iniciadaEn.toLocalDateTime());
        Timestamp finalizadaEn = rs.getTimestamp("finalizada_en");
        if (finalizadaEn != null) fase.setFinalizadaEn(finalizadaEn.toLocalDateTime());
        fase.setErrorDetalle(rs.getString("error_detalle"));
        return fase;
    };
}
