package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import com.enterprises_management.enterprise.application.ports.output.IUploadLogoOutputPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;

@Component
public class UploadLogoAdapter implements IUploadLogoOutputPort {

    // Carpeta física donde se guardan los logos
    @Value("${app.upload.logo-dir:uploads/logos}")
    private String logoDir;

    // Base URL pública (si consumes por gateway suele ser http://localhost:8080)
    @Value("${app.upload.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    @Override
    public String save(MultipartFile file, String fileName) {
        try {
            Path dir = Paths.get(logoDir);
            Files.createDirectories(dir);

            Path target = dir.resolve(fileName);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            // URL para que el front pueda pintar el logo
            return publicBaseUrl + "/logos/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar el logo", e);
        }
    }
}