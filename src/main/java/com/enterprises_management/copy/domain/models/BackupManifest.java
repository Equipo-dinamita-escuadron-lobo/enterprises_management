package com.enterprises_management.copy.domain.models;

import com.enterprises_management.copy.domain.enums.CopyProcessType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Value object inmutable que representa el manifest de un backup serializado.
 * Contiene los metadatos del proceso original para auditoría y restauración (REQ-BACKUP-03, ADR-45).
 *
 * @param version                versión del formato de backup (actualmente "1.0")
 * @param idProceso              UUID del proceso BACKUP original
 * @param tipo                   tipo del proceso (BACKUP o DUPLICATE)
 * @param empresaOrigen          UUID de la empresa origen
 * @param empresaDestinoOriginal nombre de la empresa destino original del proceso
 * @param iniciadoPor            claim sub del JWT del usuario que inició el proceso
 * @param iniciadoEn             instante de inicio del proceso
 * @param finalizadoEn           instante de finalización del proceso
 * @param backupRef              ruta relativa al archivo ZIP
 */
public record BackupManifest(
        String version,
        String idProceso,
        CopyProcessType tipo,
        UUID empresaOrigen,
        String empresaDestinoOriginal,
        String iniciadoPor,
        LocalDateTime iniciadoEn,
        LocalDateTime finalizadoEn,
        String backupRef
) {}
