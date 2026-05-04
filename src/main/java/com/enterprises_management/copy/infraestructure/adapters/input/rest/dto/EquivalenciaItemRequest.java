package com.enterprises_management.copy.infraestructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Ítem individual de equivalencia en el request de registro (REQ-EQ-01).
 */
public record EquivalenciaItemRequest(
        @NotBlank(message = "modulo es requerido")
        String modulo,

        @NotBlank(message = "tabla es requerida")
        String tabla,

        @NotBlank(message = "idViejo es requerido")
        String idViejo,

        @NotBlank(message = "idNuevo es requerido")
        String idNuevo
) {}
