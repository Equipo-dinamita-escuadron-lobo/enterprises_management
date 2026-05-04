package com.enterprises_management.copy.infraestructure.adapters.input.rest;

import com.enterprises_management.copy.application.input.IEquivalenciaLookupInputPort;
import com.enterprises_management.copy.domain.models.EquivalenciaLookupResponse;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.advice.CopyExceptionHandler;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.interceptor.TenantInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para EquivalenciaLookupController (REQ-LOOKUP-01, ADR-35).
 * TDD ESTRICTO — task 1.3 RED.
 *
 * <p>Endpoint bajo test: POST /api/enterprises/copy/processes/{idProceso}/equivalences/lookup
 */
@WebMvcTest(controllers = EquivalenciaLookupController.class)
@Import(CopyExceptionHandler.class)
class EquivalenciaLookupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IEquivalenciaLookupInputPort lookupPort;

    /** Necesario para satisfacer WebConfiguration que requiere TenantInterceptor (multi-tenancy). */
    @MockBean
    private TenantInterceptor tenantInterceptor;

    // -------------------------------------------------------------------------
    // Happy path: body válido → 200 + shape correcto
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("POST /equivalences/lookup body válido → 200 con mappings y notFound")
    void lookup_bodyValido_devuelve200ConShapeCorrecto() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        EquivalenciaLookupResponse responseService = new EquivalenciaLookupResponse(
            Map.of("PRODUCTS:producto:42", 1042L),
            List.of()
        );
        when(lookupPort.lookup(eq(idProceso), any())).thenReturn(responseService);

        String body = """
            {
              "requests": [
                { "modulo": "PRODUCTS", "tabla": "producto", "idsViejos": [42] }
              ]
            }
            """;

        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/equivalences/lookup", idProceso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mappings['PRODUCTS:producto:42']").value(1042))
                .andExpect(jsonPath("$.notFound").isArray());
    }

    // -------------------------------------------------------------------------
    // Triangulación: body con 1001 IDs → 400 Bad Request
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("POST /equivalences/lookup con 1001 IDs totales → 400 Bad Request")
    void lookup_masDeMillIDs_devuelve400() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        when(lookupPort.lookup(eq(idProceso), any()))
            .thenThrow(new IllegalArgumentException("El total de IDs en el request (1001) supera el límite permitido (1000)."));

        // Construir un body con 1001 IDs
        StringBuilder idsJson = new StringBuilder("[");
        for (int i = 1; i <= 1001; i++) {
            if (i > 1) idsJson.append(",");
            idsJson.append(i);
        }
        idsJson.append("]");

        String body = """
            {
              "requests": [
                { "modulo": "PRODUCTS", "tabla": "producto", "idsViejos": %s }
              ]
            }
            """.formatted(idsJson);

        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/equivalences/lookup", idProceso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // Triangulación: body vacío / sin requests → 400
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("POST /equivalences/lookup body sin requests → 400 Bad Request")
    void lookup_bodySinRequests_devuelve400() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        String body = """
            { "requests": [] }
            """;

        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/equivalences/lookup", idProceso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // Triangulación: sin autenticación → 401
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /equivalences/lookup sin autenticación → 401")
    void lookup_sinAutenticacion_devuelve401() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        String body = """
            {
              "requests": [
                { "modulo": "PRODUCTS", "tabla": "producto", "idsViejos": [1] }
              ]
            }
            """;

        // Sin @WithMockUser y con csrf() → Spring Security retorna 401 (sin autenticar)
        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/equivalences/lookup", idProceso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
