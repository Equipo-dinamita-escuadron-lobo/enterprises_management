package com.enterprises_management.copy.infraestructure.adapters.output.amqp;

import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RabbitEventPublisherAdapter (REQ-EVENT-01, REQ-EVENT-02, ADR-23).
 * Verifica publicación best-effort: errores AMQP NO bloquean la lógica principal.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RabbitEventPublisherAdapter — publicación AMQP eventos")
class RabbitEventPublisherAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitEventPublisherAdapter adapter;

    private static final String EXCHANGE = "copy.process.exchange";

    @BeforeEach
    void setUp() {
        adapter = new RabbitEventPublisherAdapter(rabbitTemplate, EXCHANGE);
    }

    // =========================================================================
    // REQ-EVENT-01: routing keys correctas por tipo de evento
    // =========================================================================

    @Test
    @DisplayName("publicar PROCESO_COPIA_INICIADO → routing key copy.process.started")
    void publicar_procesoIniciado_routingKeyStarted() {
        CopyProcessEvent evento = crearEvento(CopyEventType.PROCESO_COPIA_INICIADO);

        adapter.publicar(evento);

        verify(rabbitTemplate).convertAndSend(
                eq(EXCHANGE),
                eq("copy.process.started"),
                any(Object.class)
        );
    }

    @Test
    @DisplayName("publicar FASE_COMPLETADA → routing key copy.phase.transitioned")
    void publicar_faseCompletada_routingKeyTransitioned() {
        CopyProcessEvent evento = crearEvento(CopyEventType.FASE_COMPLETADA);

        adapter.publicar(evento);

        verify(rabbitTemplate).convertAndSend(
                eq(EXCHANGE),
                eq("copy.phase.transitioned"),
                any(Object.class)
        );
    }

    @Test
    @DisplayName("publicar PROCESO_COMPLETADO → routing key copy.process.completed")
    void publicar_procesoCompletado_routingKeyCompleted() {
        CopyProcessEvent evento = crearEvento(CopyEventType.PROCESO_COMPLETADO);

        adapter.publicar(evento);

        verify(rabbitTemplate).convertAndSend(
                eq(EXCHANGE),
                eq("copy.process.completed"),
                any(Object.class)
        );
    }

    @Test
    @DisplayName("publicar PROCESO_ERROR → routing key copy.process.failed")
    void publicar_procesoError_routingKeyFailed() {
        CopyProcessEvent evento = crearEvento(CopyEventType.PROCESO_ERROR);

        adapter.publicar(evento);

        verify(rabbitTemplate).convertAndSend(
                eq(EXCHANGE),
                eq("copy.process.failed"),
                any(Object.class)
        );
    }

    @Test
    @DisplayName("publicar PROCESO_CANCELADO → routing key copy.process.cancelled")
    void publicar_procesoCancelado_routingKeyCancelled() {
        CopyProcessEvent evento = crearEvento(CopyEventType.PROCESO_CANCELADO);

        adapter.publicar(evento);

        verify(rabbitTemplate).convertAndSend(
                eq(EXCHANGE),
                eq("copy.process.cancelled"),
                any(Object.class)
        );
    }

    // =========================================================================
    // REQ-EVENT-02: publicación best-effort — fallo AMQP NO bloquea la saga
    // =========================================================================

    @Test
    @DisplayName("fallo AMQP (RuntimeException) NO propaga excepción al llamador (REQ-EVENT-02)")
    void publicar_falloAmqp_noPropagaExcepcion() {
        CopyProcessEvent evento = crearEvento(CopyEventType.PROCESO_COMPLETADO);
        doThrow(new RuntimeException("RabbitMQ no disponible"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // NO debe lanzar excepción
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> adapter.publicar(evento));
    }

    @Test
    @DisplayName("fallo AMQP → RabbitTemplate igualmente fue invocado (intento realizado)")
    void publicar_falloAmqp_templateFueInvocado() {
        CopyProcessEvent evento = crearEvento(CopyEventType.FASE_COMPLETADA);
        doThrow(new RuntimeException("broker caído"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        adapter.publicar(evento);

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    // =========================================================================
    // Payload mínimo requerido
    // =========================================================================

    @Test
    @DisplayName("payload enviado contiene idProceso y tipo de evento")
    void publicar_payloadContieneIdProcesoYTipo() {
        String idProceso = UUID.randomUUID().toString();
        CopyProcessEvent evento = CopyProcessEvent.crear(idProceso, CopyEventType.PROCESO_COMPLETADO, "{\"fases\":4}");
        evento.setId(42L);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        adapter.publicar(evento);

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), payloadCaptor.capture());
        String payload = payloadCaptor.getValue().toString();
        assertThat(payload).contains(idProceso);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CopyProcessEvent crearEvento(CopyEventType tipo) {
        String idProceso = UUID.randomUUID().toString();
        CopyProcessEvent evento = CopyProcessEvent.crear(idProceso, tipo, "{}");
        evento.setId(1L);
        return evento;
    }
}
