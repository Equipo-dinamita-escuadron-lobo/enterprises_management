package com.enterprises_management.copy.application;

import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.output.ICopyPhaseRepositoryPort;
import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.application.output.IProcessEventRepositoryPort;
import com.enterprises_management.copy.application.services.CopyProcessStartService;
import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.exceptions.DuplicateActiveProcessException;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CopyProcessStartService (REQ-PROC-01).
 * Usa mocks de todos los output ports — sin Spring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CopyProcessStartService — iniciar proceso")
class CopyProcessStartServiceTest {

    @Mock
    private ICopyProcessRepositoryPort procesoRepo;

    @Mock
    private ICopyPhaseRepositoryPort faseRepo;

    @Mock
    private IProcessEventRepositoryPort eventoRepo;

    private CopyProcessStartService sut;

    @BeforeEach
    void setUp() {
        sut = new CopyProcessStartService(procesoRepo, faseRepo, eventoRepo);
    }

    // -------------------------------------------------------------------------
    // REQ-PROC-01: Creación exitosa
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("iniciar — proceso creado con estado PENDIENTE y campos obligatorios")
    void iniciar_creaProcesoEnEstadoPendiente() {
        // GIVEN
        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand cmd = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE, empresaOrigen, "Empresa Destino", null, "user-sub-123", false);

        when(procesoRepo.existeProcesoActivoPara(empresaOrigen)).thenReturn(false);
        when(procesoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        CopyProcess resultado = sut.iniciar(cmd);

        // THEN
        assertThat(resultado.getEstado()).isEqualTo(ProcessState.PENDIENTE);
        assertThat(resultado.getTipo()).isEqualTo(CopyProcessType.DUPLICATE);
        assertThat(resultado.getEmpresaOrigen()).isEqualTo(empresaOrigen);
        assertThat(resultado.getIniciadoPor()).isEqualTo("user-sub-123");
        assertThat(resultado.getId()).isNotBlank();
        assertThat(resultado.getSnapshotCorte()).isNotNull();
    }

    @Test
    @DisplayName("iniciar — crea exactamente 4 fases en estado PENDIENTE (REQ-FASE-01)")
    void iniciar_creaExactamenteCuatroFases() {
        // GIVEN
        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand cmd = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE, empresaOrigen, "Destino", null, "user-sub", false);

        when(procesoRepo.existeProcesoActivoPara(empresaOrigen)).thenReturn(false);
        when(procesoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        sut.iniciar(cmd);

        // THEN — guardar llamado 4 veces (una por fase)
        ArgumentCaptor<CopyPhase> captor = ArgumentCaptor.forClass(CopyPhase.class);
        verify(faseRepo, times(4)).guardar(captor.capture());
        List<CopyPhase> fases = captor.getAllValues();

        assertThat(fases).hasSize(4);
        assertThat(fases.stream().map(CopyPhase::getNumero).sorted().toList())
                .containsExactly(1, 2, 3, 4);
        assertThat(fases.stream().map(CopyPhase::getNombre).toList())
                .containsExactlyInAnyOrder("BASE", "INTERNAS", "EXTERNAS", "CIERRE");
    }

    @Test
    @DisplayName("iniciar — emite evento ProcesoCopia.iniciado (REQ-EVT-01)")
    void iniciar_emiteEventoIniciado() {
        // GIVEN
        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand cmd = new IniciarProcesoCommand(
                CopyProcessType.BACKUP, empresaOrigen, null, null, "sub-abc", false);

        when(procesoRepo.existeProcesoActivoPara(empresaOrigen)).thenReturn(false);
        when(procesoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(faseRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        sut.iniciar(cmd);

        // THEN — evento registrado
        ArgumentCaptor<CopyProcessEvent> captor = ArgumentCaptor.forClass(CopyProcessEvent.class);
        verify(eventoRepo).guardar(captor.capture());
        assertThat(captor.getValue().getTipoEvento()).isEqualTo(CopyEventType.PROCESO_COPIA_INICIADO);
    }

    // -------------------------------------------------------------------------
    // REQ-PROC-01: 409 — proceso activo ya existe
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("iniciar — lanza DuplicateActiveProcessException si hay proceso activo (REQ-PROC-01 409)")
    void iniciar_lanzaExcepcionSiProcesoActivoExiste() {
        // GIVEN
        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand cmd = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE, empresaOrigen, "Destino", null, "sub-xyz", false);

        when(procesoRepo.existeProcesoActivoPara(empresaOrigen)).thenReturn(true);

        // WHEN / THEN
        assertThatThrownBy(() -> sut.iniciar(cmd))
                .isInstanceOf(DuplicateActiveProcessException.class);

        verify(procesoRepo, never()).guardar(any());
        verify(faseRepo, never()).guardar(any());
        verify(eventoRepo, never()).guardar(any());
    }

    @Test
    @DisplayName("iniciar — IDs de proceso y fases son únicos entre sí")
    void iniciar_idsUnicos() {
        // GIVEN
        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand cmd = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE, empresaOrigen, "Destino", null, "sub-id", false);

        when(procesoRepo.existeProcesoActivoPara(empresaOrigen)).thenReturn(false);
        when(procesoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<CopyPhase> captor = ArgumentCaptor.forClass(CopyPhase.class);
        when(faseRepo.guardar(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(eventoRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        CopyProcess proceso = sut.iniciar(cmd);

        // THEN
        List<String> ids = captor.getAllValues().stream().map(CopyPhase::getId).toList();
        assertThat(ids).doesNotContain(proceso.getId());
        assertThat(ids).doesNotHaveDuplicates();
    }
}
