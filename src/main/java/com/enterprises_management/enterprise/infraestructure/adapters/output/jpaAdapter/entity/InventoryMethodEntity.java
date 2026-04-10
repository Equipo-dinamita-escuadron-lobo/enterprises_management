package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/**
 * Entidad JPA que representa un tipo de empresa en la base de datos.
 * Mapea la tabla "enterprise_type".
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="InventoryMethodEntity")

public class InventoryMethodEntity {
    /**
     * Identificador único del método de inventario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    /**
     * Nombre del método de inventario.
     */
    private String name;
    
}
