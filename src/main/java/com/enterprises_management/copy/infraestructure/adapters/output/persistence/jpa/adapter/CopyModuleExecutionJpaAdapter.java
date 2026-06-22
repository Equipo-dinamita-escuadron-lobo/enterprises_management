package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.adapter;

import com.enterprises_management.copy.application.output.IModuleExecutionRepositoryPort;
import com.enterprises_management.copy.domain.enums.ModuleExecutionState;
import com.enterprises_management.copy.domain.models.CopyModuleExecution;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC para el puerto de repositorio de CopyModuleExecution.
 * Verifica constraint UNIQUE (id_proceso, id_fase, modulo) — REQ-IDEM-01.
 */
public class CopyModuleExecutionJpaAdapter implements IModuleExecutionRepositoryPort {

    private final JdbcTemplate jdbc;

    public CopyModuleExecutionJpaAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public CopyModuleExecution guardar(CopyModuleExecution ejecucion) {
        jdbc.update(
            "INSERT INTO copy_module_execution (id, id_proceso, id_fase, modulo, estado, intentos, " +
            "iniciado_en, finalizado_en, error_detalle) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            ejecucion.getId(),
            ejecucion.getIdProceso(),
            ejecucion.getIdFase(),
            ejecucion.getModulo(),
            ejecucion.getEstado().name(),
            ejecucion.getIntentos(),
            ejecucion.getIniciadoEn() != null ? Timestamp.valueOf(ejecucion.getIniciadoEn()) : null,
            ejecucion.getFinalizadoEn() != null ? Timestamp.valueOf(ejecucion.getFinalizadoEn()) : null,
            ejecucion.getErrorDetalle()
        );
        return ejecucion;
    }

    @Override
    public CopyModuleExecution actualizar(CopyModuleExecution ejecucion) {
        jdbc.update(
            "UPDATE copy_module_execution SET estado=?, intentos=?, iniciado_en=?, finalizado_en=?, " +
            "error_detalle=? WHERE id=?",
            ejecucion.getEstado().name(),
            ejecucion.getIntentos(),
            ejecucion.getIniciadoEn() != null ? Timestamp.valueOf(ejecucion.getIniciadoEn()) : null,
            ejecucion.getFinalizadoEn() != null ? Timestamp.valueOf(ejecucion.getFinalizadoEn()) : null,
            ejecucion.getErrorDetalle(),
            ejecucion.getId()
        );
        return ejecucion;
    }

    @Override
    public List<CopyModuleExecution> buscarPorFase(String idFase) {
        return jdbc.query(
            "SELECT * FROM copy_module_execution WHERE id_fase = ?",
            COPY_MODULE_EXECUTION_ROW_MAPPER,
            idFase
        );
    }

    @Override
    public Optional<CopyModuleExecution> buscarPorClaveIdempotencia(String idProceso, String idFase, String modulo) {
        List<CopyModuleExecution> resultado = jdbc.query(
            "SELECT * FROM copy_module_execution WHERE id_proceso = ? AND id_fase = ? AND modulo = ?",
            COPY_MODULE_EXECUTION_ROW_MAPPER,
            idProceso, idFase, modulo
        );
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    private static final RowMapper<CopyModuleExecution> COPY_MODULE_EXECUTION_ROW_MAPPER = (rs, rowNum) -> {
        CopyModuleExecution ejecucion = new CopyModuleExecution(
            rs.getString("id"),
            rs.getString("id_proceso"),
            rs.getString("id_fase"),
            rs.getString("modulo")
        );
        ejecucion.setEstado(ModuleExecutionState.valueOf(rs.getString("estado")));
        ejecucion.setIntentos(rs.getInt("intentos"));
        Timestamp iniciadoEn = rs.getTimestamp("iniciado_en");
        if (iniciadoEn != null) ejecucion.setIniciadoEn(iniciadoEn.toLocalDateTime());
        Timestamp finalizadoEn = rs.getTimestamp("finalizado_en");
        if (finalizadoEn != null) ejecucion.setFinalizadoEn(finalizadoEn.toLocalDateTime());
        ejecucion.setErrorDetalle(rs.getString("error_detalle"));
        return ejecucion;
    };
}
