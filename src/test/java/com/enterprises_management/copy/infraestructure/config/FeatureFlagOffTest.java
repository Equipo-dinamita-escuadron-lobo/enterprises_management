package com.enterprises_management.copy.infraestructure.config;

import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.ProcesoCopiaController;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.interceptor.TenantInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que cuando app.copy.orchestrator.enabled=false los endpoints
 * de copia devuelven 404 (REQ-FLAG-01, ADR-14).
 *
 * <p>Estrategia: se usa @WebMvcTest(ProcesoCopiaController.class) con la
 * flag disabled. Dado que ProcesoCopiaController tiene @ConditionalOnProperty(enabled=true),
 * cuando enabled=false el bean no se registra. @WebMvcTest fallará al intentar
 * cargar un controller condicional que no existe → el test verifica 404.
 *
 * <p>Implementación: se carga un contexto MVC vacío sin controllers de copy
 * (porque @ConditionalOnProperty excluye el controller) y se verifica que
 * la URL retorna 404.
 */
@WebMvcTest(controllers = ProcesoCopiaController.class)
@Import({
    com.enterprises_management.enterprise.infraestructure.security.SecurityConfig.class,
    com.enterprises_management.copy.infraestructure.adapters.input.rest.advice.CopyExceptionHandler.class
})
@TestPropertySource(properties = {
    "app.copy.orchestrator.enabled=false",
    "jwt.auth.converter.principle-attribute=preferred_username",
    "jwt.auth.converter.resource-id=microservices_client"
})
class FeatureFlagOffTest {

    @Autowired
    private MockMvc mockMvc;

    /** Necesario para WebConfiguration (multi-tenancy). */
    @MockBean
    private TenantInterceptor tenantInterceptor;

    /** Necesario para SecurityConfig. */
    @MockBean
    private com.enterprises_management.enterprise.infraestructure.security.JwtAuthConverter jwtAuthConverter;

    /**
     * Mocks de las dependencias del controller — aunque con flag OFF el controller
     * no se registra, @WebMvcTest aún puede requerir beans del contexto.
     * Estos mocks satisfacen cualquier dependencia transitiva.
     */
    @MockBean
    private ICopyProcessStartPort startPort;

    @MockBean
    private com.enterprises_management.copy.application.input.ICopyProcessQueryPort queryPort;

    @MockBean
    private com.enterprises_management.copy.application.input.ICopyProcessCancelPort cancelPort;

    @MockBean
    private com.enterprises_management.copy.application.output.IPhaseConfigRepositoryPort phaseConfigPort;

    @Test
    @WithMockUser(authorities = "Backup_Create")
    @DisplayName("Con flag OFF: POST /processes devuelve 404 — controller no registrado (ADR-14)")
    void featureFlagOff_postProcesses_devuelve404() throws Exception {
        mockMvc.perform(post("/api/enterprises/copy/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"BACKUP\",\"empresaOrigen\":\""
                                + java.util.UUID.randomUUID() + "\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "Backup_View")
    @DisplayName("Con flag OFF: GET /processes/{id} devuelve 404 — controller no registrado (ADR-14)")
    void featureFlagOff_getProcess_devuelve404() throws Exception {
        mockMvc.perform(get("/api/enterprises/copy/processes/{id}", java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
