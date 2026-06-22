package com.enterprises_management.copy.application;

import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.application.output.IProcessEventRepositoryPort;
import com.enterprises_management.copy.application.services.CopyProcessCancelService;
import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.exceptions.CopyProcessCancelDeniedException;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CopyProcessCancelService (REQ-PROC-01 cancelación).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CopyProcessCancelService — cancelar proceso")
class CopyProcessCancelServiceTest {

    @Mock
    private ICopyProcessRepositoryPort procesoRepo;

    @Mock
    private IProcessEventRepositoryPort eventoRepo;

    private CopyProcessCancelService sut;

    @BeforeEach
    void setUp() {
        sut = new CopyProcessCancelService(procesoRepo, eventoRepo);
    }

    // -------------------------------------------------------------------------
    // Cancelación exitosa desde PENDIENTE y EN_PROCESO
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @EnumSource(value = ProcessState.class, names = {"PENDIENTE", "EN_PROCESO"})
    @DisplayName("cancelar — cancela proceso en estado PENDIENTE o EN_PROCESO")
    void cancelar_cancelaProcesoActivo(ProcessState estadoInicial) {
        // GIVEN
        CopyProcess proceso = crearProceso();
        proceso.setEstado(estadoInicial);
        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        sut.cancelar(proceso.getId(), "sub-cancelador");

        // THEN — estado cambia a CANCELADO
        ArgumentCaptor<CopyProcess> captor = ArgumentCaptor.forClass(CopyProcess.class);
        verify(procesoRepo).actualizar(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo(ProcessState.CANCELADO);
        assertThat(captor.getValue().getFinalizadoEn()).isNotNull();
    }

    @Test
    @DisplayName("cancelar — emite evento Proceso.cancelado")
    void cancelar_emiteEventoCancelado() {
        // GIVEN
        CopyProcess proceso = crearProceso();
        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        when(procesoRepo.actualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        sut.cancelar(proceso.getId(), "sub-cancelador");

        // THEN
        ArgumentCaptor<CopyProcessEvent> captor = ArgumentCaptor.forClass(CopyProcessEvent.class);
        verify(eventoRepo).guardar(captor.capture());
        assertThat(captor.getValue().getTipoEvento()).isEqualTo(CopyEventType.PROCESO_CANCELADO);
    }

    // -------------------------------------------------------------------------
    // Estados terminales — cancelación denegada
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @EnumSource(value = ProcessState.class, names = {"COMPLETADO", "ERROR", "CANCELADO"})
    @DisplayName("cancelar — lanza CopyProcessCancelDeniedException para estado terminal")
    void cancelar_lanzaExcepcionEnEstadoTerminal(ProcessState estadoTerminal) {
        // GIVEN
        CopyProcess proceso = crearProceso();
        proceso.setEstado(estadoTerminal);
        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));

        // WHEN / THEN
        assertThatThrownBy(() -> sut.cancelar(proceso.getId(), "sub"))
                .isInstanceOf(CopyProcessCancelDeniedException.class);

        verify(procesoRepo, never()).actualizar(any());
        verify(eventoRepo, never()).guardar(any());
    }

    // -------------------------------------------------------------------------
    // Proceso no encontrado
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("cancelar — lanza CopyProcessNotFoundException si proceso no existe")
    void cancelar_lanzaExcepcionSiNoExiste() {
        // GIVEN
        when(procesoRepo.buscarPorId("nope")).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> sut.cancelar("nope", "sub"))
                .isInstanceOf(CopyProcessNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CopyProcess crearProceso() {
        return CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "sub-creador");
    }
}
