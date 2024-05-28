package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy;


import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.util.TenantContext;

import java.util.Map;

@SuppressWarnings("rawtypes")
@Component
class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver, HibernatePropertiesCustomizer {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        if (!ObjectUtils.isEmpty(tenantId)) {
            return tenantId;
        } else {
            // Allow bootstrapping the EntityManagerFactory, in which case no tenant is needed
            return "BOOTSTRAP";
        }
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }

}