package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.Subject;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.SubjectEntity;

/**
 * Interfaz de mapeo para convertir entre entidades de materia y su representación en el dominio.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper(componentModel = "spring")
public interface ISubjectUpdateMapper {

    /**
     * Convierte una entidad SubjectEntity a su representación en el dominio.
     *
     * @param subjectEntity la entidad a convertir
     * @return el objeto de dominio Subject correspondiente
     */
    Subject toModel(SubjectEntity subjectEntity);
}