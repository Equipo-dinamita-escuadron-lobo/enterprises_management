package com.enterprises_management.enterprise.application.ports.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.IEnterpriseExportManagerPort;
import com.enterprises_management.enterprise.application.ports.input.IEnterpriseSearchManagerPort;
import com.enterprises_management.enterprise.application.ports.output.IEnterpriseSearchOutputPort;
import com.enterprises_management.enterprise.application.ports.output.IThirdsApiOutputPort;
import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.domain.models.EnterpriseExport;
import com.enterprises_management.enterprise.domain.models.Third;

import lombok.AllArgsConstructor;

/**
 * Servicio que implementa las operaciones de búsqueda de la información de una
 * emopresa.
 * Gestiona la lógica de negocio para la consulta y recuperación de información
 * de una empresa.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */

@Service
@AllArgsConstructor
public class EnterpriseExportService implements IEnterpriseExportManagerPort {

    /**
     * Puerto de salida para operaciones de búsqueda de empresas.
     */
    private final IEnterpriseSearchOutputPort enterpriseSearchOutputPort;

    /**
     * Puerto de salida para consulta de terceros.
     */
    private final IThirdsApiOutputPort thirdsApiOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public EnterpriseExport exportEnterpriseById(UUID id) {
        Enterprise enterprise = enterpriseSearchOutputPort.getEnterpriseById(id);
        if (enterprise == null) {
            return null;
        }
        // Llamada a la API de terceros
        String thirds = thirdsApiOutputPort.getThirdsByEnterprise(id, 0, 10, "names", "asc");

        // Aquí puedes adaptar EnterpriseExport para incluir la lista de terceros si lo deseas
        return new EnterpriseExport(id, enterprise.getIdUser(), enterprise , thirds);
    }

}
