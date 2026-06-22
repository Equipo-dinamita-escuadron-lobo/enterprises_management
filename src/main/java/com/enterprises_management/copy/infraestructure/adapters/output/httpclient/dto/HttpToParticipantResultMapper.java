package com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto;

import com.enterprises_management.copy.application.output.IParticipantClientPort.EquivalenciaResultado;
import com.enterprises_management.copy.application.output.IParticipantClientPort.ParticipantResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapeador estático de respuesta HTTP → {@link ParticipantResult}.
 *
 * <p>Tabla de mapeo (ADR-18, ADR-26, REQ-CLIENT-03):
 * <pre>
 * HTTP 200 con estado COMPLETADO/COMPLETADO_CON_ADVERTENCIAS → exitoso=true
 * HTTP 200 con estado ERROR_NO_REINTENTABLE                  → exitoso=false, reintentable=false
 * HTTP 200 con estado ERROR_REINTENTABLE                     → exitoso=false, reintentable=true
 * HTTP 400                                                   → exitoso=false, reintentable=false
 * HTTP 404                                                   → exitoso=false, reintentable=false
 * HTTP 409                                                   → exitoso=false, reintentable=false
 * HTTP 422                                                   → exitoso=false, reintentable=false
 * HTTP 500                                                   → exitoso=false, reintentable=true
 * HTTP 503, 504                                              → exitoso=false, reintentable=true
 * Timeout / sin conexión                                     → exitoso=false, reintentable=true
 * </pre>
 */
public final class HttpToParticipantResultMapper {

    /** Estado de respuesta del participante que indica éxito limpio. */
    private static final String ESTADO_COMPLETADO = "COMPLETADO";

    /** Estado de respuesta del participante que indica éxito con advertencias. */
    private static final String ESTADO_COMPLETADO_CON_ADVERTENCIAS = "COMPLETADO_CON_ADVERTENCIAS";

    /** Estado de respuesta del participante que indica error reintentable. */
    private static final String ESTADO_ERROR_REINTENTABLE = "ERROR_REINTENTABLE";

    private HttpToParticipantResultMapper() {
        // clase utilitaria — no instanciar
    }

    /**
     * Traduce un response HTTP exitoso (status 2xx) a {@link ParticipantResult}.
     *
     * <p>El campo {@code estado} del body decide si el resultado es exitoso o error,
     * permitiendo que el participante comunique errores de dominio dentro de un HTTP 200.
     *
     * @param response   body deserializado de la respuesta del participante
     * @param httpStatus código de estado HTTP recibido
     * @return resultado normalizado para el orquestador
     */
    public static ParticipantResult from(CopyPhaseResponseDto response, int httpStatus) {
        if (httpStatus >= 200 && httpStatus < 300) {
            return mapearDesdeBody(response);
        }
        return mapearDesdeCodigoError(httpStatus, extraerMensajeDeError(response));
    }

    /**
     * Traduce un error HTTP (4xx/5xx) a {@link ParticipantResult}.
     *
     * <p>Usar este método cuando WebClient lanzó {@code WebClientResponseException}
     * y no hay body deserializado (ADR-26, REQ-CLIENT-03).
     *
     * @param httpStatus código de estado HTTP del error
     * @param mensaje    descripción del error
     * @return resultado normalizado para el orquestador
     */
    public static ParticipantResult fromHttpError(int httpStatus, String mensaje) {
        return mapearDesdeCodigoError(httpStatus, mensaje);
    }

    /**
     * Traduce un timeout o error de conexión a {@link ParticipantResult}.
     *
     * <p>Siempre reintentable: el participante puede haber estado transitoriamente no disponible
     * (ADR-26, REQ-CLIENT-04).
     *
     * @param mensaje descripción del timeout o causa de la excepción
     * @return resultado con exitoso=false, reintentable=true
     */
    public static ParticipantResult fromTimeout(String mensaje) {
        return ParticipantResult.errorReintentable(mensaje);
    }

    // -------------------------------------------------------------------------
    // Métodos privados de apoyo
    // -------------------------------------------------------------------------

    private static ParticipantResult mapearDesdeBody(CopyPhaseResponseDto response) {
        if (response == null) {
            return ParticipantResult.errorNoReintentable("Respuesta HTTP 2xx sin body");
        }

        String estado = response.estado();

        if (ESTADO_COMPLETADO.equals(estado)) {
            List<EquivalenciaResultado> equivs = mapearEquivalencias(response);
            Object datosExportados = response.datosExportados();
            if (datosExportados != null) {
                return ParticipantResult.exitoConDatos(equivs, datosExportados);
            }
            return ParticipantResult.exitoConEquivalencias(equivs);
        }

        if (ESTADO_COMPLETADO_CON_ADVERTENCIAS.equals(estado)) {
            // Éxito con advertencias — equivalencias presentes, advertencias en el mensaje
            String advertencias = response.advertencias() != null
                    ? String.join("; ", response.advertencias())
                    : response.mensaje();
            return new ParticipantResult(true, true, false, advertencias,
                    mapearEquivalencias(response), response.datosExportados());
        }

        if (ESTADO_ERROR_REINTENTABLE.equals(estado)) {
            return ParticipantResult.errorReintentable(response.mensaje());
        }

        // ERROR_NO_REINTENTABLE u otro estado desconocido — conservador: no reintentar
        return ParticipantResult.errorNoReintentable(
                response.mensaje() != null ? response.mensaje() : "Estado desconocido: " + estado
        );
    }

    private static ParticipantResult mapearDesdeCodigoError(int httpStatus, String mensaje) {
        if (httpStatus >= 500) {
            // 5xx → error transitorio del servidor → reintentable
            return ParticipantResult.errorReintentable(
                    "HTTP " + httpStatus + ": " + mensaje
            );
        }
        // 4xx → error estructural del cliente → no reintentable
        return ParticipantResult.errorNoReintentable(
                "HTTP " + httpStatus + ": " + mensaje
        );
    }

    private static List<EquivalenciaResultado> mapearEquivalencias(CopyPhaseResponseDto response) {
        if (response.equivalenciasGeneradas() == null) {
            return List.of();
        }
        return response.equivalenciasGeneradas().stream()
                .map(e -> new EquivalenciaResultado(e.modulo(), e.tabla(), e.idViejo(), e.idNuevo()))
                .collect(Collectors.toList());
    }

    private static String extraerMensajeDeError(CopyPhaseResponseDto response) {
        if (response == null) {
            return "sin detalle";
        }
        return response.mensaje() != null ? response.mensaje() : "sin detalle";
    }
}
