package com.enterprises_management.enterprise.application.ports.input;
import java.util.List;
import java.util.UUID;

import com.enterprises_management.enterprise.domain.models.EnterpriseExport;

/**
 * Puerto de entrada para la exportación de información de empresas.
 * Define las operaciones necesarias para exportar la información de una empresa específica.
 * Permite obtener un objeto de exportación con toda la información relevante de la empresa para su uso externo.
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */

public interface IEnterpriseExportManagerPort {
    
    /**
     * Exporta la toda la información de una empresa específica por su ID
     * @param id UUID identificador único de la empresa
     * @return EnterpriseExport Objeto con la información completa de la empresa para exportar
     * @see EnterpriseExport
     */
    EnterpriseExport exportEnterpriseById(UUID id);

}
