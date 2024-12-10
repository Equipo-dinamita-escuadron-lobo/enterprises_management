package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.async;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración de Spring para la ejecución de tareas asíncronas.
 * Implementa AsyncConfigurer para personalizar el ejecutor de tareas asíncronas.
 */
@Configuration
@EnableAsync
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Configura y proporciona un ejecutor de tareas asíncronas.
     * Establece el tamaño del pool de hilos y el decorador de tareas.
     *
     * @return el ejecutor de tareas configurado
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(7);
        executor.setMaxPoolSize(42);
        executor.setQueueCapacity(11);
        executor.setThreadNamePrefix("TenantAwareTaskExecutor-");
        executor.setTaskDecorator(new TenantAwareTaskDecorator());
        executor.initialize();

        return executor;
    }

}