package com.enterprises_management.copy.infraestructure.adapters.input.rest;

import com.enterprises_management.copy.application.input.ICopyProcessCancelPort;
import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.output.IPhaseConfigRepositoryPort;
import com.enterprises_management.copy.application.services.SagaEngineService;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.dto.*;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.mapper.CopyRestMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * Controlador REST para el orquestador de procesos de copia (REQ-API-01).
 * <p>
 * Prefijo: {@code /api/enterprises/copy}
 * <p>
 * Seguridad (ADR-15, REQ-AUTH-02):
 * <ul>
 *   <li>POST /processes → Backup_Create</li>
 *   <li>GET /processes/{id}, GET /processes/{id}/events → Backup_View</li>
 *   <li>POST /processes/{id}/cancel → Backup_Cancel</li>
 *   <li>GET /configuration/phases → Backup_View</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/enterprises/copy")
@ConditionalOnProperty(name = "app.copy.orchestrator.enabled", havingValue = "true")
public class ProcesoCopiaController {

    private static final Logger log = LoggerFactory.getLogger(ProcesoCopiaController.class);

    private final ICopyProcessStartPort startPort;
    private final ICopyProcessQueryPort queryPort;
    private final ICopyProcessCancelPort cancelPort;
    private final IPhaseConfigRepositoryPort phaseConfigPort;
    private final SagaEngineService sagaEngineService;

    public ProcesoCopiaController(
            ICopyProcessStartPort startPort,
            ICopyProcessQueryPort queryPort,
            ICopyProcessCancelPort cancelPort,
            IPhaseConfigRepositoryPort phaseConfigPort,
            SagaEngineService sagaEngineService
    ) {
        this.startPort = startPort;
        this.queryPort = queryPort;
        this.cancelPort = cancelPort;
        this.phaseConfigPort = phaseConfigPort;
        this.sagaEngineService = sagaEngineService;
    }

    // -------------------------------------------------------------------------
    // POST /processes — Iniciar proceso (REQ-PROC-01)
    // -------------------------------------------------------------------------

    /**
     * Inicia un nuevo proceso de copia.
     * Responde 201 con idProceso y estado PENDIENTE.
     *
     * <p>Captura el header Authorization del request entrante y lo propaga
     * al proceso para que el saga lo reenvíe a los participantes (ADR-29, REQ-FIX-02).
     *
     * @param request        datos del proceso a iniciar (validados con Jakarta Validation)
     * @param authentication contexto de seguridad — se extrae el claim 'sub'
     * @param httpRequest    request HTTP — se extrae el Bearer token (ADR-29)
     * @return 201 con ProcesoCopiaResponse o error semántico
     */
    @PostMapping("/processes")
    @PreAuthorize("hasRole('admin_client') or hasRole('user_client') or hasRole('super_client')")
    public ResponseEntity<ProcesoCopiaResponse> iniciarProceso(
            @Valid @RequestBody IniciarProcesoRequest request,
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest httpRequest
    ) {
        String iniciadoPor = extraerSub(authentication);
        IniciarProcesoCommand command = CopyRestMapper.toCommand(request, iniciadoPor);
        CopyProcess proceso = startPort.iniciar(command);

        // ADR-29: capturar Bearer del request entrante y almacenarlo en el proceso (transient)
        String bearerToken = extraerBearerToken(httpRequest);
        proceso.setBearerToken(bearerToken);

        // Disparar la saga asíncronamente — el cliente recibe 201 de inmediato y hace polling
        final String procesoId = proceso.getId();
        sagaEngineService.registrarBearerToken(procesoId, bearerToken); // ADR-29
        CompletableFuture.runAsync(() -> {
            try {
                sagaEngineService.avanzarFase(procesoId, 1);
            } catch (Exception ex) {
                log.error("Error al ejecutar la saga para proceso {}: {}", procesoId, ex.getMessage(), ex);
            } finally {
                sagaEngineService.limpiarBearerToken(procesoId);
            }
        });

        List<CopyPhase> fases = queryPort.consultarFases(proceso.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CopyRestMapper.toResponse(proceso, fases));
    }

    // -------------------------------------------------------------------------
    // GET /processes — Listar todos los procesos
    // -------------------------------------------------------------------------

    @GetMapping("/processes")
    @PreAuthorize("hasRole('admin_client') or hasRole('user_client') or hasRole('super_client')")
    public ResponseEntity<List<ProcesoCopiaResponse>> listarProcesos() {
        List<ProcesoCopiaResponse> resultado = queryPort.listarProcesos().stream()
                .map(p -> {
                    List<CopyPhase> fases = queryPort.consultarFases(p.getId());
                    return CopyRestMapper.toResponse(p, fases);
                })
                .toList();
        return ResponseEntity.ok(resultado);
    }

    // -------------------------------------------------------------------------
    // GET /processes/{id} — Estado consolidado (REQ-API-03)
    // -------------------------------------------------------------------------

    /**
     * Consulta el estado consolidado del proceso (proceso + fases).
     *
     * @param id ID del proceso
     * @return 200 con ProcesoCopiaResponse
     */
    @GetMapping("/processes/{id}")
    @PreAuthorize("hasRole('admin_client') or hasRole('user_client') or hasRole('super_client')")
    public ResponseEntity<ProcesoCopiaResponse> consultarProceso(@PathVariable String id) {
        CopyProcess proceso = queryPort.consultarProceso(id);
        List<CopyPhase> fases = queryPort.consultarFases(id);
        return ResponseEntity.ok(CopyRestMapper.toResponse(proceso, fases));
    }

    // -------------------------------------------------------------------------
    // GET /processes/{id}/events — Eventos del proceso (REQ-EVT-01)
    // -------------------------------------------------------------------------

    /**
     * Devuelve el log de eventos del proceso ordenado por secuencia.
     *
     * @param id ID del proceso
     * @return 200 con lista de EventoProcesoResponse
     */
    @GetMapping("/processes/{id}/events")
    @PreAuthorize("hasRole('admin_client') or hasRole('user_client') or hasRole('super_client')")
    public ResponseEntity<List<EventoProcesoResponse>> consultarEventos(@PathVariable String id) {
        // Verificar existencia del proceso (lanza 404 si no existe)
        queryPort.consultarProceso(id);
        List<EventoProcesoResponse> eventos = queryPort.consultarEventos(id).stream()
                .map(CopyRestMapper::toEventoResponse)
                .toList();
        return ResponseEntity.ok(eventos);
    }

    // -------------------------------------------------------------------------
    // POST /processes/{id}/cancel — Cancelar proceso (REQ-PROC-01)
    // -------------------------------------------------------------------------

    /**
     * Cancela un proceso activo (PENDIENTE o EN_PROCESO).
     * Solo funciona en estados no-terminales; en terminales responde 409.
     *
     * @param id             ID del proceso
     * @param authentication contexto de seguridad — se extrae el claim 'sub'
     * @return 200 OK si cancelado, o error semántico
     */
    @PostMapping("/processes/{id}/cancel")
    @PreAuthorize("hasRole('admin_client') or hasRole('user_client') or hasRole('super_client')")
    public ResponseEntity<Void> cancelarProceso(
            @PathVariable String id,
            Authentication authentication
    ) {
        String canceladoPor = extraerSub(authentication);
        cancelPort.cancelar(id, canceladoPor);
        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------------
    // GET /configuration/phases — Configuración de fases (REQ-CFG-01)
    // -------------------------------------------------------------------------

    /**
     * Devuelve la matriz de configuración de módulos por fase (fases 1-4).
     *
     * @return 200 con lista de ConfiguracionFaseResponse
     */
    @GetMapping("/configuration/phases")
    @PreAuthorize("hasRole('admin_client') or hasRole('user_client') or hasRole('super_client')")
    public ResponseEntity<List<ConfiguracionFaseResponse>> listarConfiguracionFases() {
        List<ConfiguracionFaseResponse> result = IntStream.rangeClosed(1, 4)
                .boxed()
                .flatMap(fase -> phaseConfigPort.buscarActivosPorFase(fase).stream()
                        .map(CopyRestMapper::toConfigResponse))
                .toList();
        return ResponseEntity.ok(result);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Extrae el claim 'sub' (ID único de usuario en Keycloak) del JWT.
     * Si authentication es null (no debería serlo con @PreAuthorize activo), usa valor genérico.
     */
    private String extraerSub(Authentication authentication) {
        if (authentication == null) {
            return "anonymous";
        }
        // En el contexto de OAuth2/JWT el getName() retorna el sub configurado en JwtAuthConverter
        return authentication.getName();
    }

    /**
     * Extrae el token Bearer del header Authorization del request entrante (ADR-29, REQ-FIX-02).
     *
     * <p>Si el header no está presente o no tiene el prefijo "Bearer ", retorna null.
     * La ausencia del token no es un error — el proceso continúa sin propagar Authorization.
     *
     * @param httpRequest request HTTP entrante
     * @return token Bearer sin prefijo "Bearer ", o null si no está disponible
     */
    private String extraerBearerToken(jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (httpRequest == null) {
            log.warn("[ADR-29] HttpServletRequest nulo — Bearer no propagado");
            return null;
        }
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring("Bearer ".length()).trim();
    }
}
