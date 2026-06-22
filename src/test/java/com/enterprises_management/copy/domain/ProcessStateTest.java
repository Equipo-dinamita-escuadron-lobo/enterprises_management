package com.enterprises_management.copy.domain;

import com.enterprises_management.copy.domain.enums.ProcessState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias para el enum ProcessState.
 * Verifica valores del enum y el método isTerminal().
 */
class ProcessStateTest {

    @Test
    @DisplayName("ProcessState debe tener exactamente 5 valores")
    void debeHaberCincoEstados() {
        assertThat(ProcessState.values()).hasSize(5);
    }

    @Test
    @DisplayName("Los 5 valores específicos deben existir")
    void debeTenerTodosLosValoresEsperados() {
        assertThat(ProcessState.valueOf("PENDIENTE")).isNotNull();
        assertThat(ProcessState.valueOf("EN_PROCESO")).isNotNull();
        assertThat(ProcessState.valueOf("COMPLETADO")).isNotNull();
        assertThat(ProcessState.valueOf("ERROR")).isNotNull();
        assertThat(ProcessState.valueOf("CANCELADO")).isNotNull();
    }

    @Test
    @DisplayName("COMPLETADO, ERROR y CANCELADO son estados terminales")
    void estadosTerminalesSonCorretos() {
        assertThat(ProcessState.COMPLETADO.isTerminal()).isTrue();
        assertThat(ProcessState.ERROR.isTerminal()).isTrue();
        assertThat(ProcessState.CANCELADO.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("PENDIENTE y EN_PROCESO no son estados terminales")
    void estadosNoTerminalesSonCorretos() {
        assertThat(ProcessState.PENDIENTE.isTerminal()).isFalse();
        assertThat(ProcessState.EN_PROCESO.isTerminal()).isFalse();
    }
}
