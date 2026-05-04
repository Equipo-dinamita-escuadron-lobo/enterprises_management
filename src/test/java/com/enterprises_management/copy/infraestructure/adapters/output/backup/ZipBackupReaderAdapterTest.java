package com.enterprises_management.copy.infraestructure.adapters.output.backup;

import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.exceptions.BackupCorruptedException;
import com.enterprises_management.copy.domain.exceptions.BackupNotFoundException;
import com.enterprises_management.copy.domain.models.BackupManifest;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.infraestructure.config.CopyOrchestratorProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitarios de ZipBackupReaderAdapter con directorio temporal (REQ-RESTORE-01, ADR-46).
 */
@DisplayName("ZipBackupReaderAdapter — deserialización de ZIP")
class ZipBackupReaderAdapterTest {

    @TempDir
    Path tempDir;

    private ZipBackupReaderAdapter sut;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        CopyOrchestratorProperties props = new CopyOrchestratorProperties();
        props.getBackup().setDir(tempDir.toString());

        sut = new ZipBackupReaderAdapter(props, objectMapper);
    }

    // =========================================================================
    // Test 1 — leerManifest con ZIP válido retorna manifest
    // =========================================================================

    @Test
    @DisplayName("leerManifest ZIP válido → retorna BackupManifest deserializado (REQ-RESTORE-01)")
    void leerManifest_zip_valido_retorna_manifest() throws Exception {
        // GIVEN — construir un manifest y crear ZIP en tempDir
        UUID empresaOrigen = UUID.randomUUID();
        BackupManifest manifest = new BackupManifest(
                "1.0",
                UUID.randomUUID().toString(),
                CopyProcessType.BACKUP,
                empresaOrigen,
                "empresa-destino-original",
                "usuario-backup",
                LocalDateTime.of(2026, 4, 30, 10, 0),
                LocalDateTime.of(2026, 4, 30, 10, 5),
                "backup_test.zip"
        );

        String filename = "backup_test_manifest.zip";
        crearZipConManifest(filename, manifest, null);

        // WHEN
        BackupManifest resultado = sut.leerManifest(filename);

        // THEN
        assertThat(resultado.version()).isEqualTo("1.0");
        assertThat(resultado.empresaOrigen()).isEqualTo(empresaOrigen);
        assertThat(resultado.tipo()).isEqualTo(CopyProcessType.BACKUP);
    }

    // =========================================================================
    // Test 2 — leerEquivalencias con ZIP válido retorna lista
    // =========================================================================

    @Test
    @DisplayName("leerEquivalencias ZIP válido → retorna lista de CopyEquivalenceId (REQ-RESTORE-02)")
    void leerEquivalencias_zip_valido_retorna_lista() throws Exception {
        // GIVEN — lista de equivalencias y ZIP
        String idProceso = UUID.randomUUID().toString();
        CopyEquivalenceId eq1 = CopyEquivalenceId.crear(idProceso, "CATALOGUE", "tax", "1", "101");
        CopyEquivalenceId eq2 = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "product", "2", "102");

        String filename = "backup_test_equiv.zip";
        crearZipConManifestYEquivalencias(filename, crearManifestDefault(), List.of(eq1, eq2));

        // WHEN
        List<CopyEquivalenceId> resultado = sut.leerEquivalencias(filename);

        // THEN
        assertThat(resultado).hasSize(2);
        assertThat(resultado).anyMatch(eq ->
                "CATALOGUE".equals(eq.getModulo()) && "tax".equals(eq.getTabla())
                        && "1".equals(eq.getIdViejo()) && "101".equals(eq.getIdNuevo()));
    }

    // =========================================================================
    // Test 3 — archivo inexistente → BackupNotFoundException
    // =========================================================================

    @Test
    @DisplayName("leerManifest archivo no existe → BackupNotFoundException (REQ-RESTORE-01)")
    void leerManifest_archivo_no_existe_lanza_BackupNotFoundException() {
        // GIVEN — backupRef apunta a archivo que no existe
        String backupRef = "backup_no_existe.zip";

        // WHEN / THEN
        assertThatThrownBy(() -> sut.leerManifest(backupRef))
                .isInstanceOf(BackupNotFoundException.class)
                .hasMessageContaining(backupRef);
    }

    // =========================================================================
    // Test 4 — JSON corrupto en manifest → BackupCorruptedException
    // =========================================================================

    @Test
    @DisplayName("leerManifest JSON corrupto en manifest.json → BackupCorruptedException (ADR-45)")
    void leerManifest_json_corrupto_lanza_BackupCorruptedException() throws Exception {
        // GIVEN — ZIP con manifest.json que no es JSON válido
        String filename = "backup_corrupto.zip";
        crearZipConContenido(filename, "manifest.json", "esto no es json {{{");

        // WHEN / THEN
        assertThatThrownBy(() -> sut.leerManifest(filename))
                .isInstanceOf(BackupCorruptedException.class);
    }

    // =========================================================================
    // Test 5 — path traversal → BackupNotFoundException
    // =========================================================================

    @Test
    @DisplayName("backupRef con path traversal '../secrets.zip' → BackupNotFoundException (ADR-47)")
    void leerManifest_path_traversal_lanza_BackupNotFoundException() {
        // GIVEN — intentar escapar del directorio base
        String backupRef = "../secrets.zip";

        // WHEN / THEN — el adaptador detecta el traversal y lanza BackupNotFoundException
        assertThatThrownBy(() -> sut.leerManifest(backupRef))
                .isInstanceOf(BackupNotFoundException.class);
    }

    // =========================================================================
    // Helpers — construcción de ZIPs de prueba
    // =========================================================================

    /**
     * Crea un ZIP en {@code tempDir} con manifest.json solamente.
     */
    private void crearZipConManifest(String filename, BackupManifest manifest,
                                     @SuppressWarnings("unused") Object ignored) throws IOException {
        byte[] manifestJson = objectMapper.writeValueAsBytes(manifest);
        crearZipConEntradas(filename,
                new String[]{"manifest.json", "equivalencias.json", "phases.json"},
                new byte[][]{manifestJson, "[]".getBytes(StandardCharsets.UTF_8), "[]".getBytes(StandardCharsets.UTF_8)});
    }

    /**
     * Crea un ZIP con manifest.json y equivalencias.json.
     */
    private void crearZipConManifestYEquivalencias(String filename, BackupManifest manifest,
                                                   List<CopyEquivalenceId> equivalencias) throws IOException {
        byte[] manifestJson = objectMapper.writeValueAsBytes(manifest);
        byte[] equivJson = objectMapper.writeValueAsBytes(equivalencias);
        crearZipConEntradas(filename,
                new String[]{"manifest.json", "equivalencias.json", "phases.json"},
                new byte[][]{manifestJson, equivJson, "[]".getBytes(StandardCharsets.UTF_8)});
    }

    /**
     * Crea un ZIP con una única entrada de contenido arbitrario.
     */
    private void crearZipConContenido(String filename, String entryName, String contenido) throws IOException {
        crearZipConEntradas(filename,
                new String[]{entryName},
                new byte[][]{contenido.getBytes(StandardCharsets.UTF_8)});
    }

    /**
     * Crea un ZIP en {@code tempDir/{filename}} con las entradas y contenidos indicados.
     */
    private void crearZipConEntradas(String filename, String[] nombres, byte[][] contenidos) throws IOException {
        Path zipPath = tempDir.resolve(filename);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (int i = 0; i < nombres.length; i++) {
                zos.putNextEntry(new ZipEntry(nombres[i]));
                zos.write(contenidos[i]);
                zos.closeEntry();
            }
        }
        java.nio.file.Files.write(zipPath, baos.toByteArray());
    }

    private BackupManifest crearManifestDefault() {
        return new BackupManifest(
                "1.0",
                UUID.randomUUID().toString(),
                CopyProcessType.BACKUP,
                UUID.randomUUID(),
                null,
                "usuario",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                "backup_default.zip"
        );
    }
}
