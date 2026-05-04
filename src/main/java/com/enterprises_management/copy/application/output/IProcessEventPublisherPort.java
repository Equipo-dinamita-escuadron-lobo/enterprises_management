package com.enterprises_management.copy.application.output;

import com.enterprises_management.copy.domain.models.CopyProcessEvent;

/**
 * Puerto de salida para publicar eventos del proceso en un bus de mensajes.
 *
 * <p>Hito 2: implementar con RabbitMQ — ADR-16.
 * En Hito 1, {@code enterprises-management} no tiene spring-boot-starter-amqp.
 * Esta interfaz existe para delinear el contrato y facilitar la implementación futura.
 */
public interface IProcessEventPublisherPort {

    /**
     * Publica un evento del proceso en el bus de mensajes.
     *
     * @param evento evento a publicar
     */
    void publicar(CopyProcessEvent evento);
}
