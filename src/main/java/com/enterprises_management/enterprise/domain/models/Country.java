package com.enterprises_management.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Modelo de dominio que representa un país.
 * Contiene la información básica de un país en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Country {

    /**
     * Identificador único del país.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del país.
     */
    private String name;
}
