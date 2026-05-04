package com.enterprises_management.copy.infraestructure.adapters.output.amqp;

import com.enterprises_management.copy.application.output.IProcessEventPublisherPort;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación fallback de {@link IProcessEventPublisherPort} que solo loguea el evento.
 *
 * <p>Activa cuando {@link RabbitEventPublisherAdapter} NO está disponible:
 * <ul>
 *   <li>En perfil {@code test} (AMQP deshabilitado)</li>
 *   <li>Cuando {@code app.copy.orchestrator.events.amqp.enabled=false}</li>
 * </ul>
 *
 * <p>Registrado como {@code @Bean @ConditionalOnMissingBean} en
 * {@link com.enterprises_management.copy.infraestructure.config.CopyOrchestratorWebConfig}
 * para garantizar que SagaEngineService siempre tenga un publisher (REQ-EVENT-03, ADR-23).
 */
public class LoggingEventPublisherAdapter implements IProcessEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisherAdapter.class);

    /**
     * No-op: registra el evento en el log de nivel DEBUG.
     * No requiere RabbitMQ.
     *
     * @param evento evento del proceso
     */
    @Override
    public void publicar(CopyProcessEvent evento) {
        log.debug("[EventPublisher:noop] tipo={} idProceso={} payload={}",
                evento.getTipoEvento(), evento.getIdProceso(), evento.getPayloadJson());
    }
}
