package com.enterprises_management.copy.application.input.command;

/**
 * Comando para iniciar un proceso RESTORE a partir de un backup serializado (REQ-RESTORE-01, ADR-46).
 *
 * @param backupRef      ruta relativa al ZIP de backup (p. ej. "uploads/backups/backup_xxx.zip")
 * @param empresaDestino UUID o nombre de la empresa destino del RESTORE (debe diferir de la origen)
 * @param iniciadoPor    claim sub del JWT del usuario que inicia el restore
 */
public record RestoreCommand(
        String backupRef,
        String empresaDestino,
        String iniciadoPor
) {}
