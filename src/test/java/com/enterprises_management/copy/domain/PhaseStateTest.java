package com.enterprises_management.copy.domain;

import com.enterprises_management.copy.domain.enums.PhaseState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias para el enum PhaseState.
 */
class PhaseStateTest {

    @Test
    @DisplayName("PhaseState debe tener exactamente 5 valores")
    void debeHaberCincoEstados() {
        assertThat(PhaseState.values()).hasSize(5);
    }

    @Test
    @DisplayName("Los 5 valores específicos deben existir")
    void debeTenerTodosLosValoresEsperados() {
        assertThat(PhaseState.valueOf("PENDIENTE")).isNotNull();
        assertThat(PhaseState.valueOf("EN_PROCESO")).isNotNull();
        assertThat(PhaseState.valueOf("COMPLETADA")).isNotNull();
        assertThat(PhaseState.valueOf("ERROR")).isNotNull();
        assertThat(PhaseState.valueOf("OMITIDA")).isNotNull();
    }

    @Test
    @DisplayName("COMPLETADA, ERROR y OMITIDA son estados terminales")
    void estadosTerminalesSonCorretos() {
        assertThat(PhaseState.COMPLETADA.isTerminal()).isTrue();
        assertThat(PhaseState.ERROR.isTerminal()).isTrue();
        assertThat(PhaseState.OMITIDA.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("PENDIENTE y EN_PROCESO no son estados terminales")
    void estadosNoTerminalesSonCorretos() {
        assertThat(PhaseState.PENDIENTE.isTerminal()).isFalse();
        assertThat(PhaseState.EN_PROCESO.isTerminal()).isFalse();
    }
}
