package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.entity;

import com.enterprises_management.copy.domain.enums.CopyEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA de un evento del proceso de copia.
 * Mapea la tabla copy_process_event.
 * ID BIGINT autoincremental (asignado por BD) — ADR-8 para deduplicación SSE por secuencia.
 */
@Entity
@Table(name = "copy_process_event")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyProcessEventEntity {

    /** ID BIGINT autoincremental asignado por la BD (ADR-8: secuencia para SSE). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "id_proceso", nullable = false, length = 36)
    private String idProceso;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 60)
    private CopyEventType tipoEvento;

    @Column(name = "ocurrido_en", nullable = false)
    private LocalDateTime ocurridoEn;

    /** Payload en JSON para SSE y trazabilidad. */
    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;
}
