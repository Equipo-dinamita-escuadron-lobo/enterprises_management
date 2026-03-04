package com.enterprises_management.enterprise.application.ports.services;

import com.enterprises_management.enterprise.application.ports.input.IUploadLogoInputPort;
import com.enterprises_management.enterprise.application.ports.output.IUploadLogoOutputPort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class UploadLogoService implements IUploadLogoInputPort {

    private final IUploadLogoOutputPort uploadLogoOutputPort;

    public UploadLogoService(IUploadLogoOutputPort uploadLogoOutputPort) {
        this.uploadLogoOutputPort = uploadLogoOutputPort;
    }

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Archivo vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }

        String originalName = Objects.toString(file.getOriginalFilename(), "logo");
        String ext = getExtension(originalName);

        // nombre seguro y único
        String safeName = UUID.randomUUID() + ext;

        return uploadLogoOutputPort.save(file, safeName);
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot == -1) return "";
        return filename.substring(dot).toLowerCase(Locale.ROOT);
    }
}