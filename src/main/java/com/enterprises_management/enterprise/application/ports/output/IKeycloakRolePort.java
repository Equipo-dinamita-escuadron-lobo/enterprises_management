package com.enterprises_management.enterprise.application.ports.output;

public interface IKeycloakRolePort {

    /**
     * Busca al usuario por email en Keycloak y le asigna el rol indicado.
     * @return true si el rol fue asignado, false si el usuario no existe en Keycloak.
     */
    boolean assignRoleByEmail(String email, String role);

    String getUserIdByEmail(String email);
}
