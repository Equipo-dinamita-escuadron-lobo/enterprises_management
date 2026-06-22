package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import com.enterprises_management.enterprise.application.ports.input.IPreUploadLogoInputPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/enterprises/upload/logo")
public class EnterprisePreUploadLogoController {

    private final IPreUploadLogoInputPort preUploadLogoInputPort;

    @Value("${app.upload.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    public EnterprisePreUploadLogoController(IPreUploadLogoInputPort preUploadLogoInputPort) {
        this.preUploadLogoInputPort = preUploadLogoInputPort;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> preUploadLogo(
            @RequestPart("file") MultipartFile file) {

        String logoKey = preUploadLogoInputPort.preUploadLogo(file);

        return ResponseEntity.ok(Map.of("url", publicBaseUrl + "/logos/" + logoKey));
    }
}
