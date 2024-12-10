package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy;


import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.util.TenantContext;

import java.util.Map;

/**
 * Implementación del resolvedor de identificador de inquilino (tenant) para Hibernate.
 * Proporciona el identificador del inquilino actual para las operaciones de base de datos.
 */
@SuppressWarnings("rawtypes")
@Component
class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver, HibernatePropertiesCustomizer {

    /**
     * Resuelve el identificador del inquilino actual.
     * Si no hay inquilino establecido, retorna "BOOTSTRAP" para permitir
     * la inicialización del EntityManagerFactory.
     *
     * @return el identificador del inquilino actual o "BOOTSTRAP"
     */
    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        if (!ObjectUtils.isEmpty(tenantId)) {
            return tenantId;
        } else {
            return "BOOTSTRAP";
        }
    }

    /**
     * Indica si se deben validar las sesiones existentes.
     *
     * @return true para validar las sesiones existentes
     */
    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    /**
     * Personaliza las propiedades de Hibernate para configurar el resolvedor de inquilinos.
     *
     * @param hibernateProperties las propiedades de Hibernate a personalizar
     */
    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }

}