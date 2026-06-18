package com.enterprises_management.enterprise.infraestructure.adapters.output.keycloak;

import com.enterprises_management.enterprise.application.ports.output.IKeycloakRolePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class KeycloakRoleAdapter implements IKeycloakRolePort {

    private final WebClient webClient;
    private final String serverUrl;
    private final String realm;
    private final String masterRealm;
    private final String clientId;
    private final String username;
    private final String password;

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    public KeycloakRoleAdapter(
            @Value("${keycloak.admin.server-url}") String serverUrl,
            @Value("${keycloak.admin.realm}") String realm,
            @Value("${keycloak.admin.master-realm}") String masterRealm,
            @Value("${keycloak.admin.client-id}") String clientId,
            @Value("${keycloak.admin.username}") String username,
            @Value("${keycloak.admin.password}") String password) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.masterRealm = masterRealm;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.webClient = WebClient.builder().build();
    }

    private static final java.util.Map<String, String> ROLE_MAP = java.util.Map.of(
        "estudiante",    "super_client",
        "profesor",      "user_client",
        "administrador", "admin_client"
    );

    @Override
    public boolean assignRoleByEmail(String email, String role) {
        try {
            String keycloakRole = ROLE_MAP.getOrDefault(role.toLowerCase(), role);
            String token = getAdminToken();
            String userId = findUserIdByEmail(email, token);
            if (userId == null) {
                log.info("Usuario {} no encontrado en Keycloak — pendiente de registro", email);
                return false;
            }
            String roleId = getRoleId(keycloakRole, token);
            if (roleId == null) {
                log.warn("Rol '{}' (keycloak: '{}') no existe en Keycloak", role, keycloakRole);
                return false;
            }
            assignRole(userId, roleId, keycloakRole, token);
            log.info("Rol '{}' asignado a {} en Keycloak", keycloakRole, email);
            return true;
        } catch (Exception e) {
            log.error("Error al asignar rol Keycloak para {}: {}", email, e.getMessage());
            return false;
        }
    }

    private String getAdminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("username", username);
        form.add("password", password);

        Map<String, Object> response = webClient.post()
                .uri(serverUrl + "/realms/" + masterRealm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("No se pudo obtener token de admin de Keycloak");
        }
        return (String) response.get("access_token");
    }

    private String findUserIdByEmail(String email, String token) {
        // Intenta primero por campo email, luego por username (en Keycloak son campos distintos)
        String byEmail = queryUsers("email", email, token);
        if (byEmail != null) return byEmail;
        return queryUsers("username", email, token);
    }

    private String queryUsers(String field, String value, String token) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(serverUrl + "/admin/realms/" + realm + "/users")
                .queryParam(field, value)
                .queryParam("exact", "true")
                .build()
                .toUriString();

        List<Map<String, Object>> users = webClient.get()
                .uri(uri)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();

        if (users == null || users.isEmpty()) return null;
        return users.stream()
                .filter(u -> value.equalsIgnoreCase((String) u.get("email"))
                          || value.equalsIgnoreCase((String) u.get("username")))
                .map(u -> (String) u.get("id"))
                .findFirst()
                .orElse(null);
    }

    // Obtiene el UUID interno del cliente (distinto al clientId textual)
    private String getClientUuid(String clientId, String token) {
        try {
            String uri = UriComponentsBuilder
                    .fromHttpUrl(serverUrl + "/admin/realms/" + realm + "/clients")
                    .queryParam("clientId", clientId)
                    .build()
                    .toUriString();

            List<Map<String, Object>> clients = webClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();

            if (clients == null || clients.isEmpty()) return null;
            return (String) clients.get(0).get("id");
        } catch (Exception e) {
            log.warn("No se pudo obtener UUID del cliente '{}': {}", clientId, e.getMessage());
            return null;
        }
    }

    private String getRoleId(String roleName, String token) {
        // Primero busca como client role bajo resourceId (microservices_client)
        String clientUuid = getClientUuid(resourceId, token);
        if (clientUuid != null) {
            try {
                Map<String, Object> role = webClient.get()
                        .uri(serverUrl + "/admin/realms/" + realm + "/clients/" + clientUuid + "/roles/" + roleName)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .block();
                if (role != null) return (String) role.get("id");
            } catch (WebClientResponseException.NotFound ignored) {}
        }
        // Fallback: realm role
        try {
            Map<String, Object> role = webClient.get()
                    .uri(serverUrl + "/admin/realms/" + realm + "/roles/" + roleName)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            return role != null ? (String) role.get("id") : null;
        } catch (WebClientResponseException.NotFound e) {
            return null;
        }
    }

    private void assignRole(String userId, String roleId, String roleName, String token) {
        List<Map<String, String>> roles = List.of(Map.of("id", roleId, "name", roleName));

        // Intenta asignar como client role primero
        String clientUuid = getClientUuid(resourceId, token);
        if (clientUuid != null) {
            try {
                webClient.post()
                        .uri(serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/clients/" + clientUuid)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(roles)
                        .retrieve()
                        .toBodilessEntity()
                        .block();
                return;
            } catch (Exception e) {
                log.warn("No se pudo asignar client role, intentando realm role: {}", e.getMessage());
            }
        }
        // Fallback: realm role
        webClient.post()
                .uri(serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roles)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
