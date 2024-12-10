package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Clase utilitaria para gestionar el contexto del inquilino (tenant) en una aplicación multitenant.
 * Utiliza un ThreadLocal para almacenar el identificador del inquilino actual.
 */
@Slf4j
public class TenantContext {
    private TenantContext() {}

    /**
     * ThreadLocal para almacenar el identificador del inquilino actual.
     */
    private static final InheritableThreadLocal<String> currentTenant = new InheritableThreadLocal<>();

    /**
     * Establece el identificador del inquilino actual.
     *
     * @param tenantId el identificador del inquilino a establecer
     */
    public static void setTenantId(String tenantId) {
        log.debug("Setting tenantId to " + tenantId);
        currentTenant.set(tenantId);
    }

    /**
     * Obtiene el identificador del inquilino actual.
     *
     * @return el identificador del inquilino actual
     */
    public static String getTenantId() {
        return currentTenant.get();
    }

    /**
     * Limpia el identificador del inquilino actual.
     */
    public static void clear(){
        currentTenant.remove();
    }
}
