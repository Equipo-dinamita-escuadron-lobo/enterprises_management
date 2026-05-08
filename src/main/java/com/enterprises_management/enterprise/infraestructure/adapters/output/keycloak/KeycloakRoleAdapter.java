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

    @Override
    public boolean assignRoleByEmail(String email, String role) {
        try {
            String token = getAdminToken();
            String userId = findUserIdByEmail(email, token);
            if (userId == null) {
                log.info("Usuario {} no encontrado en Keycloak — pendiente de registro", email);
                return false;
            }
            String roleId = getRoleId(role, token);
            if (roleId == null) {
                log.warn("Rol '{}' no existe en Keycloak", role);
                return false;
            }
            assignRole(userId, roleId, role, token);
            log.info("Rol '{}' asignado a {} en Keycloak", role, email);
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
        List<Map<String, Object>> users = webClient.get()
                .uri(serverUrl + "/admin/realms/" + realm + "/users?email=" + email + "&exact=true")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();

        if (users == null || users.isEmpty()) return null;
        return users.stream()
                .filter(u -> email.equalsIgnoreCase((String) u.get("email")))
                .map(u -> (String) u.get("id"))
                .findFirst()
                .orElse(null);
    }

    private String getRoleId(String roleName, String token) {
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
