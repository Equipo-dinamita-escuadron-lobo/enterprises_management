package com.enterprises_management.copy.infraestructure.adapters.input.rest;

import com.enterprises_management.copy.application.input.IEquivalenceQueryPort;
import com.enterprises_management.copy.application.input.IEquivalenceRegistryPort;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.dto.EquivalenciaItemRequest;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.dto.EquivalenciaResponse;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.mapper.CopyRestMapper;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el registro y consulta de equivalencias de IDs (REQ-EQ-01, REQ-EQ-02).
 * <p>
 * Prefijo: {@code /api/enterprises/copy/processes/{idProceso}/equivalences}
 * <p>
 * Seguridad (ADR-15, REQ-AUTH-02):
 * Los endpoints de equivalencias son de uso interno del orquestador y
 * requieren solo autenticación básica (JWT válido), sin permiso específico.
 */
@RestController
@RequestMapping("/api/enterprises/copy/processes/{idProceso}/equivalences")
@ConditionalOnProperty(name = "app.copy.orchestrator.enabled", havingValue = "true")
public class EquivalenciaController {

    private final IEquivalenceRegistryPort registryPort;
    private final IEquivalenceQueryPort queryPort;

    public EquivalenciaController(
            IEquivalenceRegistryPort registryPort,
            IEquivalenceQueryPort queryPort
    ) {
        this.registryPort = registryPort;
        this.queryPort = queryPort;
    }

    // -------------------------------------------------------------------------
    // POST /{idProceso}/equivalences — Registrar equivalencias (REQ-EQ-01, ADR-9)
    // -------------------------------------------------------------------------

    /**
     * Registra una lista de equivalencias de IDs para un proceso.
     * <p>
     * Idempotente (ADR-9):
     * <ul>
     *   <li>Si idNuevo es igual al existente → omitida, devuelve 200</li>
     *   <li>Si idNuevo es diferente → 409 EquivalenceConflictException</li>
     *   <li>Si se insertan nuevas → 201 con conteo</li>
     * </ul>
     *
     * @param idProceso ID del proceso (de la URL)
     * @param items     lista de equivalencias a registrar (mínimo 1 elemento)
     * @return 201 con conteo si hubo inserciones, 200 si todo era idempotente
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> registrarEquivalencias(
            @PathVariable String idProceso,
            @RequestBody List<@Valid EquivalenciaItemRequest> items
    ) {
        if (items == null || items.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La lista de equivalencias no puede estar vacía"));
        }

        List<CopyEquivalenceId> equivalencias = items.stream()
                .map(item -> CopyRestMapper.toEquivalencia(idProceso, item))
                .toList();

        int insertadas = registryPort.registrar(equivalencias);

        // ADR-9: si insertadas == 0 todas eran idempotentes → 200 OK
        // Si insertadas > 0 hay nuevas registradas → 201 Created
        if (insertadas == 0) {
            return ResponseEntity.ok(Map.of("insertadas", 0, "mensaje", "Todas las equivalencias ya existían"));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("insertadas", insertadas));
    }

    // -------------------------------------------------------------------------
    // GET /{idProceso}/equivalences — Consultar equivalencias (REQ-EQ-02)
    // -------------------------------------------------------------------------

    /**
     * Consulta equivalencias con filtros opcionales.
     * Sin filtros devuelve todas las equivalencias del proceso.
     * Nunca devuelve 404 por falta de resultados — solo 200 con lista vacía.
     *
     * @param idProceso ID del proceso (URL)
     * @param modulo    filtro opcional por módulo
     * @param tabla     filtro opcional por tabla
     * @param idViejo   filtro opcional por ID original
     * @return 200 con lista de EquivalenciaResponse (puede ser vacía)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquivalenciaResponse>> consultarEquivalencias(
            @PathVariable String idProceso,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String tabla,
            @RequestParam(required = false) String idViejo
    ) {
        List<EquivalenciaResponse> result = queryPort.consultar(idProceso, modulo, tabla, idViejo)
                .stream()
                .map(CopyRestMapper::toEquivalenciaResponse)
                .toList();
        return ResponseEntity.ok(result);
    }
}
