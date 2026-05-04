package com.enterprises_management.copy.domain.exceptions;

/**
 * Excepción lanzada cuando no se encuentra el archivo ZIP de backup referenciado.
 * Mapeada a HTTP 404 por CopyExceptionHandler (REQ-RESTORE-01, REQ-DOWNLOAD-01).
 */
public class BackupNotFoundException extends RuntimeException {

    /**
     * Crea la excepción con la referencia al backup no encontrado.
     *
     * @param backupRef ruta relativa del backup que no existe en el filesystem
     */
    public BackupNotFoundException(String backupRef) {
        super("Backup no encontrado: " + backupRef);
    }
}
