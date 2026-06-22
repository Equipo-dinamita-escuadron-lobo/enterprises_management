package com.enterprises_management.enterprise.domain.models;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio que representa una materia (subject).
 * Contiene los datos básicos de identificación y nombre.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    /**
     * Identificador único de la materia.
     */
    private UUID id;

    /**
     * Código de la materia.
     */
    private String code;

    /**
     * Nombre de la materia.
     */
    private String name;
}
