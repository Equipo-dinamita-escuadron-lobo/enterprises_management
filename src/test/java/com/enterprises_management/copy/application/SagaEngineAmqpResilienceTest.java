package com.enterprises_management.copy.application;

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
import com.enterprises_management.copy.application.services.SagaEngineService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ModuleExecutionState;
import com.enterprises_management.copy.domain.enums.PhaseState;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyModuleExecution;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyPhaseConfig;
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
 * Tests de resiliencia: un fallo AMQP durante publicación de eventos NO debe
 * bloquear ni revertir las transiciones de estado del proceso (REQ-EVENT-02, ADR-23).
 *
 * <p>Verifica que el SagaEngineService con IProcessEventPublisherPort inyectado
 * funciona correctamente incluso cuando el publisher AMQP lanza excepción.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SagaEngineService — resiliencia ante fallo AMQP (REQ-EVENT-02)")
class SagaEngineAmqpResilienceTest {

    @Mock private ICopyProcessRepositoryPort procesoRepo;
    @Mock private ICopyPhaseRepositoryPort faseRepo;
    @Mock private IModuleExecutionRepositoryPort moduloRepo;
    @Mock private IEquivalenceRepositoryPort equivalenciaRepo;
    @Mock private IPhaseConfigRepositoryPort configRepo;
    @Mock private IProcessEventRepositoryPort eventoRepo;
    @Mock private IProcessNotifierPort notifier;
    @Mock private IParticipantClientPort participantClient;
    @Mock private IProcessEventPublisherPort eventPublisher;
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

    /**
     * REQ-EVENT-02: fallo AMQP NO bloquea la transición de fase.
     * El proceso debe quedar COMPLETADO incluso si el publisher lanza RuntimeException.
     */
    @Test
    @DisplayName("fallo AMQP durante publicación → fase igualmente pasa a COMPLETADA en BD (REQ-EVENT-02)")
    void avanzarFase_falloAmqp_faseCompletadaEnBd() {
        // GIVEN: publisher AMQP que siempre lanza excepción
        doThrow(new RuntimeException("RabbitMQ no disponible"))
                .when(eventPublisher).publicar(any());

        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig config = CopyPhaseConfig.crear(1, "CATALOGUE", 1, true, null);

        configurarMocksBasicos(proceso, fase1, config);
        when(participantClient.invoke(any(), any()))
                .thenReturn(IParticipantClientPort.ParticipantResult.exito());

        CopyModuleExecution completado = CopyModuleExecution.crear(proceso.getId(), fase1.getId(), "CATALOGUE");
        completado.setEstado(ModuleExecutionState.COMPLETADO);
        when(moduloRepo.buscarPorFase(fase1.getId())).thenReturn(List.of(completado));
        when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(List.of(
                completedPhase(proceso.getId(), 1, "BASE")));

        // WHEN — no debe lanzar excepción a pesar del fallo AMQP
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> sut.avanzarFase(proceso.getId(), 1));

        // THEN — fase igualmente fue actualizada en BD
        ArgumentCaptor<CopyPhase> faseCaptor = ArgumentCaptor.forClass(CopyPhase.class);
        verify(faseRepo, atLeastOnce()).actualizar(faseCaptor.capture());
        assertThat(faseCaptor.getAllValues().stream()
                .anyMatch(f -> f.getEstado() == PhaseState.COMPLETADA))
                .isTrue();
    }

    /**
     * REQ-EVENT-02: fallo AMQP → proceso igualmente marca COMPLETADO en BD.
     */
    @Test
    @DisplayName("fallo AMQP durante publicación → proceso igualmente pasa a COMPLETADO en BD (REQ-EVENT-02)")
    void avanzarFase_falloAmqp_procesoCompletadoEnBd() {
        doThrow(new RuntimeException("broker no disponible"))
                .when(eventPublisher).publicar(any());

        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        proceso.setFaseActual(4);
        CopyPhase fase4 = CopyPhase.crear(proceso.getId(), 4, "CIERRE");
        fase4.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig config = CopyPhaseConfig.crear(4, "PRODUCTS", 1, true, null);

        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), 4)).thenReturn(Optional.of(fase4));
        when(configRepo.buscarActivosPorFase(4)).thenReturn(List.of(config));
        when(moduloRepo.buscarPorClaveIdempotencia(anyString(), anyString(), eq("PRODUCTS")))
                .thenReturn(Optional.empty());
        when(moduloRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(moduloRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantClient.getNombreModulo()).thenReturn("PRODUCTS");
        when(participantClient.invoke(any(), any()))
                .thenReturn(IParticipantClientPort.ParticipantResult.exito());

        CopyModuleExecution completado = CopyModuleExecution.crear(proceso.getId(), fase4.getId(), "PRODUCTS");
        completado.setEstado(ModuleExecutionState.COMPLETADO);
        when(moduloRepo.buscarPorFase(fase4.getId())).thenReturn(List.of(completado));
        when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(List.of(
                completedPhase(proceso.getId(), 1, "BASE"),
                completedPhase(proceso.getId(), 2, "INTERNAS"),
                completedPhase(proceso.getId(), 3, "EXTERNAS"),
                completedPhase(proceso.getId(), 4, "CIERRE")));

        // WHEN
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> sut.avanzarFase(proceso.getId(), 4));

        // THEN — proceso actualizado a COMPLETADO a pesar del fallo AMQP
        ArgumentCaptor<CopyProcess> captor = ArgumentCaptor.forClass(CopyProcess.class);
        verify(procesoRepo, atLeastOnce()).actualizar(captor.capture());
        assertThat(captor.getAllValues().stream()
                .anyMatch(p -> p.getEstado() == ProcessState.COMPLETADO))
                .isTrue();
    }

    /**
     * REQ-EVENT-01: cuando AMQP funciona, el publisher es invocado en las transiciones.
     */
    @Test
    @DisplayName("cuando AMQP funciona, eventPublisher.publicar es invocado en transiciones (REQ-EVENT-01)")
    void avanzarFase_amqpFuncional_publisherInvocado() {
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig config = CopyPhaseConfig.crear(1, "CATALOGUE", 1, true, null);

        configurarMocksBasicos(proceso, fase1, config);
        when(participantClient.invoke(any(), any()))
                .thenReturn(IParticipantClientPort.ParticipantResult.exito());

        CopyModuleExecution completado = CopyModuleExecution.crear(proceso.getId(), fase1.getId(), "CATALOGUE");
        completado.setEstado(ModuleExecutionState.COMPLETADO);
        when(moduloRepo.buscarPorFase(fase1.getId())).thenReturn(List.of(completado));
        when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(List.of(
                completedPhase(proceso.getId(), 1, "BASE")));

        sut.avanzarFase(proceso.getId(), 1);

        // El publisher AMQP debe haber sido invocado al menos una vez
        verify(eventPublisher, atLeastOnce()).publicar(any());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void configurarMocksBasicos(CopyProcess proceso, CopyPhase fase, CopyPhaseConfig config) {
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
    }

    private CopyProcess crearProceso() {
        return CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "sub-test");
    }

    private CopyPhase completedPhase(String idProceso, int numero, String nombre) {
        CopyPhase fase = CopyPhase.crear(idProceso, numero, nombre);
        fase.setEstado(PhaseState.COMPLETADA);
        return fase;
    }
}
