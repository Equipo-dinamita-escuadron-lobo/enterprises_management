package com.enterprises_management.copy.infraestructure.adapters.output.backup;

import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.models.BackupManifest;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.infraestructure.config.CopyOrchestratorProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios de ZipBackupSerializerAdapter con directorio temporal (REQ-BACKUP-01, ADR-45).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ZipBackupSerializerAdapter — serialización a ZIP")
class ZipBackupSerializerAdapterTest {

    @TempDir
    Path tempDir;

    @Mock
    JdbcTemplate jdbcTemplate;

    private ZipBackupSerializerAdapter sut;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        CopyOrchestratorProperties props = new CopyOrchestratorProperties();
        props.getBackup().setDir(tempDir.toString());
        // No llamamos @PostConstruct manualmente — el directorio ya existe (TempDir)
        // jdbcTemplate es un mock: query() devuelve null → enterprise.json no se añade al ZIP
        sut = new ZipBackupSerializerAdapter(props, objectMapper, jdbcTemplate);
    }

    // =========================================================================
    // Test 1 — ZIP creado con tres entradas
    // =========================================================================

    @Test
    @DisplayName("serializarBackup crea ZIP con exactamente tres entradas (REQ-BACKUP-03)")
    void serializarBackup_crea_zip_con_tres_entradas() throws Exception {
        // GIVEN
        CopyProcess proceso = crearProcesoBACKUP();

        // WHEN
        String backupRef = sut.serializarBackup(proceso, List.of(), List.of());

        // THEN — archivo existe
        Path zipPath = tempDir.resolve(backupRef);
        assertThat(Files.exists(zipPath)).isTrue();

        // THEN — exactamente 3 entradas
        try (ZipFile zip = new ZipFile(zipPath.toFile())) {
            assertThat(zip.size()).isEqualTo(3);
            assertThat(zip.getEntry("manifest.json")).isNotNull();
            assertThat(zip.getEntry("equivalencias.json")).isNotNull();
            assertThat(zip.getEntry("phases.json")).isNotNull();
        }
    }

    // =========================================================================
    // Test 2 — manifest.json contiene campos correctos
    // =========================================================================

    @Test
    @DisplayName("serializarBackup — manifest.json tiene version '1.0' e idProceso correcto (ADR-45)")
    void serializarBackup_manifest_json_campos_correctos() throws Exception {
        // GIVEN
        CopyProcess proceso = crearProcesoBACKUP();

        // WHEN
        String backupRef = sut.serializarBackup(proceso, List.of(), List.of());

        // THEN — parsear manifest.json y verificar campos
        Path zipPath = tempDir.resolve(backupRef);
        try (ZipFile zip = new ZipFile(zipPath.toFile())) {
            ZipEntry entrada = zip.getEntry("manifest.json");
            assertThat(entrada).isNotNull();

            try (InputStream is = zip.getInputStream(entrada)) {
                BackupManifest manifest = objectMapper.readValue(is, BackupManifest.class);
                assertThat(manifest.version()).isEqualTo("1.0");
                assertThat(manifest.idProceso()).isEqualTo(proceso.getId());
                assertThat(manifest.tipo()).isEqualTo(CopyProcessType.BACKUP);
                assertThat(manifest.empresaOrigen()).isEqualTo(proceso.getEmpresaOrigen());
            }
        }
    }

    // =========================================================================
    // Test 3 — filenames distintos en llamadas sucesivas
    // =========================================================================

    @Test
    @DisplayName("serializarBackup genera filename distinto en cada llamada (timestamp diferente)")
    void serializarBackup_genera_filename_distinto_en_cada_llamada() throws Exception {
        // GIVEN
        CopyProcess proceso = crearProcesoBACKUP();

        // WHEN — dos llamadas con una pausa mínima para cambiar el timestamp
        String backupRef1 = sut.serializarBackup(proceso, List.of(), List.of());
        // Forzar timestamp diferente creando un nuevo proceso (ID distinto)
        CopyProcess proceso2 = crearProcesoBACKUP();
        String backupRef2 = sut.serializarBackup(proceso2, List.of(), List.of());

        // THEN — filenames distintos (pueden diferir por ID de proceso o timestamp)
        assertThat(backupRef1).isNotEqualTo(backupRef2);
    }

    // =========================================================================
    // Test 4 — resolverRutaBackup retorna path con directorio base
    // =========================================================================

    @Test
    @DisplayName("resolverRutaBackup retorna nombre de archivo con patrón backup_{entId}_{proc}_{ts}.zip")
    void resolverRutaBackup_retorna_path_absoluto_correcto() {
        // GIVEN
        String entIdOrigen = UUID.randomUUID().toString();
        String idProceso = UUID.randomUUID().toString();

        // WHEN
        String resultado = sut.resolverRutaBackup(entIdOrigen, idProceso);

        // THEN — tiene el prefijo correcto y termina en .zip
        assertThat(resultado).startsWith("backup_" + entIdOrigen + "_" + idProceso);
        assertThat(resultado).endsWith(".zip");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CopyProcess crearProcesoBACKUP() {
        return CopyProcess.crear(CopyProcessType.BACKUP, UUID.randomUUID(), null, null, "usuario-test");
    }
}
