package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/enterprises/test")
public class TestController {

    @Operation(summary = "Prueba de acceso para administradores", description = "Permite verificar que un usuario con el rol de administrador ('admin_client') puede acceder a este endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acceso exitoso al recurso", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "403", description = "Acceso denegado, permisos insuficientes")
    })
    @GetMapping("/testAdmin")
    @PreAuthorize("hasRole('admin_client')")
    public String test() {
        return "Test for Admin";
    }

    @Operation(summary = "Prueba de acceso para usuarios y administradores", description = "Permite verificar que un usuario con los roles 'user_client' o 'admin_client' puede acceder a este endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acceso exitoso al recurso", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "403", description = "Acceso denegado, permisos insuficientes")
    })
    @GetMapping("/testUser")
    @PreAuthorize("hasRole('user_client') or hasRole('admin_client')")
    public String testUser() {
        return "Test for User";
    }

    @Operation(summary = "Prueba básica de disponibilidad del servidor", description = "Endpoint público para verificar que el servidor está disponible.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servidor disponible", content = @Content(mediaType = "text/plain"))
    })
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
