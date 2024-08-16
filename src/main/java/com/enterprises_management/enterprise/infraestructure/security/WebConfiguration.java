package com.enterprises_management.enterprise.infraestructure.security;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.interceptor.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addWebRequestInterceptor(tenantInterceptor);
    }

}