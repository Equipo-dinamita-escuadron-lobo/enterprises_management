package com.enterprises_management.copy.application.output;

import com.enterprises_management.copy.domain.models.CopyProcess;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistir y recuperar procesos de copia.
 */
public interface ICopyProcessRepositoryPort {

    /**
     * Persiste un proceso nuevo.
     *
     * @param proceso proceso a guardar
     * @return proceso guardado (con posibles valores asignados por la BD)
     */
    CopyProcess guardar(CopyProcess proceso);

    /**
     * Actualiza un proceso existente.
     *
     * @param proceso proceso con los cambios aplicados
     * @return proceso actualizado
     */
    CopyProcess actualizar(CopyProcess proceso);

    /**
     * Busca un proceso por su ID.
     *
     * @param idProceso ID del proceso
     * @return proceso encontrado o vacío
     */
    Optional<CopyProcess> buscarPorId(String idProceso);

    /**
     * Verifica si existe un proceso activo (PENDIENTE o EN_PROCESO) para una empresa origen.
     * Implementa ADR-4: índice único parcial en BD.
     *
     * @param empresaOrigen UUID de la empresa origen
     * @return true si existe un proceso activo para esa empresa
     */
    boolean existeProcesoActivoPara(UUID empresaOrigen);

    /**
     * Devuelve todos los procesos de copia ordenados por fecha de inicio descendente.
     *
     * @return lista de todos los procesos registrados
     */
    List<CopyProcess> listarTodos();
}
