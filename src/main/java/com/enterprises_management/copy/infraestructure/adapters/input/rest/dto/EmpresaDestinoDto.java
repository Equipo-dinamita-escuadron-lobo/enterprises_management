package com.enterprises_management.copy.infraestructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO anidado para la empresa destino en el request de inicio de proceso.
 * OQ-6 resuelto: nombre proporcionado por el cliente; el entId se asigna en Fase 1.
 */
public record EmpresaDestinoDto(
        @NotBlank(message = "nombre de empresa destino es requerido")
        String nombre
) {}
