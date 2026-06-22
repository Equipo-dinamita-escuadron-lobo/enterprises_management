package com.enterprises_management.copy.application;

import com.enterprises_management.copy.application.output.ICopyPhaseRepositoryPort;
import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.application.output.IProcessEventRepositoryPort;
import com.enterprises_management.copy.application.services.CopyProcessQueryService;
import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para CopyProcessQueryService (REQ-API-03).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CopyProcessQueryService — consultar estado")
class CopyProcessQueryServiceTest {

    @Mock
    private ICopyProcessRepositoryPort procesoRepo;

    @Mock
    private ICopyPhaseRepositoryPort faseRepo;

    @Mock
    private IProcessEventRepositoryPort eventoRepo;

    private CopyProcessQueryService sut;

    @BeforeEach
    void setUp() {
        sut = new CopyProcessQueryService(procesoRepo, faseRepo, eventoRepo);
    }

    // -------------------------------------------------------------------------
    // consultarProceso
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("consultarProceso — retorna proceso existente")
    void consultarProceso_retornaProcesoExistente() {
        // GIVEN
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "sub");
        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));

        // WHEN
        CopyProcess resultado = sut.consultarProceso(proceso.getId());

        // THEN
        assertThat(resultado.getId()).isEqualTo(proceso.getId());
    }

    @Test
    @DisplayName("consultarProceso — lanza CopyProcessNotFoundException si no existe")
    void consultarProceso_lanzaExcepcionSiNoExiste() {
        // GIVEN
        when(procesoRepo.buscarPorId("inexistente")).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> sut.consultarProceso("inexistente"))
                .isInstanceOf(CopyProcessNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // consultarFases
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("consultarFases — retorna fases del proceso")
    void consultarFases_retornaFasesDelProceso() {
        // GIVEN
        String idProceso = UUID.randomUUID().toString();
        CopyPhase fase1 = CopyPhase.crear(idProceso, 1, "BASE");
        CopyPhase fase2 = CopyPhase.crear(idProceso, 2, "INTERNAS");

        when(procesoRepo.buscarPorId(idProceso)).thenReturn(Optional.of(
                CopyProcess.crear(CopyProcessType.BACKUP, UUID.randomUUID(), null, null, "sub")));
        when(faseRepo.buscarPorProceso(idProceso)).thenReturn(List.of(fase1, fase2));

        // WHEN
        List<CopyPhase> fases = sut.consultarFases(idProceso);

        // THEN
        assertThat(fases).hasSize(2);
        assertThat(fases.get(0).getNumero()).isEqualTo(1);
    }

    @Test
    @DisplayName("consultarFases — lanza CopyProcessNotFoundException si proceso no existe")
    void consultarFases_lanzaExcepcionSiProcesoNoExiste() {
        // GIVEN
        when(procesoRepo.buscarPorId("nope")).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> sut.consultarFases("nope"))
                .isInstanceOf(CopyProcessNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // consultarEventos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("consultarEventos — retorna eventos del proceso")
    void consultarEventos_retornaEventos() {
        // GIVEN
        String idProceso = UUID.randomUUID().toString();
        CopyProcessEvent evento = CopyProcessEvent.crear(idProceso, CopyEventType.PROCESO_COPIA_INICIADO, null);

        when(procesoRepo.buscarPorId(idProceso)).thenReturn(Optional.of(
                CopyProcess.crear(CopyProcessType.BACKUP, UUID.randomUUID(), null, null, "sub")));
        when(eventoRepo.buscarPorProceso(idProceso)).thenReturn(List.of(evento));

        // WHEN
        List<CopyProcessEvent> eventos = sut.consultarEventos(idProceso);

        // THEN
        assertThat(eventos).hasSize(1);
        assertThat(eventos.get(0).getTipoEvento()).isEqualTo(CopyEventType.PROCESO_COPIA_INICIADO);
    }

    @Test
    @DisplayName("consultarEventos — lanza CopyProcessNotFoundException si proceso no existe")
    void consultarEventos_lanzaExcepcionSiProcesoNoExiste() {
        // GIVEN
        when(procesoRepo.buscarPorId("x")).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> sut.consultarEventos("x"))
                .isInstanceOf(CopyProcessNotFoundException.class);
    }
}
