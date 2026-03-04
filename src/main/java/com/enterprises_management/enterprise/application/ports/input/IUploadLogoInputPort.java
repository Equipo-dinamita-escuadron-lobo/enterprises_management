package com.enterprises_management.enterprise.application.ports.input;

import org.springframework.web.multipart.MultipartFile;

public interface IUploadLogoInputPort {
    /**
     * Sube un logo y retorna la URL pública para consumirlo luego.
     */
    String upload(MultipartFile file);
}