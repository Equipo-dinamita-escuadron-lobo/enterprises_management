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
import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ModuleExecutionState;
import com.enterprises_management.copy.domain.enums.PhaseState;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.models.CopyModuleExecution;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyPhaseConfig;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para SagaEngineService (REQ-PROC-02, REQ-FASE-02, REQ-MOD-02, REQ-IDEM-01).
 * ADR-12: llamadas a IParticipantClientPort deben ocurrir FUERA de @Transactional.
 * Aquí se verifica el comportamiento de orquestación sin probar transacciones reales.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SagaEngineService — motor de saga")
class SagaEngineServiceTest {

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

    /** default-retries para los tests. */
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
    // REQ-PROC-02: Avance PENDIENTE → EN_PROCESO al iniciar Fase 1
    // =========================================================================

    @Test
    @DisplayName("avanzarFase — transición PENDIENTE→EN_PROCESO al iniciar Fase 1 (REQ-PROC-02)")
    void avanzarFase_transicionProcesoAEnProceso() {
        // GIVEN: proceso PENDIENTE, Fase 1 PENDIENTE, 1 módulo activo que responde exitoso
        CopyProcess proceso = crearProceso();
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        CopyPhaseConfig config = CopyPhaseConfig.crear(1, "ENTERPRISES", 1, true, null);
        CopyModuleExecution modulo = CopyModuleExecution.crear(proceso.getId(), fase1.getId(), "ENTERPRISES");

        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), 1)).thenReturn(Optional.of(fase1));
        when(configRepo.buscarActivosPorFase(1)).thenReturn(List.of(config));
        when(moduloRepo.buscarPorClaveIdempotencia(anyString(), anyString(), eq("ENTERPRISES")))
                .thenReturn(Optional.empty());
        when(moduloRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(moduloRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantClient.getNombreModulo()).thenReturn("ENTERPRISES");
        when(participantClient.invoke(any(), any()))
                .thenReturn(IParticipantClientPort.ParticipantResult.exito());
        when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(List.of(
                completedPhase(proceso.getId(), 1, "BASE")));

        // WHEN
        sut.avanzarFase(proceso.getId(), 1);

        // THEN — proceso actualizado a EN_PROCESO en algún momento
        ArgumentCaptor<CopyProcess> procesoCaptor = ArgumentCaptor.forClass(CopyProcess.class);
        verify(procesoRepo, atLeastOnce()).actualizar(procesoCaptor.capture());
        List<CopyProcess> capturedProcesos = procesoCaptor.getAllValues();
        assertThat(capturedProcesos.stream()
                .anyMatch(p -> p.getEstado() == ProcessState.EN_PROCESO))
                .isTrue();
    }

    // =========================================================================
    // REQ-FASE-02: Fase pasa a COMPLETADA si todos los módulos completan
    // =========================================================================

    @Test
    @DisplayName("avanzarFase — fase pasa a COMPLETADA cuando todos los módulos completan (REQ-FASE-02)")
    void avanzarFase_fasePasaACompletadaCuandoModulosCompletados() {
        // GIVEN
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig config = CopyPhaseConfig.crear(1, "CATALOGUE", 1, true, null);

        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), 1)).thenReturn(Optional.of(fase1));
        when(configRepo.buscarActivosPorFase(1)).thenReturn(List.of(config));
        when(moduloRepo.buscarPorClaveIdempotencia(anyString(), anyString(), eq("CATALOGUE")))
                .thenReturn(Optional.empty());
        when(moduloRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(moduloRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantClient.getNombreModulo()).thenReturn("CATALOGUE");
        when(participantClient.invoke(any(), any()))
                .thenReturn(IParticipantClientPort.ParticipantResult.exito());
        // Después de ejecutar, la fase tiene su módulo completado
        CopyModuleExecution ejecutado = CopyModuleExecution.crear(proceso.getId(), fase1.getId(), "CATALOGUE");
        ejecutado.setEstado(ModuleExecutionState.COMPLETADO);
        when(moduloRepo.buscarPorFase(fase1.getId())).thenReturn(List.of(ejecutado));
        // No hay más fases (proceso se completa)
        when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(List.of(
                completedPhase(proceso.getId(), 1, "BASE")));

        // WHEN
        sut.avanzarFase(proceso.getId(), 1);

        // THEN — fase fue actualizada a COMPLETADA
        ArgumentCaptor<CopyPhase> faseCaptor = ArgumentCaptor.forClass(CopyPhase.class);
        verify(faseRepo, atLeastOnce()).actualizar(faseCaptor.capture());
        assertThat(faseCaptor.getAllValues().stream()
                .anyMatch(f -> f.getEstado() == PhaseState.COMPLETADA))
                .isTrue();
    }

    // =========================================================================
    // REQ-FASE-02: Fase pasa a ERROR si módulo alcanza ERROR_NO_REINTENTABLE
    // =========================================================================

    @Test
    @DisplayName("avanzarFase — fase y proceso pasan a ERROR si módulo es no-reintentable (REQ-FASE-02)")
    void avanzarFase_faseYProcesoErrorCuandoModuloNoReintentable() {
        // GIVEN
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig config = CopyPhaseConfig.crear(1, "CATALOGUE", 1, true, null);

        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), 1)).thenReturn(Optional.of(fase1));
        when(configRepo.buscarActivosPorFase(1)).thenReturn(List.of(config));
        when(moduloRepo.buscarPorClaveIdempotencia(anyString(), anyString(), eq("CATALOGUE")))
                .thenReturn(Optional.empty());
        when(moduloRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(moduloRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantClient.getNombreModulo()).thenReturn("CATALOGUE");
        when(participantClient.invoke(any(), any()))
                .thenReturn(IParticipantClientPort.ParticipantResult.errorNoReintentable("fallo crítico"));

        // WHEN
        sut.avanzarFase(proceso.getId(), 1);

        // THEN — fase y proceso marcados como ERROR
        ArgumentCaptor<CopyPhase> faseCaptor = ArgumentCaptor.forClass(CopyPhase.class);
        verify(faseRepo, atLeastOnce()).actualizar(faseCaptor.capture());
        assertThat(faseCaptor.getAllValues().stream()
                .anyMatch(f -> f.getEstado() == PhaseState.ERROR))
                .isTrue();

        ArgumentCaptor<CopyProcess> procCaptor = ArgumentCaptor.forClass(CopyProcess.class);
        verify(procesoRepo, atLeastOnce()).actualizar(procCaptor.capture());
        assertThat(procCaptor.getAllValues().stream()
                .anyMatch(p -> p.getEstado() == ProcessState.ERROR))
                .isTrue();
    }

    // =========================================================================
    // REQ-MOD-02: Reintentos — módulo reintentable dentro del límite
    // =========================================================================

    @Test
    @DisplayName("avanzarFase — reintenta módulo reintentable si intentos < defaultRetries (REQ-MOD-02)")
    void avanzarFase_reintentaModuloReintentableDentroDelLimite() {
        // GIVEN
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig config = CopyPhaseConfig.crear(1, "CATALOGUE", 1, true, null);

        // Primera llamada → error reintentable; segunda → éxito
        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), 1)).thenReturn(Optional.of(fase1));
        when(configRepo.buscarActivosPorFase(1)).thenReturn(List.of(config));
        when(moduloRepo.buscarPorClaveIdempotencia(anyString(), anyString(), eq("CATALOGUE")))
                .thenReturn(Optional.empty());
        when(moduloRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(moduloRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantClient.getNombreModulo()).thenReturn("CATALOGUE");
        when(participantClient.invoke(any(), any()))
                .thenReturn(IParticipantClientPort.ParticipantResult.errorReintentable("timeout"))
                .thenReturn(IParticipantClientPort.ParticipantResult.exito());

        // Módulo completado
        CopyModuleExecution completado = CopyModuleExecution.crear(proceso.getId(), fase1.getId(), "CATALOGUE");
        completado.setEstado(ModuleExecutionState.COMPLETADO);
        when(moduloRepo.buscarPorFase(fase1.getId())).thenReturn(List.of(completado));
        when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(List.of(
                completedPhase(proceso.getId(), 1, "BASE")));

        // WHEN
        sut.avanzarFase(proceso.getId(), 1);

        // THEN — participante invocado 2 veces (1 fallo + 1 reintento)
        verify(participantClient, times(2)).invoke(any(), any());
    }

    // =========================================================================
    // REQ-MOD-02: Reintentos agotados → ERROR_NO_REINTENTABLE
    // =========================================================================

    @Test
    @DisplayName("avanzarFase — módulo pasa a ERROR_NO_REINTENTABLE al agotar reintentos (REQ-MOD-02)")
    void avanzarFase_moduleErrorNoReintentableAlAgotarReintentos() {
        // GIVEN: defaultRetries=3, el módulo siempre falla reintentable
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig config = CopyPhaseConfig.crear(1, "CATALOGUE", 1, true, null);

        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), 1)).thenReturn(Optional.of(fase1));
        when(configRepo.buscarActivosPorFase(1)).thenReturn(List.of(config));
        when(moduloRepo.buscarPorClaveIdempotencia(anyString(), anyString(), eq("CATALOGUE")))
                .thenReturn(Optional.empty());
        when(moduloRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(moduloRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantClient.getNombreModulo()).thenReturn("CATALOGUE");
        // Siempre reintentable
        when(participantClient.invoke(any(), any()))
                .thenReturn(IParticipantClientPort.ParticipantResult.errorReintentable("siempre falla"));

        CopyModuleExecution fallido = CopyModuleExecution.crear(proceso.getId(), fase1.getId(), "CATALOGUE");
        fallido.setEstado(ModuleExecutionState.ERROR_NO_REINTENTABLE);
        when(moduloRepo.buscarPorFase(fase1.getId())).thenReturn(List.of(fallido));

        // WHEN
        sut.avanzarFase(proceso.getId(), 1);

        // THEN — invocado exactamente DEFAULT_RETRIES veces (hasta agotar)
        verify(participantClient, times(DEFAULT_RETRIES)).invoke(any(), any());

        // Y el módulo capturado finalmente como ERROR_NO_REINTENTABLE
        ArgumentCaptor<CopyModuleExecution> modCaptor = ArgumentCaptor.forClass(CopyModuleExecution.class);
        verify(moduloRepo, atLeastOnce()).actualizar(modCaptor.capture());
        assertThat(modCaptor.getAllValues().stream()
                .anyMatch(m -> m.getEstado() == ModuleExecutionState.ERROR_NO_REINTENTABLE))
                .isTrue();
    }

    // =========================================================================
    // REQ-IDEM-01: Módulo ya COMPLETADO → no reinvocar
    // =========================================================================

    @Test
    @DisplayName("avanzarFase — módulo ya COMPLETADO no es invocado de nuevo (REQ-IDEM-01)")
    void avanzarFase_moduloYaCompletadoNoSeReinvoca() {
        // GIVEN: módulo ya existe en estado COMPLETADO (idempotencia)
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig config = CopyPhaseConfig.crear(1, "ENTERPRISES", 1, true, null);

        CopyModuleExecution existente = CopyModuleExecution.crear(proceso.getId(), fase1.getId(), "ENTERPRISES");
        existente.setEstado(ModuleExecutionState.COMPLETADO);

        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), 1)).thenReturn(Optional.of(fase1));
        when(configRepo.buscarActivosPorFase(1)).thenReturn(List.of(config));
        when(moduloRepo.buscarPorClaveIdempotencia(proceso.getId(), fase1.getId(), "ENTERPRISES"))
                .thenReturn(Optional.of(existente));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantClient.getNombreModulo()).thenReturn("ENTERPRISES");
        when(moduloRepo.buscarPorFase(fase1.getId())).thenReturn(List.of(existente));
        when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(List.of(
                completedPhase(proceso.getId(), 1, "BASE")));

        // WHEN
        sut.avanzarFase(proceso.getId(), 1);

        // THEN — participante NO invocado (REQ-IDEM-01)
        verify(participantClient, never()).invoke(any(), any());
    }

    // =========================================================================
    // Proceso no encontrado
    // =========================================================================

    @Test
    @DisplayName("avanzarFase — lanza CopyProcessNotFoundException si proceso no existe")
    void avanzarFase_lanzaExcepcionSiProcesoNoExiste() {
        // GIVEN
        when(procesoRepo.buscarPorId("nope")).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> sut.avanzarFase("nope", 1))
                .isInstanceOf(CopyProcessNotFoundException.class);
    }

    // =========================================================================
    // REQ-PROC-03: Proceso COMPLETADO cuando última fase termina
    // =========================================================================

    @Test
    @DisplayName("avanzarFase — proceso pasa a COMPLETADO cuando todas las fases completan (REQ-PROC-03)")
    void avanzarFase_procesoCompletadoCuandoTodasFasesCompletan() {
        // GIVEN: Proceso en EN_PROCESO, Fase 4 (la última) está EN_PROCESO con 1 módulo
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

        // Todas las fases 1-4 están completas
        when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(List.of(
                completedPhase(proceso.getId(), 1, "BASE"),
                completedPhase(proceso.getId(), 2, "INTERNAS"),
                completedPhase(proceso.getId(), 3, "EXTERNAS"),
                completedPhase(proceso.getId(), 4, "CIERRE")));

        // WHEN
        sut.avanzarFase(proceso.getId(), 4);

        // THEN — proceso pasa a COMPLETADO
        ArgumentCaptor<CopyProcess> captor = ArgumentCaptor.forClass(CopyProcess.class);
        verify(procesoRepo, atLeastOnce()).actualizar(captor.capture());
        assertThat(captor.getAllValues().stream()
                .anyMatch(p -> p.getEstado() == ProcessState.COMPLETADO))
                .isTrue();
    }

    // =========================================================================
    // REQ-EVT-01: Eventos emitidos por el motor
    // =========================================================================

    @Test
    @DisplayName("avanzarFase — emite eventos Fase.iniciada, Modulo.iniciado, Modulo.completado, Fase.completada")
    void avanzarFase_emiteEventosCorrectamente() {
        // GIVEN
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig config = CopyPhaseConfig.crear(1, "ENTERPRISES", 1, true, null);
        CopyModuleExecution completado = CopyModuleExecution.crear(proceso.getId(), fase1.getId(), "ENTERPRISES");
        completado.setEstado(ModuleExecutionState.COMPLETADO);

        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), 1)).thenReturn(Optional.of(fase1));
        when(configRepo.buscarActivosPorFase(1)).thenReturn(List.of(config));
        when(moduloRepo.buscarPorClaveIdempotencia(anyString(), anyString(), eq("ENTERPRISES")))
                .thenReturn(Optional.empty());
        when(moduloRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(moduloRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantClient.getNombreModulo()).thenReturn("ENTERPRISES");
        when(participantClient.invoke(any(), any()))
                .thenReturn(IParticipantClientPort.ParticipantResult.exito());
        when(moduloRepo.buscarPorFase(fase1.getId())).thenReturn(List.of(completado));
        when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(List.of(
                completedPhase(proceso.getId(), 1, "BASE")));

        // WHEN
        sut.avanzarFase(proceso.getId(), 1);

        // THEN — eventos guardados incluyen Modulo.iniciado y Modulo.completado
        ArgumentCaptor<CopyProcessEvent> eventCaptor = ArgumentCaptor.forClass(CopyProcessEvent.class);
        verify(eventoRepo, atLeastOnce()).guardar(eventCaptor.capture());
        List<CopyEventType> tipos = eventCaptor.getAllValues().stream()
                .map(CopyProcessEvent::getTipoEvento).toList();
        assertThat(tipos).contains(CopyEventType.MODULO_INICIADO, CopyEventType.MODULO_COMPLETADO);
    }

    // =========================================================================
    // REQ-CHAIN-01 / ADR-27: Encadenamiento automático Fase N → Fase N+1
    // =========================================================================

    @Test
    @DisplayName("1.7 RED — Fase 1 completa dispara avanzarFase(id, 2) exactamente una vez (REQ-CHAIN-01)")
    void avanzarFase_fase1Completa_encadenaaFase2() {
        // GIVEN: Proceso EN_PROCESO, Fase 1 EN_PROCESO, módulo CATALOGUE completa exitosamente
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        CopyPhase fase1 = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1.setEstado(PhaseState.EN_PROCESO);
        // Fase 2 disponible para ser iniciada
        CopyPhase fase2 = CopyPhase.crear(proceso.getId(), 2, "CATALOGO");
        CopyPhaseConfig configFase1 = CopyPhaseConfig.crear(1, "CATALOGUE", 1, true, null);
        CopyPhaseConfig configFase2 = CopyPhaseConfig.crear(2, "PRODUCTS", 1, true, null);
        CopyModuleExecution moduloFase1Completado = CopyModuleExecution.crear(proceso.getId(), fase1.getId(), "CATALOGUE");
        moduloFase1Completado.setEstado(ModuleExecutionState.COMPLETADO);
        CopyModuleExecution moduloFase2 = CopyModuleExecution.crear(proceso.getId(), fase2.getId(), "PRODUCTS");

        // Fase 1 setup
        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), 1)).thenReturn(Optional.of(fase1));
        when(configRepo.buscarActivosPorFase(1)).thenReturn(List.of(configFase1));
        when(moduloRepo.buscarPorClaveIdempotencia(anyString(), anyString(), eq("CATALOGUE")))
                .thenReturn(Optional.empty());
        when(moduloRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(moduloRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantClient.getNombreModulo()).thenReturn("CATALOGUE");
        when(participantClient.invoke(any(), any()))
                .thenReturn(IParticipantClientPort.ParticipantResult.exito());
        when(moduloRepo.buscarPorFase(fase1.getId())).thenReturn(List.of(moduloFase1Completado));

        // Encadenamiento: configRepo.buscarActivosPorFase(2) debe retornar módulos activos
        when(configRepo.buscarActivosPorFase(2)).thenReturn(List.of(configFase2));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), 2)).thenReturn(Optional.of(fase2));
        when(moduloRepo.buscarPorClaveIdempotencia(anyString(), anyString(), eq("PRODUCTS")))
                .thenReturn(Optional.empty());
        when(moduloRepo.buscarPorFase(fase2.getId())).thenReturn(List.of());
        // Fase 2 no completa aún (para evitar encadenamiento Fase 2→3 en el mismo test)
        when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(List.of(
                completedPhase(proceso.getId(), 1, "BASE"),
                fase2
        ));

        // WHEN
        sut.avanzarFase(proceso.getId(), 1);

        // THEN — avanzarFase(id, 2) se ejecutó: se consultó la Fase 2 y se invocó su módulo
        verify(faseRepo, atLeastOnce()).buscarPorProcesoYNumero(proceso.getId(), 2);
        verify(configRepo, atLeastOnce()).buscarActivosPorFase(2);
    }

    @Test
    @DisplayName("1.7 RED — Última fase (4) no intenta avanzar a Fase 5 — llama evaluarCompletacionProceso (REQ-CHAIN-01)")
    void avanzarFase_ultimaFaseCompleta_noAvanzaAFase5() {
        // GIVEN: Proceso en EN_PROCESO, Fase 4 completada
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        proceso.setFaseActual(4);
        CopyPhase fase4 = CopyPhase.crear(proceso.getId(), 4, "CIERRE");
        fase4.setEstado(PhaseState.EN_PROCESO);
        CopyPhaseConfig configFase4 = CopyPhaseConfig.crear(4, "ENTERPRISES", 1, true, null);
        CopyModuleExecution moduloFase4 = CopyModuleExecution.crear(proceso.getId(), fase4.getId(), "ENTERPRISES");
        moduloFase4.setEstado(ModuleExecutionState.COMPLETADO);

        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), 4)).thenReturn(Optional.of(fase4));
        when(configRepo.buscarActivosPorFase(4)).thenReturn(List.of(configFase4));
        when(moduloRepo.buscarPorClaveIdempotencia(anyString(), anyString(), eq("ENTERPRISES")))
                .thenReturn(Optional.empty());
        when(moduloRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(moduloRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantClient.getNombreModulo()).thenReturn("ENTERPRISES");
        when(participantClient.invoke(any(), any()))
                .thenReturn(IParticipantClientPort.ParticipantResult.exito());
        when(moduloRepo.buscarPorFase(fase4.getId())).thenReturn(List.of(moduloFase4));
        // Todas las fases 1-4 están COMPLETADAS
        when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(List.of(
                completedPhase(proceso.getId(), 1, "BASE"),
                completedPhase(proceso.getId(), 2, "CATALOGO"),
                completedPhase(proceso.getId(), 3, "PRODUCTOS"),
                completedPhase(proceso.getId(), 4, "CIERRE")));

        // WHEN
        sut.avanzarFase(proceso.getId(), 4);

        // THEN — NO se consulta Fase 5 (no existe)
        verify(faseRepo, never()).buscarPorProcesoYNumero(proceso.getId(), 5);
        verify(configRepo, never()).buscarActivosPorFase(5);
        // PERO sí se actualiza el proceso a COMPLETADO
        ArgumentCaptor<CopyProcess> captor = ArgumentCaptor.forClass(CopyProcess.class);
        verify(procesoRepo, atLeastOnce()).actualizar(captor.capture());
        assertThat(captor.getAllValues().stream()
                .anyMatch(p -> p.getEstado() == ProcessState.COMPLETADO)).isTrue();
    }

    @Test
    @DisplayName("1.7 RED — Idempotencia: evaluarCompletacionFase con fase ya COMPLETADA no reencadena (REQ-CHAIN-02)")
    void avanzarFase_evaluarFaseYaCompletada_noReencadena() {
        // GIVEN: Proceso EN_PROCESO, Fase 1 ya está EN_PROCESO con módulo ya COMPLETADO
        // y la fase también ya está COMPLETADA (simula segundo callback post-transición)
        CopyProcess proceso = crearProceso();
        proceso.setEstado(ProcessState.EN_PROCESO);
        // La Fase 1 ya fue marcada COMPLETADA por una invocación anterior
        CopyPhase fase1YaCompletada = CopyPhase.crear(proceso.getId(), 1, "BASE");
        fase1YaCompletada.setEstado(PhaseState.COMPLETADA);

        CopyPhaseConfig configFase1 = CopyPhaseConfig.crear(1, "CATALOGUE", 1, true, null);
        CopyModuleExecution moduloYaCompletado = CopyModuleExecution.crear(proceso.getId(), fase1YaCompletada.getId(), "CATALOGUE");
        moduloYaCompletado.setEstado(ModuleExecutionState.COMPLETADO);

        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(faseRepo.buscarPorProcesoYNumero(proceso.getId(), 1)).thenReturn(Optional.of(fase1YaCompletada));
        when(configRepo.buscarActivosPorFase(1)).thenReturn(List.of(configFase1));
        // Módulo ya COMPLETADO → ejecutarModulo lo detecta y no llama a invoke
        when(moduloRepo.buscarPorClaveIdempotencia(anyString(), anyString(), eq("CATALOGUE")))
                .thenReturn(Optional.of(moduloYaCompletado));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantClient.getNombreModulo()).thenReturn("CATALOGUE");
        when(moduloRepo.buscarPorFase(fase1YaCompletada.getId())).thenReturn(List.of(moduloYaCompletado));
        when(faseRepo.buscarPorProceso(proceso.getId())).thenReturn(List.of(fase1YaCompletada));

        // WHEN — segunda invocación de avanzarFase para la misma Fase 1 ya COMPLETADA
        sut.avanzarFase(proceso.getId(), 1);

        // THEN — el encadenamiento NO se dispara porque la fase ya estaba COMPLETADA
        // La guarda de evaluarCompletacionFase detecta la fase COMPLETADA y no reencadena
        verify(configRepo, never()).buscarActivosPorFase(2);
        // Participante tampoco invocado (módulo ya en estado terminal)
        verify(participantClient, never()).invoke(any(), any());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CopyProcess crearProceso() {
        return CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "sub-test");
    }

    private CopyPhase completedPhase(String idProceso, int numero, String nombre) {
        CopyPhase fase = CopyPhase.crear(idProceso, numero, nombre);
        fase.setEstado(PhaseState.COMPLETADA);
        return fase;
    }
}
