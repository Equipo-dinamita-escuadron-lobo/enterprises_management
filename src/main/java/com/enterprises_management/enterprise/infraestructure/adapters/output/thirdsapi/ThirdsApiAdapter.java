package com.enterprises_management.enterprise.infraestructure.adapters.output.thirdsapi;

import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.enterprises_management.enterprise.application.ports.output.IThirdsApiOutputPort;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ThirdsApiAdapter implements IThirdsApiOutputPort {

    private final RestTemplate restTemplate;
    private final HttpServletRequest request;

    @Override
    public String getThirdsByEnterprise(UUID entId, int numPage, int size, String sortField, String sortOrder) {
        String url = "http://THIRDS/api/thirds/?entId=" + entId
                + "&numPage=" + numPage
                + "&size=" + size
                + "&sortField=" + sortField
                + "&sortOrder=" + sortOrder;

        String authHeader = request.getHeader("Authorization");

        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set("Authorization", authHeader);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            System.out.println("Llamando a THIRDS: " + url);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Respuesta cruda de THIRDS:");
            System.out.println(response.getBody());

            return response.getBody();
        } catch (Exception e) {
            System.out.println("Error al conectar con THIRDS: " + e.getMessage());
            throw new RuntimeException("Error al consumir el servicio THIRDS", e);
        }
    }
}