package com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto;

/**
 * DTO canónico que representa una equivalencia de IDs entre empresa origen y destino.
 *
 * <p>Usado tanto en el request (equivalencias previas para remapeo de FKs)
 * como en el response (equivalencias generadas en la fase actual) (ADR-18, REQ-CONTRACT-02, REQ-CONTRACT-03).
 *
 * @param modulo   nombre del módulo que generó la equivalencia
 * @param tabla    tabla de la entidad copiada (ej: "account", "tax")
 * @param idViejo  ID original en la empresa origen
 * @param idNuevo  ID asignado en la empresa destino
 */
public record CopyEquivalenceDto(
        String modulo,
        String tabla,
        String idViejo,
        String idNuevo
) {}
