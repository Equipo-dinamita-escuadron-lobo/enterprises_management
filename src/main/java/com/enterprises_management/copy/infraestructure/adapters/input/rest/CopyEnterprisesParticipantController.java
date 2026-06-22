package com.enterprises_management.copy.infraestructure.adapters.input.rest;

import com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto.CopyCancelResponseDto;
import com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto.CopyPhaseRequestDto;
import com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto.CopyPhaseResponseDto;
import com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto.CopyStatusResponseDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoint participante del módulo ENTERPRISES dentro de la saga de copia.
 *
 * <p>enterprises-management es a la vez orquestador y participante de la fase BASE.
 * La entidad empresa destino ya existe antes de iniciar la saga, por lo que este
 * participante confirma la fase sin copiar entidades adicionales (REQ-PARTICIPANT-01).
 */
@RestController
@RequestMapping("/api/enterprises/copy")
@ConditionalOnProperty(name = "app.copy.orchestrator.enabled", havingValue = "true")
public class CopyEnterprisesParticipantController {

    @PostMapping("/phase")
    public ResponseEntity<CopyPhaseResponseDto> executePhase(
            @RequestBody CopyPhaseRequestDto request) {

        if (request.entOrigen().equals(request.entDestino())) {
            return ResponseEntity.unprocessableEntity().body(new CopyPhaseResponseDto(
                    "ERROR_NO_REINTENTABLE",
                    0,
                    List.of(),
                    "entOrigen y entDestino no pueden ser iguales",
                    List.of(),
                    null
            ));
        }

        return ResponseEntity.ok(new CopyPhaseResponseDto(
                "COMPLETADO",
                0,
                List.of(),
                "Módulo ENTERPRISES confirmado — empresa destino ya existe",
                List.of(),
                null
        ));
    }

    @GetMapping("/{idProceso}/status")
    public ResponseEntity<CopyStatusResponseDto> getStatus(@PathVariable String idProceso) {
        return ResponseEntity.ok(new CopyStatusResponseDto(1, "COMPLETADO", 0, 1, null));
    }

    @PostMapping("/{idProceso}/cancel")
    public ResponseEntity<CopyCancelResponseDto> cancel(@PathVariable String idProceso) {
        return ResponseEntity.ok(new CopyCancelResponseDto("CANCELADO"));
    }

    @DeleteMapping("/{idProceso}/cleanup")
    public ResponseEntity<Void> cleanup(@PathVariable String idProceso) {
        return ResponseEntity.noContent().build();
    }
}
