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
import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ModuleExecutionState;
import com.enterprises_management.copy.domain.enums.PhaseState;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyModuleExecution;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyPhaseConfig;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import com.enterprises_management.copy.domain.policy.PhaseTransitionPolicy;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Motor de saga — orquesta el avance de fases y módulos del proceso de copia.
 *
 * <p><b>ADR-12:</b> Las llamadas a {@link IParticipantClientPort#invoke} se realizan
 * SIEMPRE FUERA de la transacción activa. El patrón es:
 * <ol>
 *   <li>tx1: marcar ejecución como EN_EJECUCION y persistir</li>
 *   <li>invoke (sin @Transactional activo en este método)</li>
 *   <li>tx2: marcar resultado (COMPLETADO / ERROR_*) y persistir</li>
 * </ol>
 *
 * <p>Este servicio NO lleva anotación @Service ni @Transactional en la capa de
 * aplicación. La gestión transaccional se delega a la capa de infraestructura
 * mediante un wrapper @Transactional en el adaptador de entrada REST, o se usa
 * @Transactional en los métodos de repositorio individuales.
 */
public class SagaEngineService {

    private static final Logger log = LoggerFactory.getLogger(SagaEngineService.class);
    private static final int TOTAL_FASES = 4;

    private final ICopyProcessRepositoryPort procesoRepo;
    private final ICopyPhaseRepositoryPort faseRepo;
    private final IModuleExecutionRepositoryPort moduloRepo;
    private final IEquivalenceRepositoryPort equivalenciaRepo;
    private final IPhaseConfigRepositoryPort configRepo;
    private final IProcessEventRepositoryPort eventoRepo;
    private final IProcessNotifierPort notifier;
    private final IProcessEventPublisherPort eventPublisher;
    private final List<IParticipantClientPort> participantes;
    private final int defaultRetries;
    private final IBackupSerializerPort backupSerializer;
    private final MeterRegistry meterRegistry;
    private final boolean parallelExecution;

    public SagaEngineService(
            ICopyProcessRepositoryPort procesoRepo,
            ICopyPhaseRepositoryPort faseRepo,
            IModuleExecutionRepositoryPort moduloRepo,
            IEquivalenceRepositoryPort equivalenciaRepo,
            IPhaseConfigRepositoryPort configRepo,
            IProcessEventRepositoryPort eventoRepo,
            IProcessNotifierPort notifier,
            IProcessEventPublisherPort eventPublisher,
            List<IParticipantClientPort> participantes,
            int defaultRetries,
            IBackupSerializerPort backupSerializer,
            MeterRegistry meterRegistry,
            boolean parallelExecution
    ) {
        this.procesoRepo = procesoRepo;
        this.faseRepo = faseRepo;
        this.moduloRepo = moduloRepo;
        this.equivalenciaRepo = equivalenciaRepo;
        this.configRepo = configRepo;
        this.eventoRepo = eventoRepo;
        this.notifier = notifier;
        this.eventPublisher = eventPublisher;
        this.participantes = List.copyOf(participantes);
        this.defaultRetries = defaultRetries;
        this.backupSerializer = backupSerializer;
        this.meterRegistry = meterRegistry;
        this.parallelExecution = parallelExecution;
    }

    // =========================================================================
    // Punto de entrada principal
    // =========================================================================

    /**
     * Avanza la ejecución de una fase del proceso de copia.
     * Invoca a cada módulo activo según la configuración, gestiona reintentos
     * y propaga errores o completación hacia la fase y el proceso raíz.
     *
     * @param idProceso  ID del proceso
     * @param numeroFase número de la fase a ejecutar (1-4)
     * @throws CopyProcessNotFoundException si el proceso no existe
     */
    public void avanzarFase(String idProceso, int numeroFase) {
        // 1. Cargar proceso y verificar existencia
        CopyProcess proceso = procesoRepo.buscarPorId(idProceso)
                .orElseThrow(() -> new CopyProcessNotFoundException(idProceso));

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            // 2. Transición del proceso a EN_PROCESO si estaba PENDIENTE
            if (proceso.getEstado() == ProcessState.PENDIENTE) {
                PhaseTransitionPolicy.requireValidProcess(ProcessState.PENDIENTE, ProcessState.EN_PROCESO);
                proceso.setEstado(ProcessState.EN_PROCESO);
                proceso.setFaseActual(numeroFase);
                proceso = procesoRepo.actualizar(proceso);
            }

            // 3. Cargar la fase
            CopyPhase fase = faseRepo.buscarPorProcesoYNumero(idProceso, numeroFase)
                    .orElseThrow(() -> new IllegalStateException(
                            "Fase " + numeroFase + " no encontrada para proceso " + idProceso));

            // 4. Iniciar la fase si estaba PENDIENTE
            if (fase.getEstado() == PhaseState.PENDIENTE) {
                PhaseTransitionPolicy.requireValidPhase(PhaseState.PENDIENTE, PhaseState.EN_PROCESO);
                fase.setEstado(PhaseState.EN_PROCESO);
                fase.setIniciadaEn(LocalDateTime.now());
                fase = faseRepo.actualizar(fase);
                emitirEvento(idProceso, CopyEventType.FASE_INICIADA,
                        "{\"fase\":" + numeroFase + "}");
            }

            // 5. Obtener módulos activos de la configuración
            List<CopyPhaseConfig> modulosConfig = configRepo.buscarActivosPorFase(numeroFase);

            // 6. Ejecutar módulos — paralelo (CompletableFuture) o secuencial (H3-R3, ADR-12)
            boolean faseEnError;
            if (parallelExecution && modulosConfig.size() > 1) {
                // proceso y fase son reasignados arriba → copias finales para captura en lambda
                final CopyProcess procesoCaptura = proceso;
                final CopyPhase faseCaptura = fase;
                List<CompletableFuture<Boolean>> futures = modulosConfig.stream()
                        .map(cfg -> CompletableFuture.supplyAsync(
                                () -> ejecutarModulo(procesoCaptura, faseCaptura, cfg)))
                        .toList();
                faseEnError = futures.stream()
                        .map(CompletableFuture::join)
                        .anyMatch(Boolean::booleanValue);
            } else {
                faseEnError = false;
                for (CopyPhaseConfig cfg : modulosConfig) {
                    if (ejecutarModulo(proceso, fase, cfg)) {
                        faseEnError = true;
                        break;
                    }
                }
            }

            // 7. Evaluar resultado de la fase
            if (faseEnError) {
                marcarFaseError(proceso, fase);
                marcarProcesoError(proceso);
            } else {
                evaluarCompletacionFase(proceso, fase, numeroFase);
            }
        } finally {
            sample.stop(meterRegistry.timer(
                    "copy.saga.phase.advance.duration",
                    "tipo", proceso.getTipo().name().toLowerCase()
            ));
        }
    }

    // =========================================================================
    // Ejecución de un módulo (ADR-12)
    // =========================================================================

    /**
     * Ejecuta un módulo con reintentos.
     *
     * @return true si el módulo terminó en ERROR_NO_REINTENTABLE, false en caso de éxito
     */
    private boolean ejecutarModulo(CopyProcess proceso, CopyPhase fase, CopyPhaseConfig cfg) {
        // REQ-IDEM-01: verificar si ya existe
        Optional<CopyModuleExecution> existenteOpt = moduloRepo.buscarPorClaveIdempotencia(
                proceso.getId(), fase.getId(), cfg.getModulo());

        if (existenteOpt.isPresent()) {
            CopyModuleExecution existente = existenteOpt.get();
            // Módulo terminal → no reinvocar
            if (existente.getEstado().isTerminal()) {
                return existente.getEstado() == ModuleExecutionState.ERROR_NO_REINTENTABLE;
            }
        }

        // Crear o reutilizar la ejecución
        CopyModuleExecution ejecucion = existenteOpt
                .orElseGet(() -> moduloRepo.guardar(
                        CopyModuleExecution.crear(proceso.getId(), fase.getId(), cfg.getModulo())));

        // Bucle de reintentos
        while (ejecucion.getIntentos() < defaultRetries) {
            // Marcar EN_EJECUCION (tx1)
            PhaseTransitionPolicy.requireValidModule(ejecucion.getEstado(), ModuleExecutionState.EN_EJECUCION);
            ejecucion.setEstado(ModuleExecutionState.EN_EJECUCION);
            ejecucion.incrementarIntentos();
            ejecucion.setIniciadoEn(LocalDateTime.now());
            ejecucion = moduloRepo.actualizar(ejecucion);

            emitirEvento(proceso.getId(), CopyEventType.MODULO_INICIADO,
                    "{\"modulo\":\"" + cfg.getModulo() + "\",\"fase\":" + fase.getNumero()
                    + ",\"intento\":" + ejecucion.getIntentos() + "}");

            // Invocar participante FUERA de transacción (ADR-12)
            IParticipantClientPort client = participantes.stream()
                    .filter(p -> cfg.getModulo().equals(p.getNombreModulo()))
                    .findFirst()
                    .orElse(null);
            IParticipantClientPort.ParticipantResult resultado = (client != null)
                    ? client.invoke(proceso, ejecucion)
                    : IParticipantClientPort.ParticipantResult.errorNoReintentable("Sin cliente para módulo: " + cfg.getModulo());

            // Registrar resultado (tx2)
            if (resultado.exitoso()) {
                ModuleExecutionState estadoFinal = resultado.conAdvertencias()
                        ? ModuleExecutionState.COMPLETADO_CON_ADVERTENCIAS
                        : ModuleExecutionState.COMPLETADO;
                PhaseTransitionPolicy.requireValidModule(ejecucion.getEstado(), estadoFinal);
                ejecucion.setEstado(estadoFinal);
                ejecucion.setFinalizadoEn(LocalDateTime.now());
                ejecucion = moduloRepo.actualizar(ejecucion);

                // Persistir equivalencias generadas por el módulo (ADR-13)
                persistirEquivalencias(proceso.getId(), resultado);

                emitirEvento(proceso.getId(), CopyEventType.MODULO_COMPLETADO,
                        "{\"modulo\":\"" + cfg.getModulo() + "\",\"fase\":" + fase.getNumero() + "}");
                return false; // éxito

            } else if (!resultado.reintentable() || ejecucion.getIntentos() >= defaultRetries) {
                // Error no reintentable o se agotaron los intentos
                PhaseTransitionPolicy.requireValidModule(ejecucion.getEstado(), ModuleExecutionState.ERROR_NO_REINTENTABLE);
                ejecucion.setEstado(ModuleExecutionState.ERROR_NO_REINTENTABLE);
                ejecucion.setErrorDetalle(resultado.errorDetalle());
                ejecucion.setFinalizadoEn(LocalDateTime.now());
                ejecucion = moduloRepo.actualizar(ejecucion);
                emitirEvento(proceso.getId(), CopyEventType.MODULO_ERROR,
                        "{\"modulo\":\"" + cfg.getModulo() + "\",\"fase\":" + fase.getNumero()
                        + ",\"error\":\"" + resultado.errorDetalle() + "\"}");
                return true; // error fatal

            } else {
                // Error reintentable — marcar y reintentar
                PhaseTransitionPolicy.requireValidModule(ejecucion.getEstado(), ModuleExecutionState.ERROR_REINTENTABLE);
                ejecucion.setEstado(ModuleExecutionState.ERROR_REINTENTABLE);
                ejecucion.setErrorDetalle(resultado.errorDetalle());
                ejecucion = moduloRepo.actualizar(ejecucion);
                meterRegistry.counter(
                        "copy.saga.module.retry",
                        "module", cfg.getModulo().toLowerCase(),
                        "fase", String.valueOf(fase.getNumero())
                ).increment();
                // Continuar bucle con siguiente intento
            }
        }

        // Se alcanzó el límite de reintentos sin éxito
        PhaseTransitionPolicy.requireValidModule(ejecucion.getEstado(), ModuleExecutionState.ERROR_NO_REINTENTABLE);
        ejecucion.setEstado(ModuleExecutionState.ERROR_NO_REINTENTABLE);
        ejecucion.setFinalizadoEn(LocalDateTime.now());
        moduloRepo.actualizar(ejecucion);
        return true;
    }

    // =========================================================================
    // Evaluación de completación
    // =========================================================================

    /**
     * Evalúa si la fase actual completó y, si es así, encadena automáticamente
     * con la siguiente fase (REQ-CHAIN-01, REQ-CHAIN-02, ADR-27).
     *
     * <p>Guarda de idempotencia: si la fase ya está COMPLETADA no reencadena.
     * Guarda de recursión: no avanza más allá de {@code TOTAL_FASES} (ADR-27).
     *
     * @param proceso    proceso de copia activo
     * @param fase       fase que acaba de terminar sus módulos
     * @param numeroFase número de la fase (1-4)
     */
    /**
     * Marca la fase indicada como OMITIDA y continúa encadenando:
     * si la siguiente también está vacía, la omite también; al agotar las fases evalúa completación.
     */
    private void omitirFasesVaciasYContinuar(CopyProcess proceso, int numeroFase) {
        faseRepo.buscarPorProcesoYNumero(proceso.getId(), numeroFase).ifPresent(fase -> {
            if (fase.getEstado() == PhaseState.PENDIENTE) {
                fase.setEstado(PhaseState.OMITIDA);
                fase.setFinalizadaEn(LocalDateTime.now());
                faseRepo.actualizar(fase);
                log.info("[SagaEngine] Fase {} marcada OMITIDA (sin módulos activos)", numeroFase);
            }
        });

        int siguiente = numeroFase + 1;
        if (siguiente <= TOTAL_FASES) {
            List<CopyPhaseConfig> modulosSig = configRepo.buscarActivosPorFase(siguiente);
            if (!modulosSig.isEmpty()) {
                emitirEvento(proceso.getId(), CopyEventType.FASE_INICIADA,
                        "{\"fase\":" + siguiente + ",\"encadenada\":true}");
                avanzarFase(proceso.getId(), siguiente);
            } else {
                omitirFasesVaciasYContinuar(proceso, siguiente);
            }
        } else {
            evaluarCompletacionProceso(proceso);
        }
    }

    private void evaluarCompletacionFase(CopyProcess proceso, CopyPhase fase, int numeroFase) {
        // Guarda de idempotencia: la fase ya fue marcada COMPLETADA → no reencadenar (REQ-CHAIN-02)
        if (fase.getEstado() == PhaseState.COMPLETADA || fase.getEstado() == PhaseState.OMITIDA) {
            log.debug("[SagaEngine] Fase {} ya en estado {} — se omite evaluación (idempotencia)",
                    numeroFase, fase.getEstado());
            return;
        }

        List<CopyModuleExecution> ejecuciones = moduloRepo.buscarPorFase(fase.getId());

        boolean todasCompletas = ejecuciones.stream().allMatch(e ->
                e.getEstado() == ModuleExecutionState.COMPLETADO
                || e.getEstado() == ModuleExecutionState.COMPLETADO_CON_ADVERTENCIAS);

        if (todasCompletas) {
            // Marcar fase como COMPLETADA
            PhaseTransitionPolicy.requireValidPhase(fase.getEstado(), PhaseState.COMPLETADA);
            fase.setEstado(PhaseState.COMPLETADA);
            fase.setFinalizadaEn(LocalDateTime.now());
            faseRepo.actualizar(fase);
            meterRegistry.counter(
                    "copy.saga.phase.outcome",
                    "fase", String.valueOf(fase.getNumero()),
                    "estado", PhaseState.COMPLETADA.name().toLowerCase()
            ).increment();
            emitirEvento(proceso.getId(), CopyEventType.FASE_COMPLETADA,
                    "{\"fase\":" + numeroFase + ",\"estado\":\"COMPLETADA\"}");

            // REQ-CHAIN-01, ADR-27: encadenamiento automático con la siguiente fase
            int siguienteFase = numeroFase + 1;
            if (siguienteFase <= TOTAL_FASES) {
                List<CopyPhaseConfig> modulosSiguienteFase = configRepo.buscarActivosPorFase(siguienteFase);
                if (!modulosSiguienteFase.isEmpty()) {
                    // Hay módulos activos en la siguiente fase → encadenar
                    log.info("[SagaEngine] Fase {} completada → encadenando con Fase {} (REQ-CHAIN-01)",
                            numeroFase, siguienteFase);
                    emitirEvento(proceso.getId(), CopyEventType.FASE_INICIADA,
                            "{\"fase\":" + siguienteFase + ",\"encadenada\":true}");
                    avanzarFase(proceso.getId(), siguienteFase);
                } else {
                    // Sin módulos activos → marcar como OMITIDA y avanzar al resto
                    omitirFasesVaciasYContinuar(proceso, siguienteFase);
                }
            } else {
                // Última fase — evaluar completación del proceso completo
                evaluarCompletacionProceso(proceso);
            }
        }
    }

    private void evaluarCompletacionProceso(CopyProcess proceso) {
        List<CopyPhase> todasLasFases = faseRepo.buscarPorProceso(proceso.getId());

        boolean todasCompletas = todasLasFases.stream()
                .filter(f -> f.getNumero() <= TOTAL_FASES)
                .allMatch(f -> f.getEstado() == PhaseState.COMPLETADA
                        || f.getEstado() == PhaseState.OMITIDA);

        if (todasCompletas && todasLasFases.size() == TOTAL_FASES) {
            PhaseTransitionPolicy.requireValidProcess(proceso.getEstado(), ProcessState.COMPLETADO);
            proceso.setEstado(ProcessState.COMPLETADO);
            proceso.setFinalizadoEn(LocalDateTime.now());
            procesoRepo.actualizar(proceso);
            meterRegistry.counter(
                    "copy.saga.process.outcome",
                    "tipo", proceso.getTipo().name().toLowerCase(),
                    "estado", ProcessState.COMPLETADO.name().toLowerCase()
            ).increment();
            emitirEvento(proceso.getId(), CopyEventType.PROCESO_COMPLETADO,
                    "{\"fases\":" + TOTAL_FASES + "}");

            // Serialización de backup post-completación (REQ-BACKUP-01, REQ-BACKUP-02, ADR-44)
            if (proceso.getTipo() == CopyProcessType.BACKUP ||
                    (proceso.getTipo() == CopyProcessType.DUPLICATE && proceso.isGenerateBackup())) {
                try {
                    List<CopyEquivalenceId> equivalencias =
                            equivalenciaRepo.buscarConFiltros(proceso.getId(), null, null, null);
                    List<CopyPhase> fases = faseRepo.buscarPorProceso(proceso.getId());
                    String backupRef = backupSerializer.serializarBackup(proceso, equivalencias, fases);
                    proceso.setBackupRef(backupRef);
                    procesoRepo.actualizar(proceso);
                } catch (Exception ex) {
                    log.warn("[idProceso={}] Fallo al serializar backup: {}", proceso.getId(), ex.getMessage());
                }
            }
        }
    }

    // =========================================================================
    // Propagación de errores
    // =========================================================================

    private void marcarFaseError(CopyProcess proceso, CopyPhase fase) {
        PhaseTransitionPolicy.requireValidPhase(fase.getEstado(), PhaseState.ERROR);
        fase.setEstado(PhaseState.ERROR);
        fase.setFinalizadaEn(LocalDateTime.now());
        faseRepo.actualizar(fase);
        meterRegistry.counter(
                "copy.saga.phase.outcome",
                "fase", String.valueOf(fase.getNumero()),
                "estado", PhaseState.ERROR.name().toLowerCase()
        ).increment();
        emitirEvento(proceso.getId(), CopyEventType.FASE_ERROR,
                "{\"fase\":" + fase.getNumero() + ",\"estado\":\"ERROR\"}");
    }

    private void marcarProcesoError(CopyProcess proceso) {
        PhaseTransitionPolicy.requireValidProcess(proceso.getEstado(), ProcessState.ERROR);
        proceso.setEstado(ProcessState.ERROR);
        proceso.setFinalizadoEn(LocalDateTime.now());
        procesoRepo.actualizar(proceso);
        meterRegistry.counter(
                "copy.saga.process.outcome",
                "tipo", proceso.getTipo().name().toLowerCase(),
                "estado", ProcessState.ERROR.name().toLowerCase()
        ).increment();
        emitirEvento(proceso.getId(), CopyEventType.PROCESO_ERROR,
                "{\"mensaje\":\"Error no reintentable en módulo\"}");
    }

    // =========================================================================
    // Persistencia de equivalencias generadas por módulos participantes (ADR-13)
    // =========================================================================

    /**
     * Persiste las equivalencias de IDs generadas por un módulo participante (ADR-13).
     * Se llama solo cuando el resultado es exitoso y hay equivalencias en el resultado.
     * Idempotente: si una equivalencia ya existe con el mismo idNuevo, se omite silenciosamente.
     *
     * @param idProceso ID del proceso padre
     * @param resultado resultado de la invocación del módulo
     */
    private void persistirEquivalencias(String idProceso, IParticipantClientPort.ParticipantResult resultado) {
        if (resultado.equivalencias() == null || resultado.equivalencias().isEmpty()) {
            return;
        }
        for (IParticipantClientPort.EquivalenciaResultado eq : resultado.equivalencias()) {
            try {
                CopyEquivalenceId equivalencia = CopyEquivalenceId.crear(
                        idProceso, eq.modulo(), eq.tabla(), eq.idViejo(), eq.idNuevo()
                );
                equivalenciaRepo.guardar(equivalencia);
            } catch (Exception ex) {
                // Si ya existe la equivalencia (idempotencia), registrar advertencia y continuar
                log.warn("[idProceso={}] Equivalencia ya existente o error al persistir ({}/{}/{}): {}",
                        idProceso, eq.modulo(), eq.tabla(), eq.idViejo(), ex.getMessage());
            }
        }
    }

    // =========================================================================
    // Registro de eventos
    // =========================================================================

    private void emitirEvento(String idProceso, CopyEventType tipo, String payload) {
        CopyProcessEvent evento = CopyProcessEvent.crear(idProceso, tipo, payload);
        evento = eventoRepo.guardar(evento);
        notifier.publicar(evento);
        // Publicación AMQP best-effort: un fallo no bloquea la saga (REQ-EVENT-02, ADR-23)
        try {
            eventPublisher.publicar(evento);
        } catch (Exception ex) {
            log.warn("[SagaEngine] Fallo al publicar evento AMQP tipo={} idProceso={}: {}",
                    tipo, idProceso, ex.getMessage());
        }
    }
}
