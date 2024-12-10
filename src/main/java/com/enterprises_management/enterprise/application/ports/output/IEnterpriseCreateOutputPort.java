package com.enterprises_management.enterprise.application.ports.output;

import com.enterprises_management.enterprise.domain.models.Enterprise;

/**
 * Puerto de salida para la creación de empresas.
 * Define las operaciones necesarias para persistir nuevas empresas
 * en el sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface IEnterpriseCreateOutputPort {
    
    /**
     * Crea una nueva empresa en el sistema de almacenamiento.
     *
     * @param enterprise Objeto Enterprise con la información de la empresa a crear
     * @return Enterprise Objeto Enterprise con la información de la empresa creada, incluyendo su identificador
     * @see Enterprise
     */
    public Enterprise create(Enterprise enterprise);
}
