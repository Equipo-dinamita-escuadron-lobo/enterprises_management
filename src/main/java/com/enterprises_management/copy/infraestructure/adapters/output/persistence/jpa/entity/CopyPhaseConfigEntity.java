package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA de configuración de módulos por fase.
 * Mapea la tabla copy_phase_config. UNIQUE (numero_fase, modulo) en BD.
 * Permite configurar módulos participantes sin cambiar código (REQ-CFG-01).
 */
@Entity
@Table(name = "copy_phase_config")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyPhaseConfigEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "numero_fase", nullable = false)
    private Integer numeroFase;

    @Column(name = "modulo", nullable = false, length = 100)
    private String modulo;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "parametros_json", length = 2000)
    private String parametrosJson;
}
