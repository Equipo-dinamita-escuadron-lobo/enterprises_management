package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.adapter;

import com.enterprises_management.copy.application.output.IPhaseConfigRepositoryPort;
import com.enterprises_management.copy.domain.models.CopyPhaseConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

/**
 * Adaptador JDBC para configuración de fases.
 * Retorna módulos activos por fase ordenados por campo orden (REQ-CFG-01).
 */
public class CopyPhaseConfigJpaAdapter implements IPhaseConfigRepositoryPort {

    private final JdbcTemplate jdbc;

    public CopyPhaseConfigJpaAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<CopyPhaseConfig> buscarActivosPorFase(int numeroFase) {
        return jdbc.query(
            "SELECT * FROM copy_phase_config WHERE numero_fase = ? AND activo = true ORDER BY orden ASC",
            COPY_PHASE_CONFIG_ROW_MAPPER,
            numeroFase
        );
    }

    private static final RowMapper<CopyPhaseConfig> COPY_PHASE_CONFIG_ROW_MAPPER = (rs, rowNum) ->
        new CopyPhaseConfig(
            rs.getString("id"),
            rs.getInt("numero_fase"),
            rs.getString("modulo"),
            rs.getInt("orden"),
            rs.getBoolean("activo"),
            rs.getString("parametros_json")
        );
}
