package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Entidad JPA que representa una materia (Subject).
 * Mapea la tabla "subjects" en la base de datos.
 */
@Entity
@Table(name = "subjects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectEntity {

    /**
     * Identificador único de la materia (UUID autogenerado).
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Código de la materia (enviado por el usuario).
     */
    @Column(nullable = false, unique = true)
    private String code;

    /**
     * Nombre de la materia.
     */
    @Column(nullable = false)
    private String name;
}
