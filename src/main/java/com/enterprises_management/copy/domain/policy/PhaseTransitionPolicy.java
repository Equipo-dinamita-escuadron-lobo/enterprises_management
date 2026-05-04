package com.enterprises_management.copy.domain.policy;

import com.enterprises_management.copy.domain.enums.ModuleExecutionState;
import com.enterprises_management.copy.domain.enums.PhaseState;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.exceptions.InvalidPhaseTransitionException;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Política de transiciones de estado para las 3 máquinas de estado del orquestador.
 * POJO sin Spring. Define las transiciones permitidas mediante mapas estáticos (ADR-7).
 * Cualquier transición no contemplada lanza {@link InvalidPhaseTransitionException}.
 */
public final class PhaseTransitionPolicy {

    // -------------------------------------------------------------------------
    // Mapa de transiciones válidas para ProcessState
    // -------------------------------------------------------------------------
    private static final Map<ProcessState, Set<ProcessState>> PROCESS_TRANSITIONS;

    static {
        Map<ProcessState, Set<ProcessState>> m = new EnumMap<>(ProcessState.class);
        m.put(ProcessState.PENDIENTE,   EnumSet.of(ProcessState.EN_PROCESO, ProcessState.CANCELADO));
        m.put(ProcessState.EN_PROCESO,  EnumSet.of(ProcessState.COMPLETADO, ProcessState.ERROR, ProcessState.CANCELADO));
        m.put(ProcessState.COMPLETADO,  Collections.emptySet());
        m.put(ProcessState.ERROR,       Collections.emptySet());
        m.put(ProcessState.CANCELADO,   Collections.emptySet());
        PROCESS_TRANSITIONS = Collections.unmodifiableMap(m);
    }

    // -------------------------------------------------------------------------
    // Mapa de transiciones válidas para PhaseState
    // -------------------------------------------------------------------------
    private static final Map<PhaseState, Set<PhaseState>> PHASE_TRANSITIONS;

    static {
        Map<PhaseState, Set<PhaseState>> m = new EnumMap<>(PhaseState.class);
        m.put(PhaseState.PENDIENTE,  EnumSet.of(PhaseState.EN_PROCESO, PhaseState.OMITIDA));
        m.put(PhaseState.EN_PROCESO, EnumSet.of(PhaseState.COMPLETADA, PhaseState.ERROR));
        m.put(PhaseState.COMPLETADA, Collections.emptySet());
        m.put(PhaseState.ERROR,      Collections.emptySet());
        m.put(PhaseState.OMITIDA,    Collections.emptySet());
        PHASE_TRANSITIONS = Collections.unmodifiableMap(m);
    }

    // -------------------------------------------------------------------------
    // Mapa de transiciones válidas para ModuleExecutionState
    // -------------------------------------------------------------------------
    private static final Map<ModuleExecutionState, Set<ModuleExecutionState>> MODULE_TRANSITIONS;

    static {
        Map<ModuleExecutionState, Set<ModuleExecutionState>> m = new EnumMap<>(ModuleExecutionState.class);
        m.put(ModuleExecutionState.PENDIENTE,
              EnumSet.of(ModuleExecutionState.EN_EJECUCION));
        m.put(ModuleExecutionState.EN_EJECUCION,
              EnumSet.of(ModuleExecutionState.COMPLETADO,
                         ModuleExecutionState.COMPLETADO_CON_ADVERTENCIAS,
                         ModuleExecutionState.ERROR_REINTENTABLE,
                         ModuleExecutionState.ERROR_NO_REINTENTABLE));
        m.put(ModuleExecutionState.ERROR_REINTENTABLE,
              EnumSet.of(ModuleExecutionState.EN_EJECUCION,
                         ModuleExecutionState.ERROR_NO_REINTENTABLE));
        m.put(ModuleExecutionState.COMPLETADO,                Collections.emptySet());
        m.put(ModuleExecutionState.COMPLETADO_CON_ADVERTENCIAS, Collections.emptySet());
        m.put(ModuleExecutionState.ERROR_NO_REINTENTABLE,       Collections.emptySet());
        MODULE_TRANSITIONS = Collections.unmodifiableMap(m);
    }

    // Constructor privado — clase utilitaria
    private PhaseTransitionPolicy() {}

    /**
     * Valida que la transición de ProcessState sea permitida.
     *
     * @param from estado actual del proceso
     * @param to   estado destino del proceso
     * @throws InvalidPhaseTransitionException si la transición no está permitida
     */
    public static void requireValidProcess(ProcessState from, ProcessState to) {
        validate(from, to, PROCESS_TRANSITIONS);
    }

    /**
     * Valida que la transición de PhaseState sea permitida.
     *
     * @param from estado actual de la fase
     * @param to   estado destino de la fase
     * @throws InvalidPhaseTransitionException si la transición no está permitida
     */
    public static void requireValidPhase(PhaseState from, PhaseState to) {
        validate(from, to, PHASE_TRANSITIONS);
    }

    /**
     * Valida que la transición de ModuleExecutionState sea permitida.
     *
     * @param from estado actual de la ejecución de módulo
     * @param to   estado destino de la ejecución de módulo
     * @throws InvalidPhaseTransitionException si la transición no está permitida
     */
    public static void requireValidModule(ModuleExecutionState from, ModuleExecutionState to) {
        validate(from, to, MODULE_TRANSITIONS);
    }

    /**
     * Método genérico de validación de transiciones.
     *
     * @param from         estado origen
     * @param to           estado destino
     * @param transitions  mapa de transiciones permitidas
     * @param <S>          tipo de enum de estado
     * @throws InvalidPhaseTransitionException si la transición no está permitida
     */
    private static <S extends Enum<S>> void validate(
            S from,
            S to,
            Map<S, Set<S>> transitions
    ) {
        Set<S> allowed = transitions.getOrDefault(from, Collections.emptySet());
        if (!allowed.contains(to)) {
            throw new InvalidPhaseTransitionException(from.name(), to.name());
        }
    }
}
