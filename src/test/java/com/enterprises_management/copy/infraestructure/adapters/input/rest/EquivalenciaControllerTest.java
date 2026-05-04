package com.enterprises_management.copy.infraestructure.adapters.input.rest;

import com.enterprises_management.copy.application.input.IEquivalenceQueryPort;
import com.enterprises_management.copy.application.input.IEquivalenceRegistryPort;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.exceptions.EquivalenceConflictException;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para EquivalenciaController (REQ-EQ-01, REQ-EQ-02, ADR-9).
 * TDD ESTRICTO: primero ROJO.
 */
@WebMvcTest(controllers = EquivalenciaController.class)
@Import(com.enterprises_management.copy.infraestructure.adapters.input.rest.advice.CopyExceptionHandler.class)
class EquivalenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IEquivalenceRegistryPort registryPort;

    @MockBean
    private IEquivalenceQueryPort queryPort;

    /** Necesario para satisfacer WebConfiguration que requiere TenantInterceptor (multi-tenancy). */
    @MockBean
    private TenantInterceptor tenantInterceptor;

    // -------------------------------------------------------------------------
    // POST /api/enterprises/copy/processes/{id}/equivalences
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("POST /equivalences con lista válida devuelve 201")
    void registrarEquivalencias_bodyValido_devuelve201() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        when(registryPort.registrar(any())).thenReturn(2);

        String body = """
            [
                {"modulo": "PRODUCTS", "tabla": "producto", "idViejo": "42", "idNuevo": "1042"},
                {"modulo": "PRODUCTS", "tabla": "producto", "idViejo": "43", "idNuevo": "1043"}
            ]
            """;

        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/equivalences", idProceso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.insertadas").value(2));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /equivalences idempotente: mismo body 2x devuelve 200 con misma idNuevo")
    void registrarEquivalencias_idempotente_devuelve200() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        // Segunda llamada retorna 0 (ya existía)
        when(registryPort.registrar(any())).thenReturn(0);

        String body = """
            [
                {"modulo": "PRODUCTS", "tabla": "producto", "idViejo": "42", "idNuevo": "1042"}
            ]
            """;

        // Primera invocación ya insertada, segunda devuelve 0 insertadas → 200 OK
        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/equivalences", idProceso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /equivalences proceso no encontrado devuelve 404")
    void registrarEquivalencias_procesoNoExiste_devuelve404() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        when(registryPort.registrar(any())).thenThrow(new CopyProcessNotFoundException(idProceso));

        String body = """
            [
                {"modulo": "PRODUCTS", "tabla": "producto", "idViejo": "1", "idNuevo": "1001"}
            ]
            """;

        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/equivalences", idProceso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /equivalences conflicto en idNuevo devuelve 409")
    void registrarEquivalencias_conflictoIdNuevo_devuelve409() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        when(registryPort.registrar(any())).thenThrow(
                new EquivalenceConflictException(idProceso, "PRODUCTS", "producto", "42"));

        String body = """
            [
                {"modulo": "PRODUCTS", "tabla": "producto", "idViejo": "42", "idNuevo": "9999"}
            ]
            """;

        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/equivalences", idProceso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /equivalences con lista vacía devuelve 400")
    void registrarEquivalencias_listaVacia_devuelve400() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/equivalences", idProceso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/enterprises/copy/processes/{id}/equivalences
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("GET /equivalences sin filtros devuelve 200 con lista")
    void consultarEquivalencias_sinFiltros_devuelve200() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        List<CopyEquivalenceId> lista = List.of(
                new CopyEquivalenceId(UUID.randomUUID().toString(), idProceso,
                        "PRODUCTS", "producto", "42", "1042", LocalDateTime.now())
        );
        when(queryPort.consultar(eq(idProceso), isNull(), isNull(), isNull())).thenReturn(lista);

        mockMvc.perform(get("/api/enterprises/copy/processes/{id}/equivalences", idProceso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].idViejo").value("42"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /equivalences con filtro modulo devuelve 200")
    void consultarEquivalencias_conFiltroModulo_devuelve200() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        when(queryPort.consultar(eq(idProceso), eq("PRODUCTS"), isNull(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/enterprises/copy/processes/{id}/equivalences", idProceso)
                        .param("modulo", "PRODUCTS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /equivalences proceso no encontrado devuelve 404")
    void consultarEquivalencias_procesoNoExiste_devuelve404() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        when(queryPort.consultar(any(), any(), any(), any()))
                .thenThrow(new CopyProcessNotFoundException(idProceso));

        mockMvc.perform(get("/api/enterprises/copy/processes/{id}/equivalences", idProceso))
                .andExpect(status().isNotFound());
    }
}
