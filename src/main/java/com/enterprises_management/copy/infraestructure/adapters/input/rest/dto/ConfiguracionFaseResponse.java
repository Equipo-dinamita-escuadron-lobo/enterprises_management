package com.enterprises_management.copy.infraestructure.adapters.input.rest.dto;

/**
 * DTO de respuesta para la configuración de un módulo en una fase (REQ-CFG-01).
 */
public record ConfiguracionFaseResponse(
        String id,
        int numeroFase,
        String modulo,
        int orden,
        boolean activo,
        String parametrosJson
) {}
