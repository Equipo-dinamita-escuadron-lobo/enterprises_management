package com.enterprises_management.copy.application.input;

import com.enterprises_management.copy.domain.models.CopyProcessEvent;

import java.util.function.Consumer;

/**
 * Puerto de entrada para el stream SSE de eventos de un proceso de copia.
 * Corresponde a GET /api/enterprises/copy/processes/{id}/stream (REQ-API-04).
 */
public interface ICopyProcessStreamPort {

    /**
     * Suscribe un consumidor a los eventos de un proceso.
     * El consumidor recibirá todos los eventos a medida que sean emitidos.
     * Retorna el ID de suscripción para poder cancelarla después.
     *
     * @param idProceso ID del proceso al que se quiere suscribir
     * @param listener  consumidor de eventos SSE
     * @return ID de suscripción
     * @throws com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException
     *         si el proceso no existe
     */
    String suscribir(String idProceso, Consumer<CopyProcessEvent> listener);

    /**
     * Cancela una suscripción SSE activa.
     *
     * @param subscriptionId ID de suscripción retornado por {@link #suscribir}
     */
    void cancelarSuscripcion(String subscriptionId);
}
