package com.enterprises_management.copy.infraestructure.adapters.input.rest;

import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.input.ICopyRestoreInputPort;
import com.enterprises_management.copy.application.input.command.RestoreCommand;
import com.enterprises_management.copy.application.services.SagaEngineService;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.dto.ProcesoCopiaResponse;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.dto.RestoreRequestDto;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.mapper.CopyRestMapper;
import com.enterprises_management.copy.infraestructure.config.CopyOrchestratorProperties;
import com.enterprises_management.enterprise.application.ports.input.IEnterpriseUpdateManagerPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Controlador REST para restaurar una empresa desde un archivo ZIP de backup (REQ-RESTORE-01, ADR-46).
 * <p>
 * Solo se activa cuando {@code app.copy.orchestrator.enabled=true}.
 * <p>
 * Seguridad: requiere la autoridad {@code Backup_Restore}.
 */
@RestController
@RequestMapping("/api/enterprises/copy/restore")
@ConditionalOnProperty(name = "app.copy.orchestrator.enabled", havingValue = "true")
public class RestoreController {

    private static final Logger log = LoggerFactory.getLogger(RestoreController.class);

    private final ICopyRestoreInputPort restoreService;
    private final ICopyProcessQueryPort queryPort;
    private final SagaEngineService sagaEngineService;
    private final CopyOrchestratorProperties props;
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final IEnterpriseUpdateManagerPort enterpriseUpdatePort;

    public RestoreController(
            ICopyRestoreInputPort restoreService,
            ICopyProcessQueryPort queryPort,
            SagaEngineService sagaEngineService,
            CopyOrchestratorProperties props,
            JdbcTemplate jdbc,
            ObjectMapper objectMapper,
            IEnterpriseUpdateManagerPort enterpriseUpdatePort
    ) {
        this.restoreService = restoreService;
        this.queryPort = queryPort;
        this.sagaEngineService = sagaEngineService;
        this.props = props;
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.enterpriseUpdatePort = enterpriseUpdatePort;
    }

    // -------------------------------------------------------------------------
    // POST /api/enterprises/copy/restore — Iniciar restore desde backup
    // -------------------------------------------------------------------------

    /**
     * Inicia un proceso RESTORE a partir de un backup ZIP existente (REQ-RESTORE-01).
     * <p>
     * Cuando {@code inplace=true} (H9): elimina la empresa destino (cascade) y la recrea
     * desde el enterprise.json del ZIP antes de lanzar la saga.
     *
     * @param request        datos del restore (backupRef, empresaDestino, inplace)
     * @param authentication contexto de seguridad — se extrae el claim 'sub' (iniciadoPor)
     * @return 201 con el proceso RESTORE creado y sus fases en estado PENDIENTE
     */
    @PostMapping
    @PreAuthorize("hasRole('admin_client') or hasRole('user_client') or hasRole('super_client')")
    public ResponseEntity<ProcesoCopiaResponse> iniciarRestore(
            @RequestBody @Valid RestoreRequestDto request,
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest httpRequest
    ) {
        String iniciadoPor = extraerSub(authentication);
        boolean inplace = Boolean.TRUE.equals(request.inplace());

        log.info("Iniciando restore desde backup '{}' hacia empresa '{}' por '{}' (inplace={})",
                request.backupRef(), request.empresaDestino(), iniciadoPor, inplace);

        String empresaDestinoFinal = request.empresaDestino();

        Path backupDir = Path.of(props.getBackup().getDir()).toAbsolutePath().normalize();
        Path zipPath = backupDir.resolve(request.backupRef());

        if (inplace) {
            // H9: eliminar empresa actual (cascade) y recrear desde el enterprise.json del ZIP
            try {
                enterpriseUpdatePort.deleteEnterprise(UUID.fromString(request.empresaDestino()));
                log.info("Empresa {} eliminada para restore inplace", request.empresaDestino());
            } catch (Exception ex) {
                log.error("No se pudo eliminar empresa {} para inplace restore: {}",
                        request.empresaDestino(), ex.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            empresaDestinoFinal = crearEmpresaDesdeJson(zipPath, true, null);

            if (empresaDestinoFinal == null) {
                log.error("No se pudo recrear la empresa desde el ZIP {} para inplace restore", request.backupRef());
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
            }
        } else {
            // Para restore normal: crear empresa desde enterprise.json del ZIP con el nombre solicitado
            String createdId = crearEmpresaDesdeJson(zipPath, false, request.empresaDestino());
            if (createdId != null) {
                empresaDestinoFinal = createdId;
            }
        }

        RestoreCommand cmd = new RestoreCommand(request.backupRef(), empresaDestinoFinal, iniciadoPor, inplace);
        CopyProcess proceso = restoreService.iniciarRestore(cmd);

        String bearerToken = extraerBearerToken(httpRequest);
        proceso.setBearerToken(bearerToken);
        sagaEngineService.registrarBearerToken(proceso.getId(), bearerToken);

        final String procesoId = proceso.getId();
        CompletableFuture.runAsync(() -> {
            try {
                sagaEngineService.avanzarFase(procesoId, 1);
            } catch (Exception ex) {
                log.error("Error al ejecutar la saga para restore {}: {}", procesoId, ex.getMessage(), ex);
            }
        });

        List<CopyPhase> fases = queryPort.consultarFases(proceso.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CopyRestMapper.toResponse(proceso, fases));
    }

    // -------------------------------------------------------------------------
    // POST /api/enterprises/copy/restore/upload — Subir ZIP externo y restaurar
    // -------------------------------------------------------------------------

    /**
     * Acepta un archivo ZIP de backup subido por el usuario, lo guarda en el
     * directorio de backups y arranca el proceso RESTORE (REQ-RESTORE-01).
     * <p>
     * Cuando {@code inplace=true} (H9): elimina la empresa {@code empresaDestino} (cascade)
     * antes de crearla desde el enterprise.json del ZIP.
     *
     * @param file           ZIP descargado previamente desde el sistema
     * @param empresaDestino UUID de la empresa destino; en modo inplace es el UUID a sobreescribir
     * @param inplace        si es true, elimina empresaDestino y recrea desde el ZIP
     * @param authentication contexto de seguridad
     * @return 201 con el proceso RESTORE creado
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('admin_client') or hasRole('user_client') or hasRole('super_client')")
    public ResponseEntity<ProcesoCopiaResponse> subirYRestaurar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("empresaDestino") String empresaDestino,
            @RequestParam(value = "inplace", defaultValue = "false") boolean inplace,
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest httpRequest
    ) throws IOException {
        String backupRef = guardarZipSubido(file);
        String iniciadoPor = extraerSub(authentication);

        Path backupDir = Path.of(props.getBackup().getDir()).toAbsolutePath().normalize();
        Path zipPath = backupDir.resolve(backupRef);

        if (inplace) {
            // H9: eliminar empresa actual antes de recrearla desde el ZIP
            try {
                enterpriseUpdatePort.deleteEnterprise(UUID.fromString(empresaDestino));
                log.info("Empresa {} eliminada para restore inplace desde ZIP", empresaDestino);
            } catch (Exception ex) {
                log.error("No se pudo eliminar empresa {} para inplace restore desde ZIP: {}",
                        empresaDestino, ex.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }

        // Crear empresa desde enterprise.json del ZIP (inplace=true → nombre sin sufijo)
        String empresaDestinoFinal = crearEmpresaDesdeJson(zipPath, inplace, null);
        if (empresaDestinoFinal == null) {
            empresaDestinoFinal = empresaDestino;
        }

        log.info("Restore desde ZIP subido '{}' hacia empresa '{}' por '{}' (inplace={})",
                backupRef, empresaDestinoFinal, iniciadoPor, inplace);

        RestoreCommand cmd = new RestoreCommand(backupRef, empresaDestinoFinal, iniciadoPor, inplace);
        CopyProcess proceso = restoreService.iniciarRestore(cmd);

        String bearerToken = extraerBearerToken(httpRequest);
        proceso.setBearerToken(bearerToken);
        sagaEngineService.registrarBearerToken(proceso.getId(), bearerToken);

        final String procesoId = proceso.getId();
        CompletableFuture.runAsync(() -> {
            try {
                sagaEngineService.avanzarFase(procesoId, 1);
            } catch (Exception ex) {
                log.error("Error al ejecutar la saga para restore upload {}: {}", procesoId, ex.getMessage(), ex);
            }
        });

        List<CopyPhase> fases = queryPort.consultarFases(proceso.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CopyRestMapper.toResponse(proceso, fases));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Lee enterprise.json del ZIP y crea una empresa nueva en la BD.
     *
     * @param zipPath       ruta al archivo ZIP de backup
     * @param inplace       si es true, usa el nombre original sin sufijo
     * @param nombreDestino nombre explícito para la empresa; null = usa nombre original + "(Importada)"
     * @return UUID de la empresa creada, o null si el ZIP no contiene enterprise.json
     */
    @SuppressWarnings("unchecked")
    private String crearEmpresaDesdeJson(Path zipPath, boolean inplace, String nombreDestino) {
        String json;
        Map<String, Object> snap;
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            ZipEntry entry = zipFile.getEntry("enterprise.json");
            if (entry == null) {
                log.warn("El ZIP no contiene enterprise.json — usando empresaDestino proporcionado");
                return null;
            }
            try (InputStream is = zipFile.getInputStream(entry)) {
                json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            log.warn("No se pudo leer enterprise.json del ZIP: {}", ex.getMessage());
            return null;
        }

        try {
            snap = objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            log.warn("No se pudo parsear enterprise.json: {}", ex.getMessage());
            return null;
        }

        // Insertar person_type y location (críticos — si fallan, abortamos)
        Long personTypeId;
        Long locationId;
        try {
            Map<String, Object> personType = (Map<String, Object>) snap.get("person_type");
            personTypeId = jdbc.queryForObject(
                    "INSERT INTO person_type (type, name, surname, bussiness_name) VALUES (?, ?, ?, ?) RETURNING id",
                    Long.class,
                    personType.get("type"), personType.get("name"),
                    personType.get("surname"), personType.get("bussiness_name")
            );

            Map<String, Object> location = (Map<String, Object>) snap.get("location");
            locationId = jdbc.queryForObject(
                    "INSERT INTO location (address, city_id, department_id, country_id) VALUES (?, ?, ?, ?) RETURNING id",
                    Long.class,
                    location.get("address"), location.get("city_id"),
                    location.get("department_id"), location.get("country_id")
            );
        } catch (Exception ex) {
            log.error("No se pudo insertar person_type/location desde enterprise.json: {}", ex.getMessage());
            return null;
        }

        String newId = UUID.randomUUID().toString();
        String restoredName = inplace
                ? (String) snap.get("name")
                : (nombreDestino != null && !nombreDestino.isBlank()
                        ? nombreDestino
                        : snap.get("name") + " (Importada)");

        // Insertar empresa (crítico — si falla, abortamos)
        try {
            jdbc.update("""
                    INSERT INTO enterprise (id, id_user, name, nit, dv, phone, branch, email, logo,
                                            state, main_activity, secondary_activity, inventory_methods,
                                            tax_payer_type_id, enterprise_type_id, person_type_id, location_id, tenant_id)
                    VALUES (CAST(? AS uuid), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    newId, snap.get("id_user"), restoredName, snap.get("nit"), snap.get("dv"),
                    snap.get("phone"), snap.get("branch"), snap.get("email"), snap.get("logo"),
                    snap.get("state"), snap.get("main_activity"), snap.get("secondary_activity"),
                    snap.get("inventory_methods"), snap.get("tax_payer_type_id"),
                    snap.get("enterprise_type_id"), personTypeId, locationId, snap.get("tenant_id")
            );
        } catch (Exception ex) {
            log.error("No se pudo insertar enterprise desde enterprise.json: {}", ex.getMessage());
            return null;
        }

        // Restaurar responsabilidades fiscales via JDBC directo (mismo enfoque que enterprise_subject)
        List<?> taxLiabilities = (List<?>) snap.get("taxLiabilities");
        if (taxLiabilities != null) {
            for (Object taxId : taxLiabilities) {
                if (taxId == null) continue;
                try {
                    jdbc.update(
                            "INSERT INTO enterprise_tax_liabilities (enterprise_id, tax_liability_id) VALUES (CAST(? AS uuid), ?)",
                            newId, ((Number) taxId).longValue()
                    );
                } catch (Exception ex) {
                    log.warn("No se pudo insertar tax_liability {} para empresa {}: {}", taxId, newId, ex.getMessage());
                }
            }
        }

        // Restaurar asociaciones con materias — best-effort (no abortan la empresa)
        List<?> subjectIds = (List<?>) snap.get("subjectIds");
        if (subjectIds != null) {
            for (Object subjectId : subjectIds) {
                if (subjectId == null) continue;
                try {
                    jdbc.update(
                            "INSERT INTO enterprise_subject (enterprise_id, subject_id) VALUES (CAST(? AS uuid), CAST(? AS uuid))",
                            newId, subjectId.toString()
                    );
                } catch (Exception ex) {
                    log.warn("No se pudo insertar subject {} para empresa {}: {}", subjectId, newId, ex.getMessage());
                }
            }
        }

        log.info("Empresa creada desde backup ZIP: id={}, name={}, inplace={}", newId, restoredName, inplace);
        return newId;
    }

    private String guardarZipSubido(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo ZIP está vacío");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("El archivo debe ser un ZIP válido");
        }
        String safeFilename = Path.of(originalName).getFileName().toString();
        Path backupDir = Path.of(props.getBackup().getDir()).toAbsolutePath().normalize();
        Files.createDirectories(backupDir);
        Path destino = backupDir.resolve(safeFilename);
        file.transferTo(destino.toFile());
        return safeFilename;
    }

    /**
     * Extrae el claim 'sub' del JWT (identificador único del usuario en Keycloak).
     */
    private String extraerSub(Authentication authentication) {
        if (authentication == null) {
            return "anonymous";
        }
        return authentication.getName();
    }

    private String extraerBearerToken(jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (httpRequest == null) return null;
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.substring("Bearer ".length()).trim();
    }
}
