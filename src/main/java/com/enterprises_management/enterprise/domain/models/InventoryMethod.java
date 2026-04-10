package com.enterprises_management.enterprise.domain.models;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 *
 * @author Juan Camilo Zarta Campo
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMethod {
    
    /**
     * Identificador único del método de inventario.
     */
    private UUID id;
    /**
     * Nombre del método de inventario.
     */
    private String name;
}
