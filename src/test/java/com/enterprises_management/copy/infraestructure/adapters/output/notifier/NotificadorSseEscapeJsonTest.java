package com.enterprises_management.copy.infraestructure.adapters.output.notifier;

import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests de serialización JSON segura del adaptador SSE (REQ-SSE-01, ADR-24).
 *
 * <p>Verifica que el campo {@code data:} del evento SSE siempre contiene JSON válido
 * incluso cuando el payload contiene caracteres especiales:
 * comillas dobles, saltos de línea, backslash, caracteres unicode.
 *
 * <p>El objetivo es reemplazar la concatenación manual de strings por
 * {@code ObjectMapper.writeValueAsString()} (ADR-24).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificadorEstadoSseAdapter — escape JSON seguro (REQ-SSE-01)")
class NotificadorSseEscapeJsonTest {

    @Mock
    private SseEmitterRegistry registry;

    private NotificadorEstadoSseAdapter adapter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        adapter = new NotificadorEstadoSseAdapter(registry, objectMapper);
    }

    // =========================================================================
    // REQ-SSE-01 Scenario: payload con comillas dobles
    // =========================================================================

    @Test
    @DisplayName("payload con comillas dobles → JSON válido (REQ-SSE-01)")
    void publicar_payloadConComillasDobles_jsonValido() throws Exception {
        String payloadConComillas = "{\"empresa\":\"El \\\"Taller\\\"\",\"fase\":1}";
        CopyProcessEvent evento = crearEvento(CopyEventType.FASE_COMPLETADA, payloadConComillas);

        ArgumentCaptor<String> dataCaptor = ArgumentCaptor.forClass(String.class);
        adapter.publicar(evento);
        verify(registry).send(anyString(), dataCaptor.capture());

        // El string capturado debe ser JSON parseable
        assertThatCode(() -> objectMapper.readTree(dataCaptor.getValue()))
                .doesNotThrowAnyException();

        JsonNode root = objectMapper.readTree(dataCaptor.getValue());
        assertThat(root.has("tipo")).isTrue();
        assertThat(root.has("idProceso")).isTrue();
    }

    // =========================================================================
    // REQ-SSE-01 Scenario: payload con saltos de línea
    // =========================================================================

    @Test
    @DisplayName("payload con salto de línea → JSON válido sin romper framing SSE (REQ-SSE-01)")
    void publicar_payloadConSaltoDeLinea_jsonValido() throws Exception {
        // Salto de línea literal en el mensaje — debe escaparse en JSON
        String payloadConSalto = "línea 1\nlínea 2";
        CopyProcessEvent evento = crearEvento(CopyEventType.MODULO_ERROR, payloadConSalto);

        ArgumentCaptor<String> dataCaptor = ArgumentCaptor.forClass(String.class);
        adapter.publicar(evento);
        verify(registry).send(anyString(), dataCaptor.capture());

        String data = dataCaptor.getValue();

        // El JSON debe ser parseable
        assertThatCode(() -> objectMapper.readTree(data)).doesNotThrowAnyException();

        // El string data: no debe contener un newline literal sin escapar dentro de la string JSON
        // (el framing SSE usa \n\n como separador — un \n literal dentro del campo data rompe el stream)
        JsonNode root = objectMapper.readTree(data);
        assertThat(root).isNotNull();
    }

    // =========================================================================
    // REQ-SSE-01 Scenario: payload con backslash
    // =========================================================================

    @Test
    @DisplayName("payload con backslash → JSON válido (REQ-SSE-01)")
    void publicar_payloadConBackslash_jsonValido() throws Exception {
        String payloadConBackslash = "ruta: C:\\Users\\test\\archivo.txt";
        CopyProcessEvent evento = crearEvento(CopyEventType.MODULO_ERROR, payloadConBackslash);

        ArgumentCaptor<String> dataCaptor = ArgumentCaptor.forClass(String.class);
        adapter.publicar(evento);
        verify(registry).send(anyString(), dataCaptor.capture());

        assertThatCode(() -> objectMapper.readTree(dataCaptor.getValue()))
                .doesNotThrowAnyException();
    }

    // =========================================================================
    // REQ-SSE-01 Scenario: payload con caracteres unicode (é, ñ, etc.)
    // =========================================================================

    @Test
    @DisplayName("payload con caracteres unicode → JSON válido UTF-8 (REQ-SSE-01)")
    void publicar_payloadConUnicode_jsonValido() throws Exception {
        String payloadUnicode = "Advertencia: entidad \"Café & Ñoño\" tiene código especial";
        CopyProcessEvent evento = crearEvento(CopyEventType.MODULO_COMPLETADO, payloadUnicode);

        ArgumentCaptor<String> dataCaptor = ArgumentCaptor.forClass(String.class);
        adapter.publicar(evento);
        verify(registry).send(anyString(), dataCaptor.capture());

        // Debe ser JSON válido
        assertThatCode(() -> objectMapper.readTree(dataCaptor.getValue()))
                .doesNotThrowAnyException();

        JsonNode root = objectMapper.readTree(dataCaptor.getValue());
        // El campo payload debe existir en el shape ADR-8
        assertThat(root.has("payload")).isTrue();
    }

    // =========================================================================
    // REQ-SSE-01 Scenario: payload con retorno de carro
    // =========================================================================

    @Test
    @DisplayName("payload con \\r\\n → JSON válido (REQ-SSE-01)")
    void publicar_payloadConRetornoDeCarroYSalto_jsonValido() throws Exception {
        String payloadConCRLF = "fin\r\nde línea";
        CopyProcessEvent evento = crearEvento(CopyEventType.PROCESO_ERROR, payloadConCRLF);

        ArgumentCaptor<String> dataCaptor = ArgumentCaptor.forClass(String.class);
        adapter.publicar(evento);
        verify(registry).send(anyString(), dataCaptor.capture());

        assertThatCode(() -> objectMapper.readTree(dataCaptor.getValue()))
                .doesNotThrowAnyException();
    }

    // =========================================================================
    // Compatibilidad — tests del Hito 1 siguen pasando (shape ADR-8)
    // =========================================================================

    @Test
    @DisplayName("payload null → JSON válido con payload vacío (compatibilidad Hito 1)")
    void publicar_payloadNull_jsonValido() throws Exception {
        CopyProcessEvent evento = crearEvento(CopyEventType.PROCESO_CANCELADO, null);

        ArgumentCaptor<String> dataCaptor = ArgumentCaptor.forClass(String.class);
        adapter.publicar(evento);
        verify(registry).send(anyString(), dataCaptor.capture());

        assertThatCode(() -> objectMapper.readTree(dataCaptor.getValue()))
                .doesNotThrowAnyException();

        JsonNode root = objectMapper.readTree(dataCaptor.getValue());
        assertThat(root.has("tipo")).isTrue();
        assertThat(root.has("idProceso")).isTrue();
        assertThat(root.has("secuencia")).isTrue();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CopyProcessEvent crearEvento(CopyEventType tipo, String payload) {
        String idProceso = UUID.randomUUID().toString();
        CopyProcessEvent evento = CopyProcessEvent.crear(idProceso, tipo, payload);
        evento.setId(1L);
        return evento;
    }
}
