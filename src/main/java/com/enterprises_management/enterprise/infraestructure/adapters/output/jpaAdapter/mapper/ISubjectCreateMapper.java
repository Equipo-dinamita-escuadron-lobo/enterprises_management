package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.enterprises_management.enterprise.domain.models.Subject;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.SubjectEntity;

/**
 * Interfaz de mapeo para convertir entre entidades de materia y su representación en el dominio.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper(componentModel = "spring")
public interface ISubjectCreateMapper {

    /**
     * Convierte un objeto de dominio Subject a su entidad correspondiente.
     * Ignora el campo tenantId durante el mapeo.
     *
     * @param subject el objeto de dominio a convertir
     * @return la entidad SubjectEntity correspondiente
     */
    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "code", source = "code"),
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "tenantId", ignore = true)
    })
    SubjectEntity toEntity(Subject subject);

    /**
     * Convierte una entidad SubjectEntity a su representación en el dominio.
     *
     * @param subjectEntity la entidad a convertir
     * @return el objeto de dominio Subject correspondiente
     */
    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "code", source = "code"),
        @Mapping(target = "name", source = "name")
    })
    Subject toModel(SubjectEntity subjectEntity);
}