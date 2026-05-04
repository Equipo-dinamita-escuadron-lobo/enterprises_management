package com.enterprises_management.copy.domain.exceptions;

/**
 * Excepción lanzada cuando el archivo ZIP de backup supera el tamaño máximo permitido.
 * Mapeada a HTTP 413 por CopyExceptionHandler (ADR-47).
 */
public class BackupTooLargeException extends RuntimeException {

    /**
     * Crea la excepción con el tamaño real y el límite configurado.
     *
     * @param sizeBytes  tamaño real del archivo en bytes
     * @param maxBytes   tamaño máximo permitido en bytes (app.copy.orchestrator.backup.max-size-bytes)
     */
    public BackupTooLargeException(long sizeBytes, long maxBytes) {
        super("Backup excede el tamaño máximo: " + sizeBytes + " > " + maxBytes);
    }
}
