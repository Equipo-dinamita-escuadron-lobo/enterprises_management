package com.enterprises_management.enterprise.application.ports.output;

import org.springframework.web.multipart.MultipartFile;

public interface IUploadLogoOutputPort {
    /**
     * Guarda físicamente el archivo y retorna la URL pública.
     */
    String save(MultipartFile file, String fileName);
}