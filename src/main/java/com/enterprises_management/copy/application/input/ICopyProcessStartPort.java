package com.enterprises_management.copy.application.input;

import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.domain.models.CopyProcess;

/**
 * Puerto de entrada para iniciar un proceso de copia.
 * Corresponde a POST /api/enterprises/copy/processes (REQ-PROC-01).
 */
public interface ICopyProcessStartPort {

    /**
     * Inicia un nuevo proceso de copia.
     *
     * @param command comando con los datos del proceso a iniciar
     * @return proceso creado en estado PENDIENTE
     * @throws com.enterprises_management.copy.domain.exceptions.DuplicateActiveProcessException
     *         si ya existe un proceso activo para la misma empresa origen (REQ-PROC-01 escenario 409)
     */
    CopyProcess iniciar(IniciarProcesoCommand command);
}
