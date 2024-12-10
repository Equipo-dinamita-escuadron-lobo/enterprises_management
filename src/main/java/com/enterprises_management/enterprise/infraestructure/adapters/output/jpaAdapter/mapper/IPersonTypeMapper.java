package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.PersonType;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.PersonTypeEntity;

/**
 * Interfaz de mapeo para convertir entre entidades de tipo de persona y su representación en el dominio.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper
public interface IPersonTypeMapper {
    
    /**
     * Convierte un objeto de dominio PersonType a su entidad correspondiente.
     *
     * @param personType el objeto de dominio a convertir
     * @return la entidad PersonTypeEntity correspondiente
     */
    PersonTypeEntity toEntity(PersonType personType);

    /**
     * Convierte una entidad PersonTypeEntity a su representación en el dominio.
     *
     * @param personTypeEntity la entidad a convertir
     * @return el objeto de dominio PersonType correspondiente
     */
    PersonType toDomain(PersonTypeEntity personTypeEntity); 
}
