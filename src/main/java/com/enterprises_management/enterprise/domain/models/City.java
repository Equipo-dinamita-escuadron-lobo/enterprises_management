package com.enterprises_management.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Modelo de dominio que representa una ciudad.
 * Contiene la información básica de una ciudad y su relación
 * con el departamento al que pertenece.
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
public class City
{
    /**
     * Identificador único de la ciudad.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de la ciudad.
     */
    private String name;

    /**
     * Departamento al que pertenece la ciudad.
     */
    @ManyToOne
    private Department department;
}
