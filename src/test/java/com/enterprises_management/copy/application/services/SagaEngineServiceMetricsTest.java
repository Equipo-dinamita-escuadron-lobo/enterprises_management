package com.enterprises_management.copy.application.services;

import com.enterprises_management.copy.application.output.IBackupSerializerPort;
import com.enterprises_management.copy.application.output.ICopyPhaseRepositoryPort;
import com.enterprises_management.copy.application.output.ITaxLiabilityRemapPort;
import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.application.output.IModuleExecutionRepositoryPort;
import com.enterprises_management.copy.application.output.IParticipantClientPort;
import com.enterprises_management.copy.application.output.IPhaseConfigRepositoryPort;
import com.enterprises_management.copy.application.output.IProcessEventPublisherPort;
import com.enterprises_management.copy.application.output.IProcessEventRepositoryPort;
import com.enterprises_management.copy.application.output.IProcessNotifierPort;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ModuleExecutionState;
import com.enterprises_management.copy.domain.enums.PhaseState;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyModuleExecution;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyPhaseConfig;
import com.enterprises_management.copy.domain.models.CopyProcess;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Verifica que SagaEngineService registra métricas correctamente en Micrometer
 * (REQ-METRICS-01, REQ-METRICS-04, REQ-METRICS-05).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SagaEngineService — registro de métricas Micrometer")
class SagaEngineServiceMetricsTest {

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
    @Mock private ITaxLiabilityRemapPort taxRemapPort;

    private SimpleMeterRegistry meterRegistry;
    private SagaEngineService sut;

    private static final int DEFAULT_RETRIES = 3;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        sut = new SagaEngineService(
                procesoRepo, faseRepo, moduloRepo, equivalenciaRepo, configRepo,
                eventoRepo, notifier, eventPublisher,
                List.of(participantClient),
                DEFAULT_RETRIES,
                backupSerializer,
                meterRegistry,
                false,
                taxRemapPort
        );
    }

    // =========================================================================
    // Test 1 — avanzarFase registra timer copy.saga.phase.advance.duration
    // =========================================================================

    @Test
    @DisplayName("avanzarFase registra timer copy.saga.phase.advance.duration con count > 0 (REQ-METRICS-01)")
    void avanzarFase_registra_timer_copy_saga_phase_advance_duration() {
        // GIVEN: proceso DUPLICATE con una fase que completa exitosamente
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig config = CopyPhaseConfig.crear(1, "CATALOGUE", 1, true, null);

        configurarMocksBasicos(proceso, fase1, config, true);

        // WHEN
        sut.avanzarFase(proceso.getId(), 1);

        // THEN — timer registrado con al menos 1 muestra
        var timer = meterRegistry.find("copy.saga.phase.advance.duration").timer();
        assertThat(timer)
                .withFailMessage("Se esperaba el timer 'copy.saga.phase.advance.duration' pero no fue registrado")
                .isNotNull();
        assertThat(timer.count())
                .withFailMessage("El timer debe tener al menos 1 registro, tuvo %d", timer.count())
                .isGreaterThan(0);
    }

    // =========================================================================
    // Test 2 — evaluarCompletacionProceso registra counter copy.saga.process.outcome
    // =========================================================================

    @Test
    @DisplayName("proceso COMPLETADO registra counter copy.saga.process.outcome (REQ-METRICS-05)")
    void evaluarCompletacionProceso_registra_counter_process_outcome() {
        // GIVEN: proceso que llega a COMPLETADO (todas las fases completadas)
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig config = CopyPhaseConfig.crear(1, "CATALOGUE", 1, true, null);

        configurarMocksBasicos(proceso, fase1, config, true);

        // WHEN
        sut.avanzarFase(proceso.getId(), 1);

        // THEN — counter copy.saga.process.outcome con tag estado=completado
        var counters = meterRegistry.find("copy.saga.process.outcome").counters();
        assertThat(counters)
                .withFailMessage("Se esperaban counters 'copy.saga.process.outcome' pero no se encontraron")
                .isNotEmpty();
    }

    // =========================================================================
    // Test 3 — evaluarCompletacionFase registra counter copy.saga.phase.outcome
    // =========================================================================

    @Test
    @DisplayName("fase COMPLETADA registra counter copy.saga.phase.outcome (REQ-METRICS-04)")
    void evaluarCompletacionFase_registra_counter_phase_outcome() {
        // GIVEN
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig config = CopyPhaseConfig.crear(1, "CATALOGUE", 1, true, null);

        configurarMocksBasicos(proceso, fase1, config, true);

        // WHEN
        sut.avanzarFase(proceso.getId(), 1);

        // THEN — counter copy.saga.phase.outcome registrado
        var counters = meterRegistry.find("copy.saga.phase.outcome").counters();
        assertThat(counters)
                .withFailMessage("Se esperaban counters 'copy.saga.phase.outcome' pero no se encontraron")
                .isNotEmpty();
    }

    // =========================================================================
    // Test 4 — al menos un meter con prefijo "copy."
    // =========================================================================

    @Test
    @DisplayName("después de una operación al menos un meter tiene prefijo 'copy.' (REQ-METRICS-01)")
    void meters_contienen_prefijo_copy() {
        // GIVEN
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig config = CopyPhaseConfig.crear(1, "CATALOGUE", 1, true, null);

        configurarMocksBasicos(proceso, fase1, config, true);

        // WHEN
        sut.avanzarFase(proceso.getId(), 1);

        // THEN — al menos un meter con prefijo "copy."
        boolean tieneMetricasCopy = meterRegistry.getMeters().stream()
                .anyMatch(m -> m.getId().getName().startsWith("copy."));
        assertThat(tieneMetricasCopy)
                .withFailMessage("No se encontraron métricas con prefijo 'copy.' en el registry")
                .isTrue();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CopyProcess crearProceso() {
        return CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "usuario-test");
    }

    private CopyPhase completedPhase(String idProceso, int numero, String nombre) {
        CopyPhase fase = CopyPhase.crear(idProceso, numero, nombre);
        fase.setEstado(PhaseState.COMPLETADA);
        return fase;
    }

    /**
     * Configura los mocks necesarios para que avanzarFase complete exitosamente.
     *
     * @param exitoso si true, el participante responde exitosamente y la fase se completa
     */
    private void configurarMocksBasicos(CopyProcess proceso, CopyPhase fase, CopyPhaseConfig config, boolean exitoso) {
        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), fase.getNumero())).thenReturn(Optional.of(fase));
        when(configRepo.buscarActivosPorFase(fase.getNumero())).thenReturn(List.of(config));
        when(moduloRepo.buscarPorClaveIdempotencia(anyString(), anyString(), eq(config.getModulo())))
                .thenReturn(Optional.empty());
        when(moduloRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(moduloRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantClient.getNombreModulo()).thenReturn(config.getModulo());

        if (exitoso) {
            when(participantClient.invoke(any(), any()))
                    .thenReturn(IParticipantClientPort.ParticipantResult.exito());
            CopyModuleExecution completado = CopyModuleExecution.crear(proceso.getId(), fase.getId(), config.getModulo());
            completado.setEstado(ModuleExecutionState.COMPLETADO);
            when(moduloRepo.buscarPorFase(fase.getId())).thenReturn(List.of(completado));
            when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(List.of(
                    completedPhase(proceso.getId(), 1, "BASE"),
                    completedPhase(proceso.getId(), 2, "PRODUCTOS"),
                    completedPhase(proceso.getId(), 3, "TRANSACCIONAL"),
                    completedPhase(proceso.getId(), 4, "POST")));
        }
    }
}
