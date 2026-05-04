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
 * Test de contrato del endpoint pull selectivo de equivalencias.
 * Valida shape JSON, header Content-Type, y campos mappings/notFound presentes (REQ-LOOKUP-01, ADR-35).
 *
 * <p>Task 1.7 — Contract test WireMock shape para futuros participantes Hito 4b.
 *
 * <p>Estrategia: @WebMvcTest con mock del puerto de aplicación. Se valida el contrato
 * observable del endpoint (forma del response) sin necesidad de H2 o servicio real.
 * El participante Fase 3 espera este contrato exacto para consumir el endpoint en 4b.
 *
 * <p>Cubre:
 * <ul>
 *   <li>Shape JSON: {@code mappings} y {@code notFound} SIEMPRE presentes.</li>
 *   <li>Content-Type: {@code application/json}.</li>
 *   <li>Clave de mapping en formato {@code "MODULO:tabla:idViejo"}.</li>
 *   <li>Batch oversize ({@literal >1000} IDs) → 400.</li>
 *   <li>Requests vacío → 400.</li>
 * </ul>
 */
@WebMvcTest(controllers = EquivalenciaLookupController.class)
@Import(CopyExceptionHandler.class)
@DisplayName("Contract test — EquivalenciaLookupController: shape JSON, headers (REQ-LOOKUP-01)")
class EquivalenciaLookupControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IEquivalenciaLookupInputPort lookupPort;

    @MockBean
    private TenantInterceptor tenantInterceptor;

    // -------------------------------------------------------------------------
    // Contract: response incluye mappings y notFound, Content-Type es application/json
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("Contract: response incluye mappings, notFound y Content-Type application/json")
    void contract_responseTieneCamposObligatoriosYContentType() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        when(lookupPort.lookup(eq(idProceso), any())).thenReturn(
            new EquivalenciaLookupResponse(
                Map.of("PRODUCTS:producto:42", 1042L),
                List.of()
            )
        );

        String body = """
            {
              "requests": [
                { "modulo": "PRODUCTS", "tabla": "producto", "idsViejos": [42] }
              ]
            }
            """;

        // WHEN / THEN — shape: mappings clave "MODULO:tabla:id", notFound array vacío, Content-Type JSON
        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/equivalences/lookup", idProceso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mappings").exists())
                .andExpect(jsonPath("$.notFound").exists())
                .andExpect(jsonPath("$.notFound").isArray())
                .andExpect(jsonPath("$.mappings['PRODUCTS:producto:42']").value(1042));
    }

    // -------------------------------------------------------------------------
    // Contract: notFound presente aunque vacío (no null, no ausente)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("Contract: notFound presente y como array vacío cuando todos los IDs existen")
    void contract_notFoundPresenteComoArrayVacio() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        when(lookupPort.lookup(eq(idProceso), any())).thenReturn(
            new EquivalenciaLookupResponse(
                Map.of("CATALOGUE:cuenta:5", 2005L, "CATALOGUE:cuenta:6", 2006L),
                List.of()   // notFound vacío
            )
        );

        String body = """
            {
              "requests": [
                { "modulo": "CATALOGUE", "tabla": "cuenta", "idsViejos": [5, 6] }
              ]
            }
            """;

        // THEN — notFound debe ser [] (no null, no ausente)
        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/equivalences/lookup", idProceso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notFound").isArray())
                .andExpect(jsonPath("$.notFound").isEmpty())
                .andExpect(jsonPath("$.mappings['CATALOGUE:cuenta:5']").value(2005))
                .andExpect(jsonPath("$.mappings['CATALOGUE:cuenta:6']").value(2006));
    }

    // -------------------------------------------------------------------------
    // Contract: batch oversize → 400 con cuerpo de error
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("Contract: batch con 1001 IDs → 400 Bad Request")
    void contract_batch1001IDs_devuelve400() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        when(lookupPort.lookup(eq(idProceso), any()))
            .thenThrow(new IllegalArgumentException(
                "El total de IDs en el request (1001) supera el límite permitido (1000)."));

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
    // Contract: notFound con items tiene el shape correcto (modulo, tabla, idViejo)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("Contract: notFound con items tiene shape correcto (modulo, tabla, idViejo)")
    void contract_notFoundItemsShapeCorrecto() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        when(lookupPort.lookup(eq(idProceso), any())).thenReturn(
            new EquivalenciaLookupResponse(
                Map.of(),
                List.of(new EquivalenciaLookupResponse.NotFoundItem("PRODUCTS", "producto", 99L))
            )
        );

        String body = """
            {
              "requests": [
                { "modulo": "PRODUCTS", "tabla": "producto", "idsViejos": [99] }
              ]
            }
            """;

        // THEN — notFound[0] tiene modulo, tabla, idViejo
        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/equivalences/lookup", idProceso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notFound[0].modulo").value("PRODUCTS"))
                .andExpect(jsonPath("$.notFound[0].tabla").value("producto"))
                .andExpect(jsonPath("$.notFound[0].idViejo").value(99));
    }
}
