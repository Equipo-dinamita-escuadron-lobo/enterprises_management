package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que representa un país en la base de datos.
 * Mapea la tabla "country".
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="country")
public class CountryEntity {

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
