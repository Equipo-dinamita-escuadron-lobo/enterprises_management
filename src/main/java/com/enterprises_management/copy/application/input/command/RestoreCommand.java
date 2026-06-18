package com.enterprises_management.copy.application.input.command;

/**
 * Comando para iniciar un proceso RESTORE a partir de un backup serializado (REQ-RESTORE-01, ADR-46).
 *
 * @param backupRef      ruta relativa al ZIP de backup (p. ej. "uploads/backups/backup_xxx.zip")
 * @param empresaDestino UUID de la empresa destino del RESTORE
 * @param iniciadoPor    claim sub del JWT del usuario que inicia el restore
 * @param inplace        true cuando se restaura sobre la empresa de origen (inplace)
 */
public record RestoreCommand(
        String backupRef,
        String empresaDestino,
        String iniciadoPor,
        boolean inplace
) {}
