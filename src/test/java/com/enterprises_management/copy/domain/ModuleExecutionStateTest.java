package com.enterprises_management.copy.domain;

import com.enterprises_management.copy.domain.enums.ModuleExecutionState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias para el enum ModuleExecutionState.
 */
class ModuleExecutionStateTest {

    @Test
    @DisplayName("ModuleExecutionState debe tener exactamente 6 valores")
    void debeHaberSeisEstados() {
        assertThat(ModuleExecutionState.values()).hasSize(6);
    }

    @Test
    @DisplayName("Los 6 valores específicos deben existir")
    void debeTenerTodosLosValoresEsperados() {
        assertThat(ModuleExecutionState.valueOf("PENDIENTE")).isNotNull();
        assertThat(ModuleExecutionState.valueOf("EN_EJECUCION")).isNotNull();
        assertThat(ModuleExecutionState.valueOf("COMPLETADO")).isNotNull();
        assertThat(ModuleExecutionState.valueOf("COMPLETADO_CON_ADVERTENCIAS")).isNotNull();
        assertThat(ModuleExecutionState.valueOf("ERROR_REINTENTABLE")).isNotNull();
        assertThat(ModuleExecutionState.valueOf("ERROR_NO_REINTENTABLE")).isNotNull();
    }

    @Test
    @DisplayName("COMPLETADO, COMPLETADO_CON_ADVERTENCIAS y ERROR_NO_REINTENTABLE son terminales")
    void estadosTerminalesSonCorretos() {
        assertThat(ModuleExecutionState.COMPLETADO.isTerminal()).isTrue();
        assertThat(ModuleExecutionState.COMPLETADO_CON_ADVERTENCIAS.isTerminal()).isTrue();
        assertThat(ModuleExecutionState.ERROR_NO_REINTENTABLE.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("PENDIENTE, EN_EJECUCION y ERROR_REINTENTABLE no son terminales")
    void estadosNoTerminalesSonCorretos() {
        assertThat(ModuleExecutionState.PENDIENTE.isTerminal()).isFalse();
        assertThat(ModuleExecutionState.EN_EJECUCION.isTerminal()).isFalse();
        assertThat(ModuleExecutionState.ERROR_REINTENTABLE.isTerminal()).isFalse();
    }
}
