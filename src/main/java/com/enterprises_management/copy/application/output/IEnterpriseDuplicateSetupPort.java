package com.enterprises_management.copy.application.output;

/**
 * Puerto de salida para crear la empresa destino antes de iniciar una saga DUPLICATE.
 *
 * <p>La entidad empresa destino debe existir en BD ANTES de que la saga arranque,
 * porque el participante ENTERPRISES asume que ya fue creada (REQ-PARTICIPANT-01).
 * Para DUPLICATE se copia la empresa origen con un nuevo UUID y el nombre indicado.
 */
public interface IEnterpriseDuplicateSetupPort {

    /**
     * Crea una copia shell de la empresa origen con el nombre dado.
     *
     * @param empresaOrigenId UUID de la empresa a duplicar
     * @param nombreDestino   nombre para la nueva empresa
     * @param tenantId        tenant del usuario que inicia el proceso
     * @return UUID de la empresa recién creada, o null si no se pudo crear
     */
    String crearEmpresaDestino(String empresaOrigenId, String nombreDestino, String tenantId);
}
