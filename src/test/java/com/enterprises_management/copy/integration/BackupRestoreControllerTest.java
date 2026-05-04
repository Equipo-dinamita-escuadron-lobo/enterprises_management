package com.enterprises_management.copy.integration;

import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.input.ICopyRestoreInputPort;
import com.enterprises_management.copy.application.output.IBackupReaderPort;
import com.enterprises_management.copy.application.services.SagaEngineService;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.exceptions.BackupCorruptedException;
import com.enterprises_management.copy.domain.exceptions.BackupNotFoundException;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.BackupController;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.RestoreController;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.advice.CopyExceptionHandler;
import com.enterprises_management.copy.infraestructure.config.CopyOrchestratorProperties;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.interceptor.TenantInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración MockMvc para BackupController y RestoreController
 * (REQ-DOWNLOAD-01, REQ-RESTORE-01, ADR-47).
 */
@WebMvcTest(controllers = {BackupController.class, RestoreController.class})
@Import({
        CopyExceptionHandler.class,
        com.enterprises_management.enterprise.infraestructure.security.SecurityConfig.class
})
@DisplayName("BackupController + RestoreController — tests de integración MockMvc")
class BackupRestoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICopyProcessQueryPort queryPort;

    @MockBean
    private IBackupReaderPort backupReader;

    @MockBean
    private ICopyRestoreInputPort restoreService;

    @MockBean
    private CopyOrchestratorProperties props;

    /** Necesario para satisfacer WebConfiguration que requiere TenantInterceptor. */
    @MockBean
    private TenantInterceptor tenantInterceptor;

    /** Necesario porque RestoreController recibe JdbcTemplate en el constructor. */
    @MockBean
    private JdbcTemplate jdbcTemplate;

    /** Necesario porque RestoreController dispara la saga async. */
    @MockBean
    private SagaEngineService sagaEngineService;

    /** Necesario para SecurityConfig. */
    @MockBean
    private com.enterprises_management.enterprise.infraestructure.security.JwtAuthConverter jwtAuthConverter;

    @TempDir
    Path tempDir;

    private CopyOrchestratorProperties.Backup backupConfig;

    @BeforeEach
    void setUp() {
        backupConfig = new CopyOrchestratorProperties.Backup();
        backupConfig.setDir(tempDir.toString());
        backupConfig.setMaxSizeBytes(524_288_000L);
        when(props.getBackup()).thenReturn(backupConfig);
    }

    // =========================================================================
    // BackupController — GET /api/enterprises/copy/processes/{id}/backup
    // =========================================================================

    @Test
    @WithMockUser(authorities = "Backup_Download")
    @DisplayName("GET /{id}/backup — 200 con ZIP body cuando backupRef existe (REQ-DOWNLOAD-01)")
    void descargarBackup_backupRefExiste_devuelve200ConZip() throws Exception {
        // GIVEN — crear ZIP real en tempDir
        String filename = "backup_test_200.zip";
        crearZipMinimo(tempDir.resolve(filename));

        String idProceso = UUID.randomUUID().toString();
        CopyProcess proceso = procesoCon(idProceso, filename);
        when(queryPort.consultarProceso(idProceso)).thenReturn(proceso);

        // WHEN / THEN
        mockMvc.perform(get("/api/enterprises/copy/processes/{id}/backup", idProceso))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/zip"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"" + filename + "\""));
    }

    @Test
    @WithMockUser(authorities = "Backup_Download")
    @DisplayName("GET /{id}/backup — 404 cuando backupRef es null (REQ-DOWNLOAD-01 escenario 2)")
    void descargarBackup_backupRefNulo_devuelve404() throws Exception {
        // GIVEN — proceso sin backupRef
        String idProceso = UUID.randomUUID().toString();
        CopyProcess proceso = procesoCon(idProceso, null);
        when(queryPort.consultarProceso(idProceso)).thenReturn(proceso);

        // WHEN / THEN
        mockMvc.perform(get("/api/enterprises/copy/processes/{id}/backup", idProceso))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "Backup_Download")
    @DisplayName("GET /{id}/backup — 404 cuando proceso no encontrado (REQ-DOWNLOAD-01 escenario 3)")
    void descargarBackup_procesoNoEncontrado_devuelve404() throws Exception {
        // GIVEN — queryPort lanza CopyProcessNotFoundException
        String idProceso = UUID.randomUUID().toString();
        when(queryPort.consultarProceso(idProceso))
                .thenThrow(new CopyProcessNotFoundException(idProceso));

        // WHEN / THEN
        mockMvc.perform(get("/api/enterprises/copy/processes/{id}/backup", idProceso))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // RestoreController — POST /api/enterprises/copy/restore
    // =========================================================================

    @Test
    @WithMockUser(authorities = "Backup_Restore")
    @DisplayName("POST /restore — 201 cuando backup válido (REQ-RESTORE-01)")
    void iniciarRestore_backupValido_devuelve201() throws Exception {
        // GIVEN
        String idProceso = UUID.randomUUID().toString();
        UUID empresaOrigen = UUID.randomUUID();
        CopyProcess nuevoProceso = CopyProcess.restaurar(
                idProceso, CopyProcessType.RESTORE, empresaOrigen,
                "empresa-destino-nueva", "backup_test.zip", null, "usuario");

        when(restoreService.iniciarRestore(any())).thenReturn(nuevoProceso);
        when(queryPort.consultarFases(idProceso)).thenReturn(List.of());

        String body = """
                {
                    "backupRef": "backup_empresa_proc_20260430-120000.zip",
                    "empresaDestino": "empresa-destino-nueva"
                }
                """;

        // WHEN / THEN
        mockMvc.perform(post("/api/enterprises/copy/restore")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProceso").value(idProceso))
                .andExpect(jsonPath("$.tipo").value("RESTORE"));
    }

    @Test
    @WithMockUser(authorities = "Backup_Restore")
    @DisplayName("POST /restore — 404 cuando BackupNotFoundException (REQ-RESTORE-01 escenario 2)")
    void iniciarRestore_backupNoEncontrado_devuelve404() throws Exception {
        // GIVEN
        when(restoreService.iniciarRestore(any()))
                .thenThrow(new BackupNotFoundException("backup_test.zip"));

        String body = """
                {
                    "backupRef": "backup_test.zip",
                    "empresaDestino": "empresa-nueva"
                }
                """;

        // WHEN / THEN
        mockMvc.perform(post("/api/enterprises/copy/restore")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "Backup_Restore")
    @DisplayName("POST /restore — 422 cuando BackupCorruptedException (REQ-RESTORE-01 escenario 2)")
    void iniciarRestore_backupCorrupto_devuelve422() throws Exception {
        // GIVEN
        when(restoreService.iniciarRestore(any()))
                .thenThrow(new BackupCorruptedException("manifest.json inválido"));

        String body = """
                {
                    "backupRef": "backup_corrupto.zip",
                    "empresaDestino": "empresa-nueva"
                }
                """;

        // WHEN / THEN
        mockMvc.perform(post("/api/enterprises/copy/restore")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Crea un proceso con un backupRef específico.
     */
    private CopyProcess procesoCon(String id, String backupRef) {
        CopyProcess proceso = CopyProcess.restaurar(
                id, CopyProcessType.BACKUP, UUID.randomUUID(),
                null, backupRef, null, "usuario-test");
        return proceso;
    }

    /**
     * Crea un ZIP mínimo válido en la ruta indicada.
     */
    private void crearZipMinimo(Path rutaZip) throws Exception {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(rutaZip))) {
            zos.putNextEntry(new ZipEntry("manifest.json"));
            zos.write("{\"version\":\"1.0\"}".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
    }
}
