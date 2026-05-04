package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.entity;

import com.enterprises_management.copy.domain.enums.ModuleExecutionState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA de la ejecución de un módulo dentro de una fase.
 * Mapea la tabla copy_module_execution.
 * UNIQUE (id_proceso, id_fase, modulo) para idempotencia (REQ-IDEM-01).
 * ADR-2: prefijo copy_. ADR-5: estado como VARCHAR/String. ADR-6: @Enumerated STRING.
 */
@Entity
@Table(name = "copy_module_execution")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyModuleExecutionEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "id_proceso", nullable = false, length = 36)
    private String idProceso;

    @Column(name = "id_fase", nullable = false, length = 36)
    private String idFase;

    @Column(name = "modulo", nullable = false, length = 100)
    private String modulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private ModuleExecutionState estado;

    @Column(name = "intentos", nullable = false)
    private Integer intentos;

    @Column(name = "iniciado_en")
    private LocalDateTime iniciadoEn;

    @Column(name = "finalizado_en")
    private LocalDateTime finalizadoEn;

    @Column(name = "error_detalle", length = 2000)
    private String errorDetalle;
}
