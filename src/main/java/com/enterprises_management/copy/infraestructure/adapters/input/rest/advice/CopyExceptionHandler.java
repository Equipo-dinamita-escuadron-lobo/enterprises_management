package com.enterprises_management.copy.infraestructure.adapters.input.rest.advice;

import com.enterprises_management.copy.domain.exceptions.BackupCorruptedException;
import com.enterprises_management.copy.domain.exceptions.BackupNotFoundException;
import com.enterprises_management.copy.domain.exceptions.BackupTooLargeException;
import com.enterprises_management.copy.domain.exceptions.CopyProcessCancelDeniedException;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.exceptions.DuplicateActiveProcessException;
import com.enterprises_management.copy.domain.exceptions.EquivalenceConflictException;
import com.enterprises_management.copy.domain.exceptions.InvalidPhaseTransitionException;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

/**
 * Manejador centralizado de excepciones para el bounded context copy (ADR-10).
 * Mapea excepciones de dominio a respuestas HTTP con códigos COPY_*.
 *
 * <p>Acotado a los paquetes del bounded context copy para no interferir con
 * los manejadores existentes del contexto enterprise.
 */
@RestControllerAdvice(basePackages = "com.enterprises_management.copy")
public class CopyExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CopyExceptionHandler.class);

    /**
     * 404 — Proceso no encontrado (COPY_404).
     */
    @ExceptionHandler(CopyProcessNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(CopyProcessNotFoundException ex) {
        log.warn("Proceso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("COPY_404", ex.getMessage(), nuevoCorrelationId()));
    }

    /**
     * 409 — Transición de estado inválida (COPY_TX_INVALID).
     */
    @ExceptionHandler(InvalidPhaseTransitionException.class)
    public ResponseEntity<ErrorResponse> handleTransicionInvalida(InvalidPhaseTransitionException ex) {
        log.warn("Transición inválida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("COPY_TX_INVALID", ex.getMessage(), nuevoCorrelationId()));
    }

    /**
     * 409 — Cancelación denegada porque el proceso está en estado terminal (COPY_CANCEL_DENIED).
     */
    @ExceptionHandler(CopyProcessCancelDeniedException.class)
    public ResponseEntity<ErrorResponse> handleCancelDenied(CopyProcessCancelDeniedException ex) {
        log.warn("Cancelación denegada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("COPY_CANCEL_DENIED", ex.getMessage(), nuevoCorrelationId()));
    }

    /**
     * 409 — Ya existe proceso activo para la empresa origen (COPY_DUPLICATE_ACTIVE).
     */
    @ExceptionHandler(DuplicateActiveProcessException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateActive(DuplicateActiveProcessException ex) {
        log.warn("Proceso activo duplicado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("COPY_DUPLICATE_ACTIVE", ex.getMessage(), nuevoCorrelationId()));
    }

    /**
     * 409 — Conflicto en idNuevo al registrar equivalencia (COPY_EQUIV_CONFLICT).
     */
    @ExceptionHandler(EquivalenceConflictException.class)
    public ResponseEntity<ErrorResponse> handleEquivalenceConflict(EquivalenceConflictException ex) {
        log.warn("Conflicto de equivalencia: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("COPY_EQUIV_CONFLICT", ex.getMessage(), nuevoCorrelationId()));
    }

    /**
     * 400 — Validación de campos del request (MethodArgumentNotValidException).
     * Agrega detalle de cada campo con error (ADR-10 — detalles de validación).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        List<ErrorResponse.CampoErrorResponse> errores = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String campo = (error instanceof FieldError fe) ? fe.getField() : error.getObjectName();
                    return new ErrorResponse.CampoErrorResponse(campo, error.getDefaultMessage());
                })
                .toList();

        String correlationId = nuevoCorrelationId();
        log.warn("Error de validación [{}]: {} campo(s) con error", correlationId, errores.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "COPY_VALIDATION_ERROR",
                        "Error de validación en los campos del request",
                        java.time.LocalDateTime.now(),
                        correlationId,
                        errores
                ));
    }

    /**
     * 404 — Backup no encontrado en disco o backupRef inválido (COPY_BACKUP_404).
     * Lanzado por ZipBackupReaderAdapter y BackupController (REQ-DOWNLOAD-01, REQ-RESTORE-01).
     */
    @ExceptionHandler(BackupNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBackupNotFound(BackupNotFoundException ex) {
        log.warn("Backup no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("COPY_BACKUP_404", ex.getMessage(), nuevoCorrelationId()));
    }

    /**
     * 422 — Backup corrupto o con versión no soportada (COPY_BACKUP_CORRUPTED).
     * Lanzado por ZipBackupReaderAdapter y RestoreService (REQ-RESTORE-01, ADR-45).
     */
    @ExceptionHandler(BackupCorruptedException.class)
    public ResponseEntity<ErrorResponse> handleBackupCorrupted(BackupCorruptedException ex) {
        log.warn("Backup corrupto o inválido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("COPY_BACKUP_CORRUPTED", ex.getMessage(), nuevoCorrelationId()));
    }

    /**
     * 413 — Archivo de backup supera el tamaño máximo configurado (COPY_BACKUP_TOO_LARGE).
     * Lanzado por BackupController (REQ-DOWNLOAD-01, ADR-47).
     */
    @ExceptionHandler(BackupTooLargeException.class)
    public ResponseEntity<ErrorResponse> handleBackupTooLarge(BackupTooLargeException ex) {
        log.warn("Backup excede tamaño máximo: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse("COPY_BACKUP_TOO_LARGE", ex.getMessage(), nuevoCorrelationId()));
    }

    /**
     * 400 — Argumento inválido en el request (IllegalArgumentException).
     * Cubre, entre otros, el límite de batch del endpoint lookup (REQ-LOOKUP-01).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleArgumentoInvalido(IllegalArgumentException ex) {
        log.warn("Argumento inválido en el request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("COPY_BAD_REQUEST", ex.getMessage(), nuevoCorrelationId()));
    }

    /**
     * 400 — Errores de deserialización JSON (tipo de enum inválido, etc.).
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMensajeNoLegible(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.warn("Request no legible: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("COPY_BAD_REQUEST",
                        "El cuerpo de la solicitud no es válido o contiene un tipo de dato incorrecto",
                        nuevoCorrelationId()));
    }

    /**
     * 500 — Cualquier excepción no contemplada (COPY_INTERNAL).
     * NOTA: Las excepciones de seguridad (AccessDeniedException, AuthenticationException)
     * NO se manejan aquí — se dejan propagar para que Spring Security las procese (403/401).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Re-lanzar excepciones de seguridad para que Spring Security las maneje (ADR-10)
        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            throw (org.springframework.security.access.AccessDeniedException) ex;
        }
        if (ex instanceof org.springframework.security.core.AuthenticationException) {
            throw (org.springframework.security.core.AuthenticationException) ex;
        }
        String correlationId = nuevoCorrelationId();
        log.error("Error interno no esperado [correlationId={}]: {}", correlationId, ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("COPY_INTERNAL",
                        "Error interno del servidor. Referencia: " + correlationId,
                        correlationId));
    }

    private static String nuevoCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
