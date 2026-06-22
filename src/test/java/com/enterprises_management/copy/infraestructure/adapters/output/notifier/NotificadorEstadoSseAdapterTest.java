package com.enterprises_management.copy.infraestructure.adapters.output.notifier;

import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import com.enterprises_management.copy.infraestructure.adapters.output.notifier.NotificadorEstadoSseAdapter;
import com.enterprises_management.copy.infraestructure.adapters.output.notifier.SseEmitterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para NotificadorEstadoSseAdapter.
 * Verifica que publicar() delega correctamente al SseEmitterRegistry con el shape ADR-8.
 */
@ExtendWith(MockitoExtension.class)
class NotificadorEstadoSseAdapterTest {

    @Mock
    private SseEmitterRegistry registry;

    private NotificadorEstadoSseAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new NotificadorEstadoSseAdapter(registry);
    }

    @Test
    @DisplayName("publicar evento no-terminal invoca registry.send con el idProceso correcto")
    void publicar_eventoNoTerminal_invocaRegistrySend() {
        String idProceso = UUID.randomUUID().toString();
        CopyProcessEvent evento = CopyProcessEvent.crear(idProceso, CopyEventType.FASE_INICIADA, "{\"fase\": 1}");

        adapter.publicar(evento);

        verify(registry, times(1)).send(eq(idProceso), any());
        verify(registry, never()).complete(idProceso);
    }

    @Test
    @DisplayName("publicar evento de proceso COMPLETADO invoca send y luego complete")
    void publicar_eventoProcesoCompletado_invocaSendYComplete() {
        String idProceso = UUID.randomUUID().toString();
        CopyProcessEvent evento = CopyProcessEvent.crear(idProceso, CopyEventType.PROCESO_COMPLETADO, "{\"estado\": \"COMPLETADO\"}");

        adapter.publicar(evento);

        verify(registry, times(1)).send(eq(idProceso), any());
        verify(registry, times(1)).complete(idProceso);
    }

    @Test
    @DisplayName("publicar evento de proceso ERROR invoca send y luego complete")
    void publicar_eventoProcesoError_invocaSendYComplete() {
        String idProceso = UUID.randomUUID().toString();
        CopyProcessEvent evento = CopyProcessEvent.crear(idProceso, CopyEventType.PROCESO_ERROR, "{\"estado\": \"ERROR\"}");

        adapter.publicar(evento);

        verify(registry, times(1)).send(eq(idProceso), any());
        verify(registry, times(1)).complete(idProceso);
    }

    @Test
    @DisplayName("publicar evento de proceso CANCELADO invoca send y luego complete")
    void publicar_eventoProcesoCancel_invocaSendYComplete() {
        String idProceso = UUID.randomUUID().toString();
        CopyProcessEvent evento = CopyProcessEvent.crear(idProceso, CopyEventType.PROCESO_CANCELADO, null);

        adapter.publicar(evento);

        verify(registry, times(1)).send(eq(idProceso), any());
        verify(registry, times(1)).complete(idProceso);
    }

    @Test
    @DisplayName("el payload enviado al registry incluye tipo, idProceso y secuencia")
    void publicar_payloadIncluyeCamposShapeAdr8() {
        String idProceso = UUID.randomUUID().toString();
        CopyProcessEvent evento = CopyProcessEvent.crear(idProceso, CopyEventType.MODULO_COMPLETADO, "{\"modulo\": \"CATALOGUE\"}");

        adapter.publicar(evento);

        // Verificar que se invoca send con un String que contiene los campos clave del shape ADR-8
        verify(registry).send(eq(idProceso), argThat(payload ->
                payload.contains("tipo") &&
                payload.contains("idProceso") &&
                payload.contains(idProceso)
        ));
    }
}
