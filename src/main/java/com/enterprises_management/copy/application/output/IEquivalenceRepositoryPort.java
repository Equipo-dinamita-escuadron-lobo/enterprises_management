package com.enterprises_management.copy.application.output;

import com.enterprises_management.copy.domain.models.CopyEquivalenceId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir y consultar equivalencias de IDs.
 */
public interface IEquivalenceRepositoryPort {

    /**
     * Persiste una equivalencia nueva.
     *
     * @param equivalencia equivalencia a guardar
     * @return equivalencia guardada
     */
    CopyEquivalenceId guardar(CopyEquivalenceId equivalencia);

    /**
     * Actualiza el idNuevo de una equivalencia existente.
     *
     * @param equivalencia equivalencia con el idNuevo actualizado
     * @return equivalencia actualizada
     */
    CopyEquivalenceId actualizar(CopyEquivalenceId equivalencia);

    /**
     * Busca una equivalencia por su clave de negocio (REQ-EQ-01, ADR-9).
     *
     * @param idProceso ID del proceso
     * @param modulo    nombre del módulo
     * @param tabla     nombre de la tabla
     * @param idViejo   ID original
     * @return equivalencia encontrada o vacío
     */
    Optional<CopyEquivalenceId> buscarPorClave(String idProceso, String modulo, String tabla, String idViejo);

    /**
     * Consulta equivalencias con filtros opcionales (REQ-EQ-02).
     * Los parámetros null son ignorados como filtro.
     *
     * @param idProceso ID del proceso (requerido)
     * @param modulo    filtro por módulo (opcional)
     * @param tabla     filtro por tabla (opcional)
     * @param idViejo   filtro por idViejo (opcional)
     * @return lista de equivalencias que coinciden
     */
    List<CopyEquivalenceId> buscarConFiltros(String idProceso, String modulo, String tabla, String idViejo);
}
