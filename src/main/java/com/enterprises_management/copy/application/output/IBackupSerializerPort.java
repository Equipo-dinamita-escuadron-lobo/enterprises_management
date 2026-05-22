package com.enterprises_management.copy.application.output;

import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;

import java.util.List;
import java.util.Map;

/**
 * Puerto de salida para serializar el estado de un proceso completado como ZIP (REQ-BACKUP-01, ADR-44).
 * La implementación concreta (ZipBackupSerializerAdapter) vive en la capa de infraestructura.
 */
public interface IBackupSerializerPort {

    /**
     * Serializa el estado de un proceso BACKUP completado en un archivo ZIP en el filesystem.
     * El ZIP contiene: manifest.json, equivalencias.json, phases.json y opcionalmente
     * archivos de datos por módulo (REQ-BACKUP-03, ADR-45).
     *
     * @param proceso        proceso en estado COMPLETADO
     * @param equivalencias  todas las equivalencias generadas durante el proceso
     * @param fases          las fases del proceso (para auditoría)
     * @param datosModulos   mapa de nombre-módulo → datos exportados (vacío si no hay datos)
     * @return backupRef — ruta relativa al ZIP escrito, persistible en CopyProcess.backupRef
     * @throws RuntimeException envoltura de IOException si falla la escritura
     */
    String serializarBackup(CopyProcess proceso, List<CopyEquivalenceId> equivalencias,
                            List<CopyPhase> fases, Map<String, Object> datosModulos);

    /**
     * Sobrecarga de compatibilidad — delega a {@link #serializarBackup(CopyProcess, List, List, Map)}
     * con un mapa de módulos vacío.
     *
     * @param proceso       proceso en estado COMPLETADO
     * @param equivalencias todas las equivalencias generadas durante el proceso
     * @param fases         las fases del proceso (para auditoría)
     * @return backupRef — ruta relativa al ZIP escrito, persistible en CopyProcess.backupRef
     */
    default String serializarBackup(CopyProcess proceso, List<CopyEquivalenceId> equivalencias, List<CopyPhase> fases) {
        return serializarBackup(proceso, equivalencias, fases, Map.of());
    }

    /**
     * Resuelve la ruta relativa donde se escribiría el backup para un proceso dado.
     * Útil para pre-validar permisos o mostrar la ruta esperada antes de la serialización.
     *
     * @param entIdOrigen identificador de la empresa origen
     * @param idProceso   ID del proceso
     * @return ruta relativa al ZIP (sin crearlo)
     */
    String resolverRutaBackup(String entIdOrigen, String idProceso);
}
