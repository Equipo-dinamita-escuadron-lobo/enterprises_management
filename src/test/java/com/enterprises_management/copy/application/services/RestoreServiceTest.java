package com.enterprises_management.copy.application.services;

import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.input.command.RestoreCommand;
import com.enterprises_management.copy.application.output.IBackupReaderPort;
import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.exceptions.BackupCorruptedException;
import com.enterprises_management.copy.domain.exceptions.BackupNotFoundException;
import com.enterprises_management.copy.domain.models.BackupManifest;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios de RestoreService (REQ-RESTORE-01, REQ-RESTORE-02, REQ-RESTORE-03, ADR-46).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RestoreService — lógica de restauración desde backup")
class RestoreServiceTest {

    @Mock private IBackupReaderPort backupReader;
    @Mock private ICopyProcessStartPort startPort;
    @Mock private IEquivalenceRepositoryPort equivalenciaRepo;

    private RestoreService sut;

    @BeforeEach
    void setUp() {
        sut = new RestoreService(backupReader, startPort, equivalenciaRepo);
    }

    // =========================================================================
    // Test 1 — Happy path: manifest válido, equivalencias sembradas
    // =========================================================================

    @Test
    @DisplayName("happy path — manifest válido, proceso RESTORE creado y equivalencias sembradas (REQ-RESTORE-01)")
    void happyPath_manifest_valido_siembra_equivalencias() {
        // GIVEN
        String backupRef = "backup_empresa-1_proc-abc_20260430-120000.zip";
        String empresaDestino = "empresa-nueva-uuid";
        UUID empresaOrigen = UUID.randomUUID();

        BackupManifest manifest = crearManifest("1.0", empresaOrigen, "empresa-destino-original");
        CopyEquivalenceId eq1 = CopyEquivalenceId.crear("proc-abc", "CATALOGUE", "tax", "100", "200");
        CopyEquivalenceId eq2 = CopyEquivalenceId.crear("proc-abc", "PRODUCTS", "product", "50", "150");

        CopyProcess nuevoProceso = CopyProcess.crear(
                CopyProcessType.RESTORE, empresaOrigen, empresaDestino, backupRef, "usuario");

        when(backupReader.leerManifest(backupRef)).thenReturn(manifest);
        when(backupReader.leerEquivalencias(backupRef)).thenReturn(List.of(eq1, eq2));
        when(startPort.iniciar(any(IniciarProcesoCommand.class))).thenReturn(nuevoProceso);
        when(equivalenciaRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        CopyProcess resultado = sut.iniciarRestore(
                new RestoreCommand(backupRef, empresaDestino, "usuario"));

        // THEN — proceso devuelto correctamente
        assertThat(resultado).isNotNull();
        assertThat(resultado.getTipo()).isEqualTo(CopyProcessType.RESTORE);

        // THEN — equivalencias sembradas con el NUEVO idProceso
        ArgumentCaptor<CopyEquivalenceId> eqCaptor = ArgumentCaptor.forClass(CopyEquivalenceId.class);
        verify(equivalenciaRepo, times(2)).guardar(eqCaptor.capture());

        List<CopyEquivalenceId> sembradas = eqCaptor.getAllValues();
        // Todas deben tener el nuevo idProceso (no el de proc-abc del ZIP)
        String nuevoId = nuevoProceso.getId();
        assertThat(sembradas).allMatch(eq -> nuevoId.equals(eq.getIdProceso()));
        // Los datos originales se preservan
        assertThat(sembradas).anyMatch(eq ->
                "CATALOGUE".equals(eq.getModulo()) && "tax".equals(eq.getTabla())
                        && "100".equals(eq.getIdViejo()) && "200".equals(eq.getIdNuevo()));
        assertThat(sembradas).anyMatch(eq ->
                "PRODUCTS".equals(eq.getModulo()) && "product".equals(eq.getTabla()));
    }

    // =========================================================================
    // Test 2 — misma empresa origen → IllegalArgumentException (REQ-RESTORE-03)
    // =========================================================================

    @Test
    @DisplayName("empresaDestino == empresaOrigen → IllegalArgumentException (REQ-RESTORE-03)")
    void mismo_origen_que_destino_lanza_IllegalArgumentException() {
        // GIVEN
        UUID empresaOrigen = UUID.randomUUID();
        String backupRef = "backup_test.zip";
        // empresaDestino == empresaOrigen.toString()
        String empresaDestino = empresaOrigen.toString();

        BackupManifest manifest = crearManifest("1.0", empresaOrigen, "empresa-original-distinta");
        when(backupReader.leerManifest(backupRef)).thenReturn(manifest);

        // WHEN / THEN
        assertThatThrownBy(() ->
                sut.iniciarRestore(new RestoreCommand(backupRef, empresaDestino, "usuario")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empresa origen");

        // No se crea proceso ni se siembran equivalencias
        verify(startPort, never()).iniciar(any());
        verify(equivalenciaRepo, never()).guardar(any());
    }

    // =========================================================================
    // Test 3 — versión de backup no soportada → BackupCorruptedException (ADR-45)
    // =========================================================================

    @Test
    @DisplayName("versión de backup != '1.0' → BackupCorruptedException (ADR-45)")
    void version_no_soportada_lanza_BackupCorruptedException() {
        // GIVEN
        String backupRef = "backup_v2_test.zip";
        UUID empresaOrigen = UUID.randomUUID();

        BackupManifest manifest = crearManifest("2.0", empresaOrigen, null);
        when(backupReader.leerManifest(backupRef)).thenReturn(manifest);

        // WHEN / THEN
        assertThatThrownBy(() ->
                sut.iniciarRestore(new RestoreCommand(backupRef, "empresa-nueva", "usuario")))
                .isInstanceOf(BackupCorruptedException.class)
                .hasMessageContaining("versión");

        verify(startPort, never()).iniciar(any());
    }

    // =========================================================================
    // Test 4 — BackupNotFoundException del reader se propaga (REQ-RESTORE-01)
    // =========================================================================

    @Test
    @DisplayName("leerManifest lanza BackupNotFoundException → se propaga sin envolver (REQ-RESTORE-01)")
    void backupNotFoundException_del_reader_se_propaga() {
        // GIVEN
        String backupRef = "backup_inexistente.zip";
        when(backupReader.leerManifest(backupRef))
                .thenThrow(new BackupNotFoundException(backupRef));

        // WHEN / THEN
        assertThatThrownBy(() ->
                sut.iniciarRestore(new RestoreCommand(backupRef, "empresa-nueva", "usuario")))
                .isInstanceOf(BackupNotFoundException.class);

        verify(startPort, never()).iniciar(any());
        verify(equivalenciaRepo, never()).guardar(any());
    }

    // =========================================================================
    // Test 5 — count de equivalencias sembradas coincide con las del ZIP
    // =========================================================================

    @Test
    @DisplayName("count equivalencias sembradas == count equivalencias en el ZIP (REQ-RESTORE-02)")
    void count_equivalencias_sembradas_coincide() {
        // GIVEN
        String backupRef = "backup_test.zip";
        UUID empresaOrigen = UUID.randomUUID();
        String idProceso = UUID.randomUUID().toString();

        BackupManifest manifest = crearManifest("1.0", empresaOrigen, "empresa-original");
        List<CopyEquivalenceId> equivalencias = List.of(
                CopyEquivalenceId.crear(idProceso, "CATALOGUE", "tax", "1", "101"),
                CopyEquivalenceId.crear(idProceso, "PRODUCTS", "product", "2", "102"),
                CopyEquivalenceId.crear(idProceso, "THIRDS", "third", "3", "103")
        );

        CopyProcess nuevoProceso = CopyProcess.crear(
                CopyProcessType.RESTORE, empresaOrigen, "empresa-nueva", backupRef, "usuario");

        when(backupReader.leerManifest(backupRef)).thenReturn(manifest);
        when(backupReader.leerEquivalencias(backupRef)).thenReturn(equivalencias);
        when(startPort.iniciar(any())).thenReturn(nuevoProceso);
        when(equivalenciaRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        sut.iniciarRestore(new RestoreCommand(backupRef, "empresa-nueva", "usuario"));

        // THEN — se sembró exactamente la misma cantidad
        verify(equivalenciaRepo, times(equivalencias.size())).guardar(any());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private BackupManifest crearManifest(String version, UUID empresaOrigen, String empresaDestinoOriginal) {
        return new BackupManifest(
                version,
                UUID.randomUUID().toString(),
                CopyProcessType.BACKUP,
                empresaOrigen,
                empresaDestinoOriginal,
                "usuario-backup",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                "backup_test.zip"
        );
    }
}
