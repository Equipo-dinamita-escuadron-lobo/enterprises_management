package com.enterprises_management.copy.domain.exceptions;

/**
 * Excepción lanzada cuando el archivo ZIP de backup existe pero está corrupto,
 * tiene estructura inválida o contiene una versión no soportada.
 * Mapeada a HTTP 422 por CopyExceptionHandler (REQ-RESTORE-01, ADR-47).
 */
public class BackupCorruptedException extends RuntimeException {

    /**
     * Crea la excepción con el detalle del problema de corrupción.
     *
     * @param detail descripción del problema encontrado (p. ej. "versión no soportada: 2.0")
     */
    public BackupCorruptedException(String detail) {
        super("Backup corrupto o inválido: " + detail);
    }
}
