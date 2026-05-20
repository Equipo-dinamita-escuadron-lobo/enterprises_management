package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import com.enterprises_management.enterprise.application.ports.input.IUploadLogoInputPort;
import com.enterprises_management.enterprise.application.ports.input.LogoResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/enterprises/{enterpriseId}/logo")
public class EnterpriseLogoController {

    private final IUploadLogoInputPort uploadLogoInputPort;

    public EnterpriseLogoController(IUploadLogoInputPort uploadLogoInputPort) {
        this.uploadLogoInputPort = uploadLogoInputPort;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadLogo(
            @PathVariable String enterpriseId,
            @RequestPart("file") MultipartFile file) {

        String logoKey = uploadLogoInputPort.uploadLogo(enterpriseId, file);

        return ResponseEntity.ok(
                Map.of(
                        "logoKey", logoKey,
                        "message", "Logo subido exitosamente"));
    }

    @GetMapping
    public ResponseEntity<Resource> getLogo(@PathVariable String enterpriseId) {
        try {
            LogoResource logoResource = uploadLogoInputPort.getLogo(enterpriseId);

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_TYPE,
                            logoResource.getContentType() != null
                                    ? logoResource.getContentType()
                                    : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .body(logoResource.getResource());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteLogo(@PathVariable String enterpriseId) {
        uploadLogoInputPort.deleteLogo(enterpriseId);

        return ResponseEntity.ok(
                Map.of("message", "Logo eliminado exitosamente"));
    }
}