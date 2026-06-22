package com.enterprises_management.copy.infraestructure.adapters.output.amqp;

import com.enterprises_management.copy.application.output.IProcessEventPublisherPort;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Adaptador de salida que publica eventos del proceso de copia en RabbitMQ (ADR-23).
 *
 * <p>Registrado como bean en {@link com.enterprises_management.copy.infraestructure.config.RabbitConfig}
 * con condiciones {@code @Profile("!test")} + {@code @ConditionalOnProperty(amqp.enabled=true)}.
 *
 * <p>Publicación best-effort (REQ-EVENT-02): cualquier excepción AMQP es capturada
 * y logueada. Un fallo AMQP NUNCA bloquea ni revierte la transición de estado del proceso.
 * La BD es la fuente de verdad (ADR-23).
 */
public class RabbitEventPublisherAdapter implements IProcessEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventPublisherAdapter.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public RabbitEventPublisherAdapter(RabbitTemplate rabbitTemplate, String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    /**
     * Publica el evento en el exchange AMQP con la routing key correspondiente.
     *
     * <p>Cualquier excepción durante la publicación es capturada y logueada (best-effort).
     *
     * @param evento evento del proceso a publicar
     */
    @Override
    public void publicar(CopyProcessEvent evento) {
        String routingKey = AmqpRoutingKeyMapper.resolver(evento.getTipoEvento());
        EventoProcesoAmqpDto dto = mapearADto(evento);
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, dto);
            log.debug("[AMQP] Evento publicado exchange={} routingKey={} idProceso={}",
                    exchange, routingKey, evento.getIdProceso());
        } catch (Exception ex) {
            // Best-effort: fallo AMQP no bloquea la saga (REQ-EVENT-02, ADR-23)
            log.warn("[AMQP] Fallo al publicar evento tipo={} idProceso={}: {}",
                    evento.getTipoEvento(), evento.getIdProceso(), ex.getMessage());
        }
    }

    private EventoProcesoAmqpDto mapearADto(CopyProcessEvent evento) {
        return new EventoProcesoAmqpDto(
                evento.getIdProceso(),
                evento.getTipoEvento().getTipo(),
                evento.getOcurridoEn(),
                evento.getPayloadJson(),
                evento.getId()
        );
    }
}
