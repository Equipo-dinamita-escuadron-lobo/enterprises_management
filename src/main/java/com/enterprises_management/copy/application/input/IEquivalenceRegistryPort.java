package com.enterprises_management.copy.application.input;

import com.enterprises_management.copy.domain.models.CopyEquivalenceId;

import java.util.List;

/**
 * Puerto de entrada para registrar equivalencias de IDs entre empresa origen y destino.
 * Corresponde a POST /api/enterprises/copy/processes/{id}/equivalences (REQ-EQ-01).
 */
public interface IEquivalenceRegistryPort {

    /**
     * Registra N equivalencias para un proceso de forma idempotente.
     * Si la clave (idProceso, modulo, tabla, idViejo) ya existe:
     *   - idNuevo igual → se omite (idempotente)
     *   - idNuevo distinto → lanza EquivalenceConflictException (ADR-9)
     *
     * @param equivalencias lista de equivalencias a registrar
     * @return número de equivalencias efectivamente insertadas o actualizadas
     * @throws com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException
     *         si el proceso asociado no existe
     * @throws com.enterprises_management.copy.domain.exceptions.EquivalenceConflictException
     *         si hay conflicto en idNuevo para una clave existente
     */
    int registrar(List<CopyEquivalenceId> equivalencias);
}
