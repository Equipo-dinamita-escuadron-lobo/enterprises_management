package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<List<ValidationError>> handleValidationExceptions(BindException ex) {
        BindingResult result = ex.getBindingResult();
        List<ValidationError> errors = new ArrayList<>();
        for (FieldError fieldError : result.getFieldErrors()) {
            errors.add(new ValidationError(fieldError.getField(), fieldError.getDefaultMessage()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    public static class ValidationError {
        private final String field;
        private final String message;

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
    
}
