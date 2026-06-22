package com.enterprises_management.copy.infraestructure.adapters.input.rest;

import com.enterprises_management.copy.application.input.ICopyProcessCancelPort;
import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.services.SagaEngineService;
import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.exceptions.CopyProcessCancelDeniedException;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.exceptions.DuplicateActiveProcessException;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.interceptor.TenantInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios del controlador de procesos de copia usando @WebMvcTest (REQ-API-01).
 * TDD ESTRICTO: estos tests deben compilar en ROJO antes de crear el controlador.
 *
 * <p>Se importa SecurityConfig para activar @EnableMethodSecurity y que @PreAuthorize
 * funcione en el contexto @WebMvcTest.
 */
@WebMvcTest(controllers = ProcesoCopiaController.class)
@Import({
    com.enterprises_management.copy.infraestructure.adapters.input.rest.advice.CopyExceptionHandler.class,
    com.enterprises_management.enterprise.infraestructure.security.SecurityConfig.class
})
@ActiveProfiles("test")
@TestPropertySource(properties = "app.copy.orchestrator.enabled=true")
class ProcesoCopiaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICopyProcessStartPort startPort;

    @MockBean
    private ICopyProcessQueryPort queryPort;

    @MockBean
    private ICopyProcessCancelPort cancelPort;

    /** Necesario para OAuth2 resource server (SecurityConfig). */
    @MockBean
    private JwtDecoder jwtDecoder;

    /** Necesario para satisfacer WebConfiguration que requiere TenantInterceptor (multi-tenancy). */
    @MockBean
    private TenantInterceptor tenantInterceptor;

    /** Necesario para SecurityConfig. */
    @MockBean
    private com.enterprises_management.enterprise.infraestructure.security.JwtAuthConverter jwtAuthConverter;

    /** Puerto de configuración de fases — inyectado en ProcesoCopiaController. */
    @MockBean
    private com.enterprises_management.copy.application.output.IPhaseConfigRepositoryPort phaseConfigPort;

    /** Necesario porque ProcesoCopiaController dispara la saga async. */
    @MockBean
    private SagaEngineService sagaEngineService;

    /** Necesario porque ProcesoCopiaController.eliminarProceso usa deletePort. */
    @MockBean
    private com.enterprises_management.copy.application.input.ICopyProcessDeletePort deletePort;

    /** Necesario porque ProcesoCopiaController.iniciarProceso (tipo DUPLICATE) usa duplicateSetupPort. */
    @MockBean
    private com.enterprises_management.copy.application.output.IEnterpriseDuplicateSetupPort duplicateSetupPort;

    // -------------------------------------------------------------------------
    // POST /api/enterprises/copy/processes
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "admin_client")
    @DisplayName("POST /processes con body válido DUPLICATE devuelve 201 con idProceso")
    void iniciarProceso_bodyValidoDuplicate_devuelve201() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        UUID empresaOrigen = UUID.randomUUID();

        CopyProcess proceso = mockProceso(idProceso, empresaOrigen, CopyProcessType.DUPLICATE);
        when(startPort.iniciar(any())).thenReturn(proceso);
        when(duplicateSetupPort.crearEmpresaDestino(any(), any(), any())).thenReturn(UUID.randomUUID().toString());

        String body = """
            {
                "tipo": "DUPLICATE",
                "empresaOrigen": "%s",
                "empresaDestino": {"nombre": "Empresa Destino Test"}
            }
            """.formatted(empresaOrigen);

        mockMvc.perform(post("/api/enterprises/copy/processes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProceso").value(idProceso))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @WithMockUser(roles = "admin_client")
    @DisplayName("POST /processes con tipo inválido devuelve 400")
    void iniciarProceso_tipoInvalido_devuelve400() throws Exception {
        String body = """
            {
                "tipo": "TIPO_INEXISTENTE",
                "empresaOrigen": "%s"
            }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/enterprises/copy/processes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "admin_client")
    @DisplayName("POST /processes sin empresaOrigen devuelve 400")
    void iniciarProceso_sinEmpresaOrigen_devuelve400() throws Exception {
        String body = """
            {
                "tipo": "BACKUP"
            }
            """;

        mockMvc.perform(post("/api/enterprises/copy/processes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "admin_client")
    @DisplayName("POST /processes cuando ya existe proceso activo devuelve 409")
    void iniciarProceso_procesoActivoExistente_devuelve409() throws Exception {
        UUID empresaOrigen = UUID.randomUUID();
        when(startPort.iniciar(any())).thenThrow(new DuplicateActiveProcessException(empresaOrigen));

        String body = """
            {
                "tipo": "BACKUP",
                "empresaOrigen": "%s"
            }
            """.formatted(empresaOrigen);

        mockMvc.perform(post("/api/enterprises/copy/processes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /processes sin autenticación devuelve 401")
    void iniciarProceso_sinAutenticacion_devuelve401() throws Exception {
        String body = """
            {
                "tipo": "BACKUP",
                "empresaOrigen": "%s"
            }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/enterprises/copy/processes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "Backup_View")
    @DisplayName("POST /processes con usuario sin Backup_Create devuelve 403")
    void iniciarProceso_sinAuthorityBackupCreate_devuelve403() throws Exception {
        String body = """
            {
                "tipo": "BACKUP",
                "empresaOrigen": "%s"
            }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/enterprises/copy/processes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "admin_client")
    @DisplayName("POST /processes con tipo=RESTORE sin backupRef devuelve 400 (REQ-API-02)")
    void iniciarProceso_restoreSinBackupRef_devuelve400() throws Exception {
        String body = """
            {
                "tipo": "RESTORE",
                "empresaOrigen": "%s"
            }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/enterprises/copy/processes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "admin_client")
    @DisplayName("POST /processes con tipo=RESTORE y backupRef válido devuelve 201 (REQ-API-02)")
    void iniciarProceso_restoreConBackupRef_devuelve201() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        UUID empresaOrigen = UUID.randomUUID();

        CopyProcess proceso = mockProceso(idProceso, empresaOrigen, CopyProcessType.RESTORE);
        when(startPort.iniciar(any())).thenReturn(proceso);

        String body = """
            {
                "tipo": "RESTORE",
                "empresaOrigen": "%s",
                "backupRef": "backup-2026-04-27.zip"
            }
            """.formatted(empresaOrigen);

        mockMvc.perform(post("/api/enterprises/copy/processes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    // -------------------------------------------------------------------------
    // GET /api/enterprises/copy/processes/{id}
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "admin_client")
    @DisplayName("GET /processes/{id} proceso existente devuelve 200")
    void consultarProceso_procesoExiste_devuelve200() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        UUID empresaOrigen = UUID.randomUUID();

        when(queryPort.consultarProceso(idProceso))
                .thenReturn(mockProceso(idProceso, empresaOrigen, CopyProcessType.BACKUP));
        when(queryPort.consultarFases(idProceso)).thenReturn(List.of());

        mockMvc.perform(get("/api/enterprises/copy/processes/{id}", idProceso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProceso").value(idProceso));
    }

    @Test
    @WithMockUser(roles = "admin_client")
    @DisplayName("GET /processes/{id} proceso inexistente devuelve 404")
    void consultarProceso_procesoNoExiste_devuelve404() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        when(queryPort.consultarProceso(idProceso))
                .thenThrow(new CopyProcessNotFoundException(idProceso));

        mockMvc.perform(get("/api/enterprises/copy/processes/{id}", idProceso))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /api/enterprises/copy/processes/{id}/cancel
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "admin_client")
    @DisplayName("POST /processes/{id}/cancel proceso cancelable devuelve 200")
    void cancelarProceso_procesoActivo_devuelve200() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        doNothing().when(cancelPort).cancelar(eq(idProceso), anyString());

        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/cancel", idProceso)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "admin_client")
    @DisplayName("POST /processes/{id}/cancel en estado terminal devuelve 409")
    void cancelarProceso_estadoTerminal_devuelve409() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        doThrow(new CopyProcessCancelDeniedException(idProceso, ProcessState.COMPLETADO))
                .when(cancelPort).cancelar(eq(idProceso), anyString());

        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/cancel", idProceso)
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "admin_client")
    @DisplayName("POST /processes/{id}/cancel proceso no encontrado devuelve 404")
    void cancelarProceso_procesoNoExiste_devuelve404() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        doThrow(new CopyProcessNotFoundException(idProceso))
                .when(cancelPort).cancelar(eq(idProceso), anyString());

        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/cancel", idProceso)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "Backup_Create")
    @DisplayName("POST /processes/{id}/cancel sin Backup_Cancel devuelve 403")
    void cancelarProceso_sinAuthorityBackupCancel_devuelve403() throws Exception {
        String idProceso = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/enterprises/copy/processes/{id}/cancel", idProceso)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // GET /api/enterprises/copy/processes/{id}/events
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "admin_client")
    @DisplayName("GET /processes/{id}/events devuelve lista de eventos")
    void consultarEventos_procesoExiste_devuelve200() throws Exception {
        String idProceso = UUID.randomUUID().toString();
        UUID empresaOrigen = UUID.randomUUID();

        when(queryPort.consultarProceso(idProceso))
                .thenReturn(mockProceso(idProceso, empresaOrigen, CopyProcessType.BACKUP));
        when(queryPort.consultarEventos(idProceso)).thenReturn(List.of(
                CopyProcessEvent.crear(idProceso, CopyEventType.PROCESO_COPIA_INICIADO, "{}")
        ));

        mockMvc.perform(get("/api/enterprises/copy/processes/{id}/events", idProceso))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // -------------------------------------------------------------------------
    // GET /api/enterprises/copy/configuration/phases
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "admin_client")
    @DisplayName("GET /configuration/phases devuelve lista de configuraciones")
    void listarConfiguracionFases_devuelve200() throws Exception {
        when(phaseConfigPort.buscarActivosPorFase(anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/api/enterprises/copy/configuration/phases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CopyProcess mockProceso(String id, UUID empresaOrigen, CopyProcessType tipo) {
        return CopyProcess.restaurar(id, tipo, empresaOrigen, null, null,
                java.time.LocalDateTime.now(), "user-test");
    }
}
