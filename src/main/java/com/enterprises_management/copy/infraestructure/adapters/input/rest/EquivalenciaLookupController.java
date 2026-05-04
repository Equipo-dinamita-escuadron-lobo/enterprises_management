package com.enterprises_management.copy.infraestructure.adapters.input.rest;

import com.enterprises_management.copy.application.input.IEquivalenciaLookupInputPort;
import com.enterprises_management.copy.domain.models.EquivalenciaLookupRequest;
import com.enterprises_management.copy.domain.models.EquivalenciaLookupResponse;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.dto.LookupRequestDto;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.dto.LookupResponseDto;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para el endpoint pull selectivo de equivalencias (REQ-LOOKUP-01, ADR-35).
 *
 * <p>Extiende el espacio de rutas de equivalencias del orquestador con un endpoint POST dedicado
 * que permite a participantes Fase 3 consultar en batch los IDs nuevos para un conjunto de
 * IDs viejos, sin recibir todas las equivalencias del proceso (evita request rico pesado).
 *
 * <p>Ruta: {@code POST /api/enterprises/copy/processes/{idProceso}/equivalences/lookup}
 *
 * <p>Seguridad (ADR-35): solo autenticación JWT válida, sin permiso Backup_* adicional.
 *
 * <p>Límite de batch: configurable via {@code app.copy.orchestrator.lookup.max-batch} (default 1000).
 * Si se supera, el servicio lanza {@link IllegalArgumentException} → 400 Bad Request via
 * {@link com.enterprises_management.copy.infraestructure.adapters.input.rest.advice.CopyExceptionHandler}.
 */
@RestController
@RequestMapping("/api/enterprises/copy/processes/{idProceso}/equivalences")
@ConditionalOnProperty(name = "app.copy.orchestrator.enabled", havingValue = "true")
public class EquivalenciaLookupController {

    private final IEquivalenciaLookupInputPort lookupPort;

    public EquivalenciaLookupController(IEquivalenciaLookupInputPort lookupPort) {
        this.lookupPort = lookupPort;
    }

    /**
     * Lookup selectivo de equivalencias por batch (REQ-LOOKUP-01, ADR-35).
     *
     * <p>Recibe una lista de ítems {@code {modulo, tabla, idsViejos[]}} y devuelve:
     * <ul>
     *   <li>{@code mappings}: Map de {@code "MODULO:tabla:idViejo"} → idNuevo para los encontrados.</li>
     *   <li>{@code notFound}: lista de IDs sin equivalencia registrada en el proceso.</li>
     * </ul>
     *
     * @param idProceso  ID del proceso de copia (de la URL)
     * @param requestDto DTO de request validado (requests no puede estar vacío)
     * @return 200 con LookupResponseDto | 400 si requests vacío o total IDs &gt; max-batch | 401 sin JWT
     */
    @PostMapping("/lookup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LookupResponseDto> lookup(
            @PathVariable String idProceso,
            @Valid @RequestBody LookupRequestDto requestDto
    ) {
        EquivalenciaLookupRequest domain = toDomain(requestDto);
        EquivalenciaLookupResponse response = lookupPort.lookup(idProceso, domain);
        return ResponseEntity.ok(LookupResponseDto.fromDomain(response));
    }

    // -------------------------------------------------------------------------
    // Mapper REST → Dominio
    // -------------------------------------------------------------------------

    private EquivalenciaLookupRequest toDomain(LookupRequestDto dto) {
        List<EquivalenciaLookupRequest.LookupItem> items = dto.getRequests().stream()
            .map(item -> new EquivalenciaLookupRequest.LookupItem(
                item.getModulo(),
                item.getTabla(),
                item.getIdsViejos() != null ? item.getIdsViejos() : List.of()
            ))
            .collect(Collectors.toList());
        return new EquivalenciaLookupRequest(items);
    }
}
