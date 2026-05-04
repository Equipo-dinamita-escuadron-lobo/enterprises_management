package com.enterprises_management.copy.application.services;

import com.enterprises_management.copy.application.output.IBackupSerializerPort;
import com.enterprises_management.copy.application.output.ICopyPhaseRepositoryPort;
import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.application.output.IModuleExecutionRepositoryPort;
import com.enterprises_management.copy.application.output.IParticipantClientPort;
import com.enterprises_management.copy.application.output.IPhaseConfigRepositoryPort;
import com.enterprises_management.copy.application.output.IProcessEventPublisherPort;
import com.enterprises_management.copy.application.output.IProcessEventRepositoryPort;
import com.enterprises_management.copy.application.output.IProcessNotifierPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.PhaseState;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios del hook de serialización de backup en SagaEngineService
 * (REQ-BACKUP-01, REQ-BACKUP-03, ADR-44).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SagaEngineService — hook de serialización de backup")
class SagaEngineServiceBackupTest {

    @Mock private ICopyProcessRepositoryPort procesoRepo;
    @Mock private ICopyPhaseRepositoryPort faseRepo;
    @Mock private IModuleExecutionRepositoryPort moduloRepo;
    @Mock private IEquivalenceRepositoryPort equivalenciaRepo;
    @Mock private IPhaseConfigRepositoryPort configRepo;
    @Mock private IProcessEventRepositoryPort eventoRepo;
    @Mock private IProcessNotifierPort notifier;
    @Mock private IProcessEventPublisherPort eventPublisher;
    @Mock private IParticipantClientPort participantClient;
    @Mock private IBackupSerializerPort backupSerializer;

    private SagaEngineService sut;

    private static final int DEFAULT_RETRIES = 3;

    @BeforeEach
    void setUp() {
        sut = new SagaEngineService(
                procesoRepo, faseRepo, moduloRepo, equivalenciaRepo, configRepo,
                eventoRepo, notifier, eventPublisher,
                List.of(participantClient),
                DEFAULT_RETRIES,
                backupSerializer,
                new SimpleMeterRegistry(),
                false
        );
    }

    // =========================================================================
    // Test 1 — tipo BACKUP llama al serializador y persiste backupRef
    // =========================================================================

    @Test
    @DisplayName("tipo BACKUP completa → serializador llamado una vez → backupRef persistido (REQ-BACKUP-01)")
    void tipo_BACKUP_llama_serializador_y_persiste_backupRef() {
        // GIVEN: proceso BACKUP en estado EN_PROCESO con las 4 fases COMPLETADAS
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.BACKUP, UUID.randomUUID(), null, null, "usuario-test");
        proceso.setEstado(ProcessState.EN_PROCESO);
        String backupRefEsperado = "backup_origin_proc_20260430-120000.zip";

        configurarFasesCompletadas(proceso, 4);
        when(equivalenciaRepo.buscarConFiltros(anyString(), any(), any(), any()))
                .thenReturn(List.of());
        when(backupSerializer.serializarBackup(any(), any(), any()))
                .thenReturn(backupRefEsperado);
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        invocarEvaluarCompletacion(proceso);

        // THEN — serializador invocado exactamente una vez
        verify(backupSerializer, times(1)).serializarBackup(any(), any(), any());

        // THEN — procesoRepo.actualizar llamado con backupRef seteado
        ArgumentCaptor<CopyProcess> captor = ArgumentCaptor.forClass(CopyProcess.class);
        verify(procesoRepo, atLeastOnce()).actualizar(captor.capture());
        List<CopyProcess> actualizaciones = captor.getAllValues();
        assertThat(actualizaciones)
                .anyMatch(p -> backupRefEsperado.equals(p.getBackupRef()));
    }

    // =========================================================================
    // Test 2 — tipo DUPLICATE con generateBackup=true llama al serializador
    // =========================================================================

    @Test
    @DisplayName("tipo DUPLICATE + generateBackup=true → serializador llamado (REQ-BACKUP-01)")
    void tipo_DUPLICATE_con_generateBackup_llama_serializador() {
        // GIVEN: proceso DUPLICATE con generateBackup=true
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Empresa Destino", null, "usuario-test");
        proceso.setEstado(ProcessState.EN_PROCESO);
        proceso.setGenerateBackup(true);

        configurarFasesCompletadas(proceso, 4);
        when(equivalenciaRepo.buscarConFiltros(anyString(), any(), any(), any()))
                .thenReturn(List.of());
        when(backupSerializer.serializarBackup(any(), any(), any()))
                .thenReturn("backup_test.zip");
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        invocarEvaluarCompletacion(proceso);

        // THEN — serializador invocado porque generateBackup=true
        verify(backupSerializer, times(1)).serializarBackup(any(), any(), any());
    }

    // =========================================================================
    // Test 3 — tipo DUPLICATE con generateBackup=false NO llama al serializador
    // =========================================================================

    @Test
    @DisplayName("tipo DUPLICATE + generateBackup=false → serializador NO llamado (REQ-BACKUP-01)")
    void tipo_DUPLICATE_sin_generateBackup_no_llama_serializador() {
        // GIVEN: proceso DUPLICATE con generateBackup=false (valor por defecto)
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Empresa Destino", null, "usuario-test");
        proceso.setEstado(ProcessState.EN_PROCESO);
        // generateBackup permanece false (valor por defecto del dominio)
        assertThat(proceso.isGenerateBackup()).isFalse();

        configurarFasesCompletadas(proceso, 4);
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        invocarEvaluarCompletacion(proceso);

        // THEN — serializador jamás invocado para DUPLICATE sin generateBackup
        verify(backupSerializer, never()).serializarBackup(any(), any(), any());
    }

    // =========================================================================
    // Test 4 — excepción en el serializador no rompe el estado COMPLETADO
    // =========================================================================

    @Test
    @DisplayName("excepción en serializador → proceso queda COMPLETADO con backupRef=null (ADR-44)")
    void serializador_lanza_excepcion_proceso_queda_COMPLETADO() {
        // GIVEN: proceso BACKUP donde el serializador falla
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.BACKUP, UUID.randomUUID(), null, null, "usuario-test");
        proceso.setEstado(ProcessState.EN_PROCESO);

        configurarFasesCompletadas(proceso, 4);
        when(equivalenciaRepo.buscarConFiltros(anyString(), any(), any(), any()))
                .thenReturn(List.of());
        when(backupSerializer.serializarBackup(any(), any(), any()))
                .thenThrow(new RuntimeException("Error de disco simulado"));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN — no debe lanzar excepción
        invocarEvaluarCompletacion(proceso);

        // THEN — proceso está COMPLETADO (la primera actualización lo marca COMPLETADO)
        ArgumentCaptor<CopyProcess> captor = ArgumentCaptor.forClass(CopyProcess.class);
        verify(procesoRepo, atLeastOnce()).actualizar(captor.capture());
        assertThat(captor.getAllValues())
                .anyMatch(p -> p.getEstado() == ProcessState.COMPLETADO);

        // THEN — backupRef sigue siendo null (falló la serialización)
        // La última actualización del proceso NO debe incluir backupRef
        // (la segunda actualización solo ocurre si serializarBackup tiene éxito)
        assertThat(proceso.getBackupRef()).isNull();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Configura el repo de fases para devolver {@code cantidadFases} fases COMPLETADAS.
     * Simula el estado del proceso justo antes de que evaluarCompletacionProceso actúe.
     */
    private void configurarFasesCompletadas(CopyProcess proceso, int cantidadFases) {
        List<CopyPhase> fases = new java.util.ArrayList<>();
        for (int i = 1; i <= cantidadFases; i++) {
            CopyPhase fase = CopyPhase.crear(proceso.getId(), i, "FASE_" + i);
            fase.setEstado(PhaseState.COMPLETADA);
            fases.add(fase);
        }
        when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(fases);
    }

    /**
     * Invoca avanzarFase(id, 4) con fase 4 sin módulos activos para que la saga
     * llegue internamente a evaluarCompletacionProceso.
     * <p>
     * La fase 4 se configura PENDIENTE (necesario para que la saga la transite a EN_PROCESO).
     * El proceso debe estar en EN_PROCESO para que la saga no lo transite desde PENDIENTE.
     */
    private void invocarEvaluarCompletacion(CopyProcess proceso) {
        // La fase 4 empieza PENDIENTE (la saga la transitará a EN_PROCESO, luego COMPLETADA)
        CopyPhase fase4 = CopyPhase.crear(proceso.getId(), 4, "FASE_4");

        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), 4)).thenReturn(Optional.of(fase4));
        when(faseRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        // Sin módulos activos → la saga omite el loop de módulos y evalúa completación
        when(configRepo.buscarActivosPorFase(4)).thenReturn(List.of());
        // buscarPorFase retorna lista vacía (no hay ejecuciones previas de módulos)
        when(moduloRepo.buscarPorFase(anyString())).thenReturn(List.of());

        sut.avanzarFase(proceso.getId(), 4);
    }
}
