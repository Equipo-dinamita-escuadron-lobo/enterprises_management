package com.enterprises_management.copy.application.services;

import com.enterprises_management.copy.application.input.ICopyProcessStreamPort;
import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.application.output.IProcessNotifierPort;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Servicio de aplicación para gestionar las suscripciones SSE del proceso de copia.
 * También implementa {@link IProcessNotifierPort} para que el SagaEngineService
 * pueda publicar eventos hacia los suscriptores sin conocer la implementación HTTP.
 *
 * <p>En Hito 1, la implementación es en-memoria (no distribuida).
 * Para escalar a múltiples instancias se necesitará un bus de eventos (Hito 7).
 */
public class CopyProcessStreamService implements ICopyProcessStreamPort, IProcessNotifierPort {

    private final ICopyProcessRepositoryPort procesoRepo;

    /**
     * Mapa de suscripciones activas: subscriptionId → (idProceso, Consumer).
     * Implementación simplificada en-memoria para Hito 1.
     */
    private final Map<String, SuscripcionEntry> suscripciones = new ConcurrentHashMap<>();

    public CopyProcessStreamService(ICopyProcessRepositoryPort procesoRepo) {
        this.procesoRepo = procesoRepo;
    }

    @Override
    public String suscribir(String idProceso, Consumer<CopyProcessEvent> listener) {
        // Verificar existencia del proceso
        procesoRepo.buscarPorId(idProceso)
                .orElseThrow(() -> new CopyProcessNotFoundException(idProceso));

        String subscriptionId = UUID.randomUUID().toString();
        suscripciones.put(subscriptionId, new SuscripcionEntry(idProceso, listener));
        return subscriptionId;
    }

    @Override
    public void cancelarSuscripcion(String subscriptionId) {
        suscripciones.remove(subscriptionId);
    }

    @Override
    public void publicar(CopyProcessEvent evento) {
        suscripciones.values().stream()
                .filter(e -> e.idProceso().equals(evento.getIdProceso()))
                .forEach(e -> e.listener().accept(evento));
    }

    // -------------------------------------------------------------------------
    // Tipos internos
    // -------------------------------------------------------------------------

    private record SuscripcionEntry(String idProceso, Consumer<CopyProcessEvent> listener) {}
}
