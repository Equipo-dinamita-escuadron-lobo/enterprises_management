package com.enterprises_management.copy.application.input.command;

import com.enterprises_management.copy.domain.enums.CopyProcessType;

import java.util.UUID;

/**
 * Comando para iniciar un proceso de copia (REQ-API-02).
 * Contiene los datos validados del DTO de inicio del proceso.
 *
 * @param tipo           tipo de proceso (BACKUP, RESTORE, DUPLICATE)
 * @param empresaOrigen  UUID de la empresa origen (requerido)
 * @param empresaDestino nombre de la empresa destino (requerido para DUPLICATE)
 * @param backupRef      referencia al backup (requerido para RESTORE)
 * @param iniciadoPor    claim 'sub' del JWT del usuario que inicia el proceso
 * @param generateBackup si true, genera ZIP de backup al completar (solo DUPLICATE; REQ-BACKUP-02)
 */
public record IniciarProcesoCommand(
        CopyProcessType tipo,
        UUID empresaOrigen,
        String empresaDestino,
        String backupRef,
        String iniciadoPor,
        boolean generateBackup
) {}
