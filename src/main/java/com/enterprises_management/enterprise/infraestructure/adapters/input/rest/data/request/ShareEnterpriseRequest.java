package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record ShareEnterpriseRequest(
        @NotBlank String enterpriseId,
        @NotNull @NotEmpty List<String> emails,
        @NotBlank @Pattern(regexp = "^(estudiante|profesor)$",
                message = "El rol debe ser 'estudiante' o 'profesor'") String role
) {}
