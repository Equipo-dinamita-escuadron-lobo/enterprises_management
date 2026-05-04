package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.adapter;

import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyProcess;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA/JDBC para el puerto de repositorio de CopyProcess.
 * Implementa ICopyProcessRepositoryPort usando JdbcTemplate.
 * Usa JdbcTemplate directamente para compatibilidad con @JdbcTest (sin multi-tenancy).
 * ADR-4: existeProcesoActivoPara() verifica estados PENDIENTE/EN_PROCESO aplicativamente (H2 no tiene índice único parcial).
 */
public class CopyProcessJpaAdapter implements ICopyProcessRepositoryPort {

    private final JdbcTemplate jdbc;

    public CopyProcessJpaAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public CopyProcess guardar(CopyProcess proceso) {
        jdbc.update(
            "INSERT INTO copy_process (id, tipo, estado, empresa_origen, empresa_destino, backup_ref, " +
            "snapshot_corte, iniciado_por, fase_actual, finalizado_en, error_resumen, version_proceso) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            proceso.getId(),
            proceso.getTipo().name(),
            proceso.getEstado().name(),
            proceso.getEmpresaOrigen().toString(),
            proceso.getEmpresaDestino(),
            proceso.getBackupRef(),
            Timestamp.valueOf(proceso.getSnapshotCorte()),
            proceso.getIniciadoPor(),
            proceso.getFaseActual(),
            proceso.getFinalizadoEn() != null ? Timestamp.valueOf(proceso.getFinalizadoEn()) : null,
            proceso.getErrorResumen(),
            proceso.getVersionProceso()
        );
        return proceso;
    }

    @Override
    public CopyProcess actualizar(CopyProcess proceso) {
        jdbc.update(
            "UPDATE copy_process SET tipo=?, estado=?, empresa_origen=?, empresa_destino=?, backup_ref=?, " +
            "snapshot_corte=?, iniciado_por=?, fase_actual=?, finalizado_en=?, error_resumen=?, " +
            "version_proceso=version_proceso+1 WHERE id=?",
            proceso.getTipo().name(),
            proceso.getEstado().name(),
            proceso.getEmpresaOrigen().toString(),
            proceso.getEmpresaDestino(),
            proceso.getBackupRef(),
            Timestamp.valueOf(proceso.getSnapshotCorte()),
            proceso.getIniciadoPor(),
            proceso.getFaseActual(),
            proceso.getFinalizadoEn() != null ? Timestamp.valueOf(proceso.getFinalizadoEn()) : null,
            proceso.getErrorResumen(),
            proceso.getId()
        );
        return proceso;
    }

    @Override
    public Optional<CopyProcess> buscarPorId(String idProceso) {
        List<CopyProcess> resultado = jdbc.query(
            "SELECT * FROM copy_process WHERE id = ?",
            COPY_PROCESS_ROW_MAPPER,
            idProceso
        );
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    @Override
    public List<CopyProcess> listarTodos() {
        return jdbc.query(
            "SELECT * FROM copy_process ORDER BY snapshot_corte DESC",
            COPY_PROCESS_ROW_MAPPER
        );
    }

    @Override
    public boolean existeProcesoActivoPara(UUID empresaOrigen) {
        // ADR-4: en H2 no hay índice único parcial. Verificación aplicativa: consultar estados activos.
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM copy_process WHERE empresa_origen = ? AND estado IN ('PENDIENTE', 'EN_PROCESO')",
            Integer.class,
            empresaOrigen.toString()
        );
        return count != null && count > 0;
    }

    private static final RowMapper<CopyProcess> COPY_PROCESS_ROW_MAPPER = (rs, rowNum) -> {
        Timestamp snapshotCorte = rs.getTimestamp("snapshot_corte");
        // Reconstituir proceso desde persistencia usando el ID de la BD (no generar nuevo UUID)
        CopyProcess proceso = CopyProcess.restaurar(
            rs.getString("id"),
            CopyProcessType.valueOf(rs.getString("tipo")),
            UUID.fromString(rs.getString("empresa_origen")),
            rs.getString("empresa_destino"),
            rs.getString("backup_ref"),
            snapshotCorte != null ? snapshotCorte.toLocalDateTime() : null,
            rs.getString("iniciado_por")
        );
        proceso.setEstado(ProcessState.valueOf(rs.getString("estado")));
        proceso.setFaseActual(rs.getInt("fase_actual"));
        Timestamp finalizadoEn = rs.getTimestamp("finalizado_en");
        if (finalizadoEn != null) {
            proceso.setFinalizadoEn(finalizadoEn.toLocalDateTime());
        }
        proceso.setErrorResumen(rs.getString("error_resumen"));
        proceso.setVersionProceso(rs.getLong("version_proceso"));
        return proceso;
    };
}
