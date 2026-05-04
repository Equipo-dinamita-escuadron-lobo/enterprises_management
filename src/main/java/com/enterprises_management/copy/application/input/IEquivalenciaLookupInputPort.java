package com.enterprises_management.copy.application.input;

import com.enterprises_management.copy.domain.models.EquivalenciaLookupRequest;
import com.enterprises_management.copy.domain.models.EquivalenciaLookupResponse;

/**
 * Puerto de entrada para el lookup selectivo de equivalencias (REQ-LOOKUP-01, ADR-35).
 *
 * <p>Permite a un participante Fase 3 consultar en batch los IDs nuevos
 * para un conjunto de IDs viejos, sin necesidad de recibir todas las
 * equivalencias del proceso en el body de inicio (request rico).
 */
public interface IEquivalenciaLookupInputPort {

    /**
     * Consulta selectiva de equivalencias por batch de IDs.
     *
     * @param idProceso ID del proceso de copia
     * @param request   solicitud con lista de ítems (modulo, tabla, idsViejos[])
     * @return respuesta con mappings encontrados y notFound
     * @throws IllegalArgumentException si el total de IDs supera el límite máximo de batch
     */
    EquivalenciaLookupResponse lookup(String idProceso, EquivalenciaLookupRequest request);
}
