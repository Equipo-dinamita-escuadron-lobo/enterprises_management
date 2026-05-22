package com.enterprises_management.copy.application.output;

import com.enterprises_management.copy.domain.exceptions.BackupCorruptedException;
import com.enterprises_management.copy.domain.exceptions.BackupNotFoundException;
import com.enterprises_management.copy.domain.models.BackupManifest;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;

import java.util.List;

/**
 * Puerto de salida para leer y deserializar el contenido de un ZIP de backup (REQ-RESTORE-01, ADR-46).
 * La implementación concreta (ZipBackupReaderAdapter) vive en la capa de infraestructura.
 */
public interface IBackupReaderPort {

    /**
     * Lee y deserializa el manifest.json del ZIP de backup.
     *
     * @param backupRef ruta relativa al ZIP
     * @return manifest deserializado
     * @throws BackupNotFoundException  si el archivo no existe en el filesystem
     * @throws BackupCorruptedException si el manifest es inválido, no es JSON o la versión no es soportada
     */
    BackupManifest leerManifest(String backupRef) throws BackupNotFoundException, BackupCorruptedException;

    /**
     * Lee y deserializa equivalencias.json del ZIP de backup.
     * Las equivalencias devueltas tienen el idProceso ORIGINAL del manifest.
     * El caller debe re-keyear con el nuevo idProceso antes de persistir (ADR-46).
     *
     * @param backupRef ruta relativa al ZIP
     * @return lista de equivalencias con el idProceso original
     * @throws BackupNotFoundException  si el archivo no existe
     * @throws BackupCorruptedException si el JSON es inválido o el ZIP está corrupto
     */
    List<CopyEquivalenceId> leerEquivalencias(String backupRef) throws BackupNotFoundException, BackupCorruptedException;

    /**
     * Lee y deserializa el archivo de datos de un módulo específico del ZIP de backup.
     * Retorna null si el archivo no existe (compatibilidad con backups sin datos de módulo).
     *
     * @param backupRef  ruta relativa al ZIP
     * @param moduleName nombre del módulo (ej: "CATALOGUE", "PRODUCTS") — se convierte a nombre de archivo
     * @return objeto deserializado (Map o List según contenido) o null si el archivo no existe
     * @throws BackupCorruptedException si el JSON es inválido
     */
    Object leerDatosModulo(String backupRef, String moduleName) throws BackupCorruptedException;
}
