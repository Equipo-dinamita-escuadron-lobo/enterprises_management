package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA de equivalencia idViejo → idNuevo.
 * Mapea la tabla copy_equivalence_id.
 * UNIQUE (id_proceso, modulo, tabla, id_viejo) — ADR-9, REQ-EQ-01.
 * ADR-5: id_viejo e id_nuevo como VARCHAR(80) para soportar UUID y bigint.
 */
@Entity
@Table(name = "copy_equivalence_id")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyEquivalenceIdEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "id_proceso", nullable = false, length = 36)
    private String idProceso;

    @Column(name = "modulo", nullable = false, length = 100)
    private String modulo;

    @Column(name = "tabla", nullable = false, length = 100)
    private String tabla;

    /** ADR-5: VARCHAR(80) para soportar UUID o bigint. */
    @Column(name = "id_viejo", nullable = false, length = 80)
    private String idViejo;

    /** ADR-5: VARCHAR(80). */
    @Column(name = "id_nuevo", nullable = false, length = 80)
    private String idNuevo;

    @Column(name = "registrado_en", nullable = false)
    private LocalDateTime registradoEn;
}
