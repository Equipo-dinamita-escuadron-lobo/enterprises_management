package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.ILocationOutputPort;
import com.enterprises_management.enterprise.domain.models.Location;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.LocationEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ILocationMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ILocationRepository;

import lombok.Data;

/**
 * Adaptador para la gestión de direcciones usando JPA.
 * Implementa la interfaz ILocationOutputPort.
 */
@Component
@Data
public class LocationJpaAdapter implements ILocationOutputPort {

    private final ILocationRepository locationRepository;
    private final ILocationMapper locationMapper;

    /**
     * Crea una nueva ubicación.
     *
     * @param location el modelo de dominio de la ubicación a crear
     * @return el modelo de dominio de la ubicación creada, o null si ya existe una ubicación con el mismo ID
     */
    @Override
    public Location create(Location location) {
        LocationEntity locationEntity = locationMapper.toEntity(location);
        if (locationEntity.getId() != null) {return null;}
        
        return locationMapper.toDomain(locationRepository.save(locationEntity));     
    }

    /**
     * Elimina una ubicación por su ID.
     *
     * @param id el ID de la ubicación a eliminar
     * @return true si la ubicación fue eliminada, false si no se encontró
     */
    @Override
    public boolean delete(Long id) {
        if (locationRepository.existsById(id)) {
            locationRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
}
