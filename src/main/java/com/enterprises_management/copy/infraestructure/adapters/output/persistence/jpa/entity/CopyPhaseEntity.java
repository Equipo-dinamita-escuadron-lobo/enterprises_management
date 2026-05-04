package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.entity;

import com.enterprises_management.copy.domain.enums.PhaseState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA de una fase de la saga de copia.
 * Mapea la tabla copy_phase. UNIQUE (id_proceso, numero) en BD (REQ-FASE-01).
 * ADR-2: prefijo copy_. ADR-6: @Enumerated STRING.
 */
@Entity
@Table(name = "copy_phase")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyPhaseEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "id_proceso", nullable = false, length = 36)
    private String idProceso;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private PhaseState estado;

    @Column(name = "iniciada_en")
    private LocalDateTime iniciadaEn;

    @Column(name = "finalizada_en")
    private LocalDateTime finalizadaEn;

    @Column(name = "error_detalle", length = 2000)
    private String errorDetalle;
}
