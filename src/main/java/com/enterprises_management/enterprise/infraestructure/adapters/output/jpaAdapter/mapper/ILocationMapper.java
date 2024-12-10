package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.Location;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.LocationEntity;

/**
 * Interfaz de mapeo para convertir entre entidades de ubicación y su representación en el dominio.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper
public interface ILocationMapper {

    /**
     * Convierte una entidad LocationEntity a su representación en el dominio.
     *
     * @param locationEntity la entidad a convertir
     * @return el objeto de dominio Location correspondiente
     */
    Location toDomain(LocationEntity locationEntity);
    
    /**
     * Convierte un objeto de dominio Location a su entidad correspondiente.
     *
     * @param location el objeto de dominio a convertir
     * @return la entidad LocationEntity correspondiente
     */
    LocationEntity toEntity(Location location); 
}
