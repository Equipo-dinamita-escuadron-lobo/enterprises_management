package com.enterprises_management.enterprise.application.ports.output;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ILogoStorageOutputPort {
    /**
     * Guarda el archivo de logo y retorna la clave única.
     */
    String save(MultipartFile file);

    /**
     * Carga el archivo de logo como Resource.
     */
    Resource load(String logoKey);

    /**
     * Elimina el archivo de logo.
     */
    void delete(String logoKey);
}