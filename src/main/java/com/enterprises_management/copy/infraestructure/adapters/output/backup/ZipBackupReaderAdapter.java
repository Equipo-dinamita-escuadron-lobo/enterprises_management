package com.enterprises_management.copy.infraestructure.adapters.output.backup;

import com.enterprises_management.copy.application.output.IBackupReaderPort;
import com.enterprises_management.copy.domain.exceptions.BackupCorruptedException;
import com.enterprises_management.copy.domain.exceptions.BackupNotFoundException;
import com.enterprises_management.copy.domain.models.BackupManifest;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.infraestructure.config.CopyOrchestratorProperties;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Adaptador de salida que deserializa el contenido de un ZIP de backup (ADR-46).
 * Implementa IBackupReaderPort con protección contra path traversal (REQ-RESTORE-01).
 */
public class ZipBackupReaderAdapter implements IBackupReaderPort {

    private final CopyOrchestratorProperties props;
    private final ObjectMapper objectMapper;

    public ZipBackupReaderAdapter(CopyOrchestratorProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addMixIn(CopyEquivalenceId.class, CopyEquivalenceIdMixin.class);
    }

    @Override
    public BackupManifest leerManifest(String backupRef) throws BackupNotFoundException, BackupCorruptedException {
        Path rutaCompleta = resolverRuta(backupRef);

        try (ZipFile zip = new ZipFile(rutaCompleta.toFile())) {
            ZipEntry entrada = zip.getEntry("manifest.json");
            if (entrada == null) {
                throw new BackupCorruptedException("manifest.json no encontrado en el ZIP");
            }
            try (InputStream is = zip.getInputStream(entrada)) {
                return objectMapper.readValue(is, BackupManifest.class);
            } catch (JsonProcessingException ex) {
                throw new BackupCorruptedException("JSON inválido en manifest.json: " + ex.getOriginalMessage());
            }
        } catch (BackupNotFoundException | BackupCorruptedException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new BackupCorruptedException("Error al leer el ZIP: " + ex.getMessage());
        }
    }

    @Override
    public List<CopyEquivalenceId> leerEquivalencias(String backupRef)
            throws BackupNotFoundException, BackupCorruptedException {
        Path rutaCompleta = resolverRuta(backupRef);

        try (ZipFile zip = new ZipFile(rutaCompleta.toFile())) {
            ZipEntry entrada = zip.getEntry("equivalencias.json");
            if (entrada == null) {
                throw new BackupCorruptedException("equivalencias.json no encontrado en el ZIP");
            }
            try (InputStream is = zip.getInputStream(entrada)) {
                return objectMapper.readValue(is, new TypeReference<List<CopyEquivalenceId>>() {});
            } catch (JsonProcessingException ex) {
                throw new BackupCorruptedException("JSON inválido en equivalencias.json: " + ex.getOriginalMessage());
            }
        } catch (BackupNotFoundException | BackupCorruptedException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new BackupCorruptedException("Error al leer el ZIP: " + ex.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Resuelve la ruta al ZIP con protección contra path traversal (REQ-RESTORE-01).
     *
     * @param backupRef ruta relativa al ZIP
     * @return ruta absoluta validada
     * @throws BackupNotFoundException si la ruta es inválida o el archivo no existe
     */
    private Path resolverRuta(String backupRef) {
        Path baseDir = Path.of(props.getBackup().getDir()).toAbsolutePath().normalize();
        Path rutaCompleta = baseDir.resolve(backupRef).normalize();

        // Guarda de path traversal — rechazar rutas que escapen al directorio base
        if (!rutaCompleta.startsWith(baseDir)) {
            // No revelar detalles de la ruta interna (security hardening)
            throw new BackupNotFoundException(backupRef);
        }

        if (!Files.exists(rutaCompleta)) {
            throw new BackupNotFoundException(backupRef);
        }

        return rutaCompleta;
    }

    // -------------------------------------------------------------------------
    // Mixin para desserialización de CopyEquivalenceId sin modificar el dominio
    // -------------------------------------------------------------------------

    /**
     * Jackson MixIn que instruye al ObjectMapper a usar el constructor
     * de CopyEquivalenceId para deserialización. El dominio permanece sin
     * anotaciones de Jackson (ADR-5, arquitectura hexagonal).
     */
    abstract static class CopyEquivalenceIdMixin {

        @JsonCreator
        CopyEquivalenceIdMixin(
                @JsonProperty("id") String id,
                @JsonProperty("idProceso") String idProceso,
                @JsonProperty("modulo") String modulo,
                @JsonProperty("tabla") String tabla,
                @JsonProperty("idViejo") String idViejo,
                @JsonProperty("idNuevo") String idNuevo,
                @JsonProperty("registradoEn") LocalDateTime registradoEn
        ) {}
    }
}
