package com.enterprises_management.copy.infraestructure.config;

import com.enterprises_management.copy.infraestructure.adapters.output.amqp.RabbitEventPublisherAdapter;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuración del exchange AMQP para eventos del ciclo de vida de copia.
 *
 * <p>Solo se activa cuando:
 * <ul>
 *   <li>El perfil activo NO es {@code test} (@Profile("!test"))</li>
 *   <li>{@code app.copy.orchestrator.events.amqp.enabled=true} (REQ-EVENT-03, ADR-23)</li>
 * </ul>
 *
 * <p>Topología declarada:
 * <ul>
 *   <li>Exchange: {@code copy.process.exchange} (Topic, durable)</li>
 *   <li>Queue: {@code audit.copy.events.queue} (durable)</li>
 *   <li>Binding: routing pattern {@code copy.process.#} → queue audit</li>
 * </ul>
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(name = "app.copy.orchestrator.events.amqp.enabled", havingValue = "true")
public class RabbitConfig {

    /** Nombre del exchange topic para eventos de copia (ADR-23). */
    public static final String EXCHANGE_COPIA = "copy.process.exchange";

    /** Cola durable para el módulo de auditoría (ADR-23). */
    public static final String QUEUE_AUDIT = "audit.copy.events.queue";

    /** Patrón de binding: todos los eventos de copy.process.* llegan a la cola audit. */
    public static final String BINDING_PATTERN = "copy.process.#";

    @Bean
    public TopicExchange copiaProcesosExchange() {
        // durable=true, autoDelete=false — sobrevive a reinicios de RabbitMQ
        return new TopicExchange(EXCHANGE_COPIA, true, false);
    }

    @Bean
    public Queue auditCopyEventsQueue() {
        // durable=true — cola sobrevive a reinicios del broker
        return new Queue(QUEUE_AUDIT, true);
    }

    @Bean
    public Binding auditQueueBinding(Queue auditCopyEventsQueue, TopicExchange copiaProcesosExchange) {
        return BindingBuilder
                .bind(auditCopyEventsQueue)
                .to(copiaProcesosExchange)
                .with(BINDING_PATTERN);
    }

    /**
     * Adaptador que publica eventos del proceso en el exchange AMQP (ADR-23, REQ-EVENT-01).
     *
     * <p>Solo se activa junto con esta configuración (mismas condiciones: !test y amqp.enabled=true).
     */
    @Bean
    public RabbitEventPublisherAdapter rabbitEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        return new RabbitEventPublisherAdapter(rabbitTemplate, EXCHANGE_COPIA);
    }
}
