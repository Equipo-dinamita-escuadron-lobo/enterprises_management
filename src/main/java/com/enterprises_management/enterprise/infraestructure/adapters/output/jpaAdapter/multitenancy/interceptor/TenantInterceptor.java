package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.interceptor;


import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.util.TenantContext;
import com.enterprises_management.enterprise.infraestructure.security.IJwtUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

/**
 * Interceptor para gestionar el contexto del inquilino (tenant) en las peticiones web.
 * Implementa WebRequestInterceptor para procesar las peticiones antes y después de su ejecución.
 */
@Component
public class TenantInterceptor implements WebRequestInterceptor {

    @Autowired
    private IJwtUtils jwtUtils;

    /**
     * Se ejecuta antes de procesar la petición.
     * Establece el identificador del inquilino basado en el token JWT.
     *
     * @param request la petición web actual
     * @throws Exception si ocurre algún error durante el procesamiento
     */
    @Override
    public void preHandle(WebRequest request) throws Exception {
        TenantContext.setTenantId(jwtUtils.getId());
    }

    /**
     * Se ejecuta después de procesar la petición.
     * Limpia el contexto del inquilino.
     *
     * @param request la petición web actual
     * @param model el modelo de datos de la petición
     * @throws Exception si ocurre algún error durante el procesamiento
     */
    @Override
    public void postHandle(WebRequest request, ModelMap model) throws Exception {
        TenantContext.clear();
    }

    /**
     * Se ejecuta después de completar la petición.
     * No realiza ninguna acción en esta implementación.
     *
     * @param request la petición web actual
     * @param ex la excepción que pudo haber ocurrido durante el procesamiento
     * @throws Exception si ocurre algún error durante el procesamiento
     */
    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws Exception {
        // No se requiere implementación
    }
}
