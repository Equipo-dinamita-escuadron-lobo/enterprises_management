package com.enterprises_management.copy.application.input;

import com.enterprises_management.copy.domain.models.CopyEquivalenceId;

import java.util.List;

/**
 * Puerto de entrada para consultar equivalencias de IDs de un proceso de copia.
 * Corresponde a GET /api/enterprises/copy/processes/{id}/equivalences (REQ-EQ-02).
 */
public interface IEquivalenceQueryPort {

    /**
     * Consulta equivalencias de un proceso con filtros opcionales.
     * Si todos los filtros son null, retorna todas las equivalencias del proceso.
     *
     * @param idProceso ID del proceso (requerido)
     * @param modulo    nombre del módulo (opcional, null = sin filtro)
     * @param tabla     nombre de la tabla (opcional, null = sin filtro)
     * @param idViejo   ID original (opcional, null = sin filtro)
     * @return lista (posiblemente vacía) de equivalencias que coinciden con los filtros
     * @throws com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException
     *         si el proceso no existe
     */
    List<CopyEquivalenceId> consultar(String idProceso, String modulo, String tabla, String idViejo);
}
