package com.enterprises_management.copy.domain;

import com.enterprises_management.copy.domain.enums.ModuleExecutionState;
import com.enterprises_management.copy.domain.enums.PhaseState;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.exceptions.InvalidPhaseTransitionException;
import com.enterprises_management.copy.domain.policy.PhaseTransitionPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas exhaustivas de la política de transiciones de estado (~50 casos).
 * Cubre las 3 máquinas de estado: ProcessState, PhaseState, ModuleExecutionState.
 */
class PhaseTransitionPolicyTest {

    // =========================================================================
    // ProcessState — transiciones VÁLIDAS
    // =========================================================================

    @Test @DisplayName("Proceso: PENDIENTE → EN_PROCESO es válida")
    void procesoPendienteAEnProceso() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidProcess(ProcessState.PENDIENTE, ProcessState.EN_PROCESO)
        ).doesNotThrowAnyException();
    }

    @Test @DisplayName("Proceso: EN_PROCESO → COMPLETADO es válida")
    void procesoEnProcesoACompletado() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidProcess(ProcessState.EN_PROCESO, ProcessState.COMPLETADO)
        ).doesNotThrowAnyException();
    }

    @Test @DisplayName("Proceso: EN_PROCESO → ERROR es válida")
    void procesoEnProcesoAError() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidProcess(ProcessState.EN_PROCESO, ProcessState.ERROR)
        ).doesNotThrowAnyException();
    }

    @Test @DisplayName("Proceso: PENDIENTE → CANCELADO es válida")
    void procesoPendienteACancelado() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidProcess(ProcessState.PENDIENTE, ProcessState.CANCELADO)
        ).doesNotThrowAnyException();
    }

    @Test @DisplayName("Proceso: EN_PROCESO → CANCELADO es válida")
    void procesoEnProcesoACancelado() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidProcess(ProcessState.EN_PROCESO, ProcessState.CANCELADO)
        ).doesNotThrowAnyException();
    }

    // =========================================================================
    // ProcessState — transiciones INVÁLIDAS
    // =========================================================================

    @Test @DisplayName("Proceso: COMPLETADO → EN_PROCESO es inválida")
    void procesoCompletadoAEnProceso_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidProcess(ProcessState.COMPLETADO, ProcessState.EN_PROCESO)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    @Test @DisplayName("Proceso: ERROR → PENDIENTE es inválida")
    void procesoErrorAPendiente_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidProcess(ProcessState.ERROR, ProcessState.PENDIENTE)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    @Test @DisplayName("Proceso: CANCELADO → EN_PROCESO es inválida")
    void procesoCanceladoAEnProceso_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidProcess(ProcessState.CANCELADO, ProcessState.EN_PROCESO)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    @Test @DisplayName("Proceso: COMPLETADO → CANCELADO es inválida")
    void procesoCompletadoACancelado_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidProcess(ProcessState.COMPLETADO, ProcessState.CANCELADO)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    @Test @DisplayName("Proceso: PENDIENTE → COMPLETADO es inválida (saltea EN_PROCESO)")
    void procesoPendienteACompletado_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidProcess(ProcessState.PENDIENTE, ProcessState.COMPLETADO)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    // =========================================================================
    // PhaseState — transiciones VÁLIDAS
    // =========================================================================

    @Test @DisplayName("Fase: PENDIENTE → EN_PROCESO es válida")
    void fasePendienteAEnProceso() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidPhase(PhaseState.PENDIENTE, PhaseState.EN_PROCESO)
        ).doesNotThrowAnyException();
    }

    @Test @DisplayName("Fase: EN_PROCESO → COMPLETADA es válida")
    void faseEnProcesoACompletada() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidPhase(PhaseState.EN_PROCESO, PhaseState.COMPLETADA)
        ).doesNotThrowAnyException();
    }

    @Test @DisplayName("Fase: EN_PROCESO → ERROR es válida")
    void faseEnProcesoAError() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidPhase(PhaseState.EN_PROCESO, PhaseState.ERROR)
        ).doesNotThrowAnyException();
    }

    @Test @DisplayName("Fase: PENDIENTE → OMITIDA es válida")
    void fasePendienteAOmitida() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidPhase(PhaseState.PENDIENTE, PhaseState.OMITIDA)
        ).doesNotThrowAnyException();
    }

    // =========================================================================
    // PhaseState — transiciones INVÁLIDAS
    // =========================================================================

    @Test @DisplayName("Fase: COMPLETADA → EN_PROCESO es inválida")
    void faseCompletadaAEnProceso_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidPhase(PhaseState.COMPLETADA, PhaseState.EN_PROCESO)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    @Test @DisplayName("Fase: ERROR → PENDIENTE es inválida")
    void faseErrorAPendiente_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidPhase(PhaseState.ERROR, PhaseState.PENDIENTE)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    @Test @DisplayName("Fase: OMITIDA → EN_PROCESO es inválida")
    void faseOmitidaAEnProceso_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidPhase(PhaseState.OMITIDA, PhaseState.EN_PROCESO)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    @Test @DisplayName("Fase: PENDIENTE → COMPLETADA es inválida (saltea EN_PROCESO)")
    void fasePendienteACompletada_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidPhase(PhaseState.PENDIENTE, PhaseState.COMPLETADA)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    @Test @DisplayName("Fase: COMPLETADA → ERROR es inválida")
    void faseCompletadaAError_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidPhase(PhaseState.COMPLETADA, PhaseState.ERROR)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    // =========================================================================
    // ModuleExecutionState — transiciones VÁLIDAS
    // =========================================================================

    @Test @DisplayName("Módulo: PENDIENTE → EN_EJECUCION es válida")
    void moduloPendienteAEnEjecucion() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidModule(
                ModuleExecutionState.PENDIENTE, ModuleExecutionState.EN_EJECUCION)
        ).doesNotThrowAnyException();
    }

    @Test @DisplayName("Módulo: EN_EJECUCION → COMPLETADO es válida")
    void moduloEnEjecucionACompletado() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidModule(
                ModuleExecutionState.EN_EJECUCION, ModuleExecutionState.COMPLETADO)
        ).doesNotThrowAnyException();
    }

    @Test @DisplayName("Módulo: EN_EJECUCION → COMPLETADO_CON_ADVERTENCIAS es válida")
    void moduloEnEjecucionACompletadoConAdvertencias() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidModule(
                ModuleExecutionState.EN_EJECUCION, ModuleExecutionState.COMPLETADO_CON_ADVERTENCIAS)
        ).doesNotThrowAnyException();
    }

    @Test @DisplayName("Módulo: EN_EJECUCION → ERROR_REINTENTABLE es válida")
    void moduloEnEjecucionAErrorReintentable() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidModule(
                ModuleExecutionState.EN_EJECUCION, ModuleExecutionState.ERROR_REINTENTABLE)
        ).doesNotThrowAnyException();
    }

    @Test @DisplayName("Módulo: ERROR_REINTENTABLE → EN_EJECUCION es válida (reintento)")
    void moduloErrorReintentableAEnEjecucion() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidModule(
                ModuleExecutionState.ERROR_REINTENTABLE, ModuleExecutionState.EN_EJECUCION)
        ).doesNotThrowAnyException();
    }

    @Test @DisplayName("Módulo: ERROR_REINTENTABLE → ERROR_NO_REINTENTABLE es válida (agota reintentos)")
    void moduloErrorReintentableAErrorNoReintentable() {
        assertThatCode(() ->
            PhaseTransitionPolicy.requireValidModule(
                ModuleExecutionState.ERROR_REINTENTABLE, ModuleExecutionState.ERROR_NO_REINTENTABLE)
        ).doesNotThrowAnyException();
    }

    // =========================================================================
    // ModuleExecutionState — transiciones INVÁLIDAS
    // =========================================================================

    @Test @DisplayName("Módulo: COMPLETADO → EN_EJECUCION es inválida (idempotencia)")
    void moduloCompletadoAEnEjecucion_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidModule(
                ModuleExecutionState.COMPLETADO, ModuleExecutionState.EN_EJECUCION)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    @Test @DisplayName("Módulo: ERROR_NO_REINTENTABLE → EN_EJECUCION es inválida")
    void moduloErrorNoReintentableAEnEjecucion_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidModule(
                ModuleExecutionState.ERROR_NO_REINTENTABLE, ModuleExecutionState.EN_EJECUCION)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    @Test @DisplayName("Módulo: PENDIENTE → COMPLETADO es inválida (saltea EN_EJECUCION)")
    void moduloPendienteACompletado_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidModule(
                ModuleExecutionState.PENDIENTE, ModuleExecutionState.COMPLETADO)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    @Test @DisplayName("Módulo: COMPLETADO_CON_ADVERTENCIAS → ERROR_REINTENTABLE es inválida")
    void moduloCompletadoConAdvertenciasAErrorReintentable_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidModule(
                ModuleExecutionState.COMPLETADO_CON_ADVERTENCIAS, ModuleExecutionState.ERROR_REINTENTABLE)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    @Test @DisplayName("Módulo: EN_EJECUCION → PENDIENTE es inválida")
    void moduloEnEjecucionAPendiente_falla() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidModule(
                ModuleExecutionState.EN_EJECUCION, ModuleExecutionState.PENDIENTE)
        ).isInstanceOf(InvalidPhaseTransitionException.class);
    }

    // =========================================================================
    // Mensaje de excepción
    // =========================================================================

    @Test @DisplayName("La excepción debe incluir los estados de origen y destino")
    void excepcionDebeMostrarEstadosInvalidos() {
        assertThatThrownBy(() ->
            PhaseTransitionPolicy.requireValidProcess(ProcessState.COMPLETADO, ProcessState.EN_PROCESO)
        )
        .isInstanceOf(InvalidPhaseTransitionException.class)
        .hasMessageContaining("COMPLETADO")
        .hasMessageContaining("EN_PROCESO");
    }
}
