package com.enterprises_management.copy.application.input;

import com.enterprises_management.copy.application.input.command.RestoreCommand;
import com.enterprises_management.copy.domain.exceptions.BackupCorruptedException;
import com.enterprises_management.copy.domain.exceptions.BackupNotFoundException;
import com.enterprises_management.copy.domain.exceptions.BackupTooLargeException;
import com.enterprises_management.copy.domain.exceptions.DuplicateActiveProcessException;
import com.enterprises_management.copy.domain.models.CopyProcess;

/**
 * Puerto de entrada para iniciar un proceso RESTORE a partir de un backup serializado (REQ-RESTORE-01, ADR-46).
 */
public interface ICopyRestoreInputPort {

    /**
     * Inicia un proceso RESTORE a partir de un backup serializado previamente.
     * Lee el ZIP, valida el manifest, crea el proceso RESTORE y siembra las
     * equivalencias en copy_equivalence_id bajo el nuevo idProceso.
     *
     * @param command datos del restore (backupRef, empresaDestino, iniciadoPor)
     * @return CopyProcess en estado PENDIENTE listo para avanzarFase(_, 1)
     * @throws BackupNotFoundException         si el archivo no existe
     * @throws BackupCorruptedException        si el manifest es inválido o la versión no es soportada
     * @throws BackupTooLargeException         si el archivo excede backup.maxSizeBytes
     * @throws DuplicateActiveProcessException si ya hay proceso activo para la empresaOrigen del manifest
     * @throws IllegalArgumentException        si empresaDestino coincide con empresaOrigen (REQ-RESTORE-03)
     */
    CopyProcess iniciarRestore(RestoreCommand command);
}
