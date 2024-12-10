package com.enterprises_management.enterprise.application.ports.services;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.ILocationMangerPort;
import com.enterprises_management.enterprise.application.ports.output.ILocationOutputPort;
import com.enterprises_management.enterprise.domain.models.Location;

import lombok.AllArgsConstructor;

/**
 * Servicio que implementa las operaciones de gestión de ubicaciones.
 * Gestiona la lógica de negocio para la creación y eliminación de ubicaciones
 * en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class LocationService implements ILocationMangerPort {

    /**
     * Puerto de salida para operaciones de gestión de ubicaciones.
     */
    private final ILocationOutputPort locationOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public Location createLocation(Location location) {
        return locationOutputPort.create(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteLocation(Long id) {
        return locationOutputPort.delete(id);
    }
}
