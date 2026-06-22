package com.enterprises_management.enterprise.application.ports.input;

import org.springframework.web.multipart.MultipartFile;

public interface IUploadLogoInputPort {
    /**
     * Sube un logo para una empresa y retorna la clave única.
     */
    String uploadLogo(String enterpriseId, MultipartFile file);

    /**
     * Obtiene el logo de una empresa como LogoResource.
     */
    LogoResource getLogo(String enterpriseId);

    /**
     * Elimina el logo de una empresa.
     */
    void deleteLogo(String enterpriseId);
}