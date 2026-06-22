package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.adapter;

import com.enterprises_management.copy.application.output.IProcessEventRepositoryPort;
import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

/**
 * Adaptador JDBC para log de eventos del proceso.
 * El ID BIGINT autoincremental lo asigna la BD (ADR-8: secuencia para deduplicación SSE).
 */
public class CopyProcessEventJpaAdapter implements IProcessEventRepositoryPort {

    private final JdbcTemplate jdbc;

    public CopyProcessEventJpaAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public CopyProcessEvent guardar(CopyProcessEvent evento) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO copy_process_event (id_proceso, tipo_evento, ocurrido_en, payload_json) VALUES (?, ?, ?, ?)",
                new String[]{"id"}
            );
            ps.setString(1, evento.getIdProceso());
            ps.setString(2, evento.getTipoEvento().name());
            ps.setTimestamp(3, Timestamp.valueOf(evento.getOcurridoEn()));
            ps.setString(4, evento.getPayloadJson());
            return ps;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            evento.setId(generatedKey.longValue());
        }
        return evento;
    }

    @Override
    public List<CopyProcessEvent> buscarPorProceso(String idProceso) {
        return jdbc.query(
            "SELECT * FROM copy_process_event WHERE id_proceso = ? ORDER BY id ASC",
            COPY_PROCESS_EVENT_ROW_MAPPER,
            idProceso
        );
    }

    private static final RowMapper<CopyProcessEvent> COPY_PROCESS_EVENT_ROW_MAPPER = (rs, rowNum) -> {
        CopyProcessEvent evento = CopyProcessEvent.crear(
            rs.getString("id_proceso"),
            CopyEventType.valueOf(rs.getString("tipo_evento")),
            rs.getString("payload_json")
        );
        evento.setId(rs.getLong("id"));
        return evento;
    };
}
