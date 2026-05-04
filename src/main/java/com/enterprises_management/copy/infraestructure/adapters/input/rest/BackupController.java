package com.enterprises_management.copy.infraestructure.adapters.input.rest;

import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.output.IBackupReaderPort;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.infraestructure.config.CopyOrchestratorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Controlador REST para la descarga de archivos ZIP de backup (REQ-DOWNLOAD-01, ADR-47).
 * <p>
 * Solo se activa cuando {@code app.copy.orchestrator.enabled=true}.
 * <p>
 * Seguridad: requiere la autoridad {@code Backup_Download}.
 * La respuesta se transmite por streaming para no cargar el archivo completo en heap (ADR-47).
 */
@RestController
@RequestMapping("/api/enterprises/copy/processes/{id}/backup")
@ConditionalOnProperty(name = "app.copy.orchestrator.enabled", havingValue = "true")
public class BackupController {

    private static final Logger log = LoggerFactory.getLogger(BackupController.class);

    private final ICopyProcessQueryPort queryPort;
    private final IBackupReaderPort backupReader;
    private final CopyOrchestratorProperties props;

    public BackupController(
            ICopyProcessQueryPort queryPort,
            IBackupReaderPort backupReader,
            CopyOrchestratorProperties props
    ) {
        this.queryPort = queryPort;
        this.backupReader = backupReader;
        this.props = props;
    }

    // -------------------------------------------------------------------------
    // GET /api/enterprises/copy/processes/{id}/backup — Descargar ZIP
    // -------------------------------------------------------------------------

    /**
     * Descarga el archivo ZIP de backup del proceso indicado (REQ-DOWNLOAD-01).
     * <p>
     * Flujo:
     * <ol>
     *   <li>Consulta el proceso — 404 si no existe (propagado por queryPort).</li>
     *   <li>Verifica que backupRef no sea nulo — 404 si el proceso no generó backup.</li>
     *   <li>Protección path traversal — 403 si la ruta escapa al directorio base.</li>
     *   <li>Comprobación de existencia del archivo — 404 si fue eliminado del disco.</li>
     *   <li>Comprobación de tamaño — 413 si supera {@code backup.maxSizeBytes}.</li>
     *   <li>Transmisión por streaming con {@link StreamingResponseBody} (ADR-47).</li>
     * </ol>
     *
     * @param id identificador del proceso
     * @return 200 con el contenido binario del ZIP, o 404/403/413 según el caso
     */
    @GetMapping
    @PreAuthorize("hasAuthority('Backup_Download')")
    public ResponseEntity<StreamingResponseBody> descargarBackup(@PathVariable String id) {
        // 1. Buscar el proceso — CopyProcessNotFoundException → 404 via CopyExceptionHandler
        CopyProcess proceso = queryPort.consultarProceso(id);

        // 2. Verificar que el proceso tenga backupRef
        String backupRef = proceso.getBackupRef();
        if (backupRef == null || backupRef.isBlank()) {
            log.debug("Proceso {} no tiene backup asociado (backupRef nulo)", id);
            return ResponseEntity.notFound().build();
        }

        // 3. Protección contra path traversal (ADR-47)
        Path backupDir = Path.of(props.getBackup().getDir()).toAbsolutePath().normalize();
        Path resolved = backupDir.resolve(backupRef).normalize();
        if (!resolved.startsWith(backupDir)) {
            log.warn("Intento de path traversal detectado para proceso {}: backupRef={}", id, backupRef);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 4. Comprobar existencia del archivo
        if (!Files.exists(resolved)) {
            log.warn("Archivo de backup no encontrado en disco para proceso {}: {}", id, resolved);
            return ResponseEntity.notFound().build();
        }

        // 5. Comprobar tamaño máximo
        long size;
        try {
            size = Files.size(resolved);
        } catch (IOException ex) {
            log.warn("No se pudo obtener el tamaño del backup para proceso {}: {}", id, ex.getMessage());
            return ResponseEntity.notFound().build();
        }
        if (size > props.getBackup().getMaxSizeBytes()) {
            log.warn("Backup para proceso {} supera el tamaño máximo ({} > {})", id, size, props.getBackup().getMaxSizeBytes());
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        }

        // 6. Transmitir por streaming (memoria constante — ADR-47)
        String filename = resolved.getFileName().toString();
        StreamingResponseBody body = outputStream -> {
            try (InputStream in = Files.newInputStream(resolved)) {
                in.transferTo(outputStream);
            } catch (IOException ex) {
                log.error("Error durante la transmisión del backup para proceso {}: {}", id, ex.getMessage(), ex);
            }
        };

        log.info("Iniciando descarga de backup para proceso {}: {} ({} bytes)", id, filename, size);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(size))
                .body(body);
    }
}
