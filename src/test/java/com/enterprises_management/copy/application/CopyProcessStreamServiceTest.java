package com.enterprises_management.copy.application;

import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.application.services.CopyProcessStreamService;
import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para CopyProcessStreamService (REQ-API-04).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CopyProcessStreamService — suscripción SSE")
class CopyProcessStreamServiceTest {

    @Mock
    private ICopyProcessRepositoryPort procesoRepo;

    private CopyProcessStreamService sut;

    @BeforeEach
    void setUp() {
        sut = new CopyProcessStreamService(procesoRepo);
    }

    // -------------------------------------------------------------------------
    // Suscripción exitosa
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("suscribir — retorna un ID de suscripción no vacío")
    void suscribir_retornaIdSuscripcion() {
        // GIVEN
        CopyProcess proceso = crearProceso();
        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));

        // WHEN
        String subscriptionId = sut.suscribir(proceso.getId(), event -> {});

        // THEN
        assertThat(subscriptionId).isNotBlank();
    }

    @Test
    @DisplayName("suscribir — listener recibe eventos publicados al proceso")
    void suscribir_listenerRecibeEventos() {
        // GIVEN
        CopyProcess proceso = crearProceso();
        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        AtomicReference<CopyProcessEvent> recibido = new AtomicReference<>();

        String subId = sut.suscribir(proceso.getId(), recibido::set);
        CopyProcessEvent evento = CopyProcessEvent.crear(proceso.getId(), CopyEventType.PROCESO_COPIA_INICIADO, null);

        // WHEN — publicar evento directamente
        sut.publicar(evento);

        // THEN
        assertThat(recibido.get()).isNotNull();
        assertThat(recibido.get().getTipoEvento()).isEqualTo(CopyEventType.PROCESO_COPIA_INICIADO);
    }

    @Test
    @DisplayName("suscribir — múltiples listeners reciben el mismo evento")
    void suscribir_multiplesListeners() {
        // GIVEN
        CopyProcess proceso = crearProceso();
        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        List<CopyProcessEvent> recibidos1 = new ArrayList<>();
        List<CopyProcessEvent> recibidos2 = new ArrayList<>();

        sut.suscribir(proceso.getId(), recibidos1::add);
        sut.suscribir(proceso.getId(), recibidos2::add);
        CopyProcessEvent evento = CopyProcessEvent.crear(proceso.getId(), CopyEventType.FASE_INICIADA, null);

        // WHEN
        sut.publicar(evento);

        // THEN
        assertThat(recibidos1).hasSize(1);
        assertThat(recibidos2).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // Cancelación de suscripción
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("cancelarSuscripcion — listener no recibe eventos tras cancelar")
    void cancelarSuscripcion_listenerNoRecibeEventosDespuesDeCancelar() {
        // GIVEN
        CopyProcess proceso = crearProceso();
        when(procesoRepo.buscarPorId(proceso.getId())).thenReturn(Optional.of(proceso));
        List<CopyProcessEvent> recibidos = new ArrayList<>();
        String subId = sut.suscribir(proceso.getId(), recibidos::add);

        // WHEN
        sut.cancelarSuscripcion(subId);
        sut.publicar(CopyProcessEvent.crear(proceso.getId(), CopyEventType.PROCESO_COMPLETADO, null));

        // THEN
        assertThat(recibidos).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Proceso no encontrado
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("suscribir — lanza CopyProcessNotFoundException si proceso no existe")
    void suscribir_lanzaExcepcionSiProcesoNoExiste() {
        // GIVEN
        when(procesoRepo.buscarPorId("nope")).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> sut.suscribir("nope", e -> {}))
                .isInstanceOf(CopyProcessNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CopyProcess crearProceso() {
        return CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "sub");
    }
}
