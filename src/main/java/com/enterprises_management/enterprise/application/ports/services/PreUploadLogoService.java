package com.enterprises_management.enterprise.application.ports.services;

import com.enterprises_management.enterprise.application.ports.input.IPreUploadLogoInputPort;
import com.enterprises_management.enterprise.application.ports.output.ILogoStorageOutputPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PreUploadLogoService implements IPreUploadLogoInputPort {

    private final ILogoStorageOutputPort logoStorageOutputPort;

    public PreUploadLogoService(
            @Qualifier("localLogoStorageAdapter") ILogoStorageOutputPort logoStorageOutputPort) {
        this.logoStorageOutputPort = logoStorageOutputPort;
    }

    @Override
    public String preUploadLogo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }
        String contentType = file.getContentType();
        if (contentType == null || !isValidContentType(contentType)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido. Solo se permiten imágenes PNG, JPG, JPEG, SVG");
        }
        return logoStorageOutputPort.save(file);
    }

    private boolean isValidContentType(String contentType) {
        return contentType.equals("image/png") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/jpeg") ||
                contentType.equals("image/svg+xml");
    }
}
