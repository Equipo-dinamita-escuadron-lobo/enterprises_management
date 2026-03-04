package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import com.enterprises_management.enterprise.application.ports.input.IUploadLogoInputPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/enterprises/upload")
public class UploadLogoController {

    private final IUploadLogoInputPort uploadLogoInputPort;

    public UploadLogoController(IUploadLogoInputPort uploadLogoInputPort) {
        this.uploadLogoInputPort = uploadLogoInputPort;
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadLogo(@RequestPart("file") MultipartFile file) {
        String url = uploadLogoInputPort.upload(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}