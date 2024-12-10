package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.async;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.util.TenantContext;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

/**
 * Decorador de tareas que asegura que el contexto del inquilino (tenant) se propague
 * a las tareas asíncronas. Implementa TaskDecorator para personalizar la ejecución de tareas.
 */
public class TenantAwareTaskDecorator implements TaskDecorator {

    /**
     * Decora una tarea Runnable para asegurar que el contexto del inquilino se mantenga.
     *
     * @param runnable la tarea original a decorar
     * @return la tarea decorada con el contexto del inquilino
     */
    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        String tenantId = TenantContext.getTenantId();
        return () -> {
            try {
                TenantContext.setTenantId(tenantId);
                runnable.run();
            } finally {
                TenantContext.setTenantId(null);
            }
        };
    }
}