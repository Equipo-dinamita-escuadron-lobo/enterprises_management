package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.Subject;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.request.SubjectCreateRequest;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.SubjectCreateResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.SubjectResponse;

/**
 * Interfaz de mapeo para convertir entre modelos de dominio y DTOs REST para materias.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper(componentModel = "spring")
public interface ISubjectRestMapper {

    /**
     * Convierte una solicitud de creación a modelo de dominio.
     *
     * @param request la solicitud de creación
     * @return el modelo de dominio Subject
     */
    Subject toDomain(SubjectCreateRequest request);

    /**
     * Convierte un modelo de dominio a respuesta de creación.
     *
     * @param subject el modelo de dominio
     * @return la respuesta de creación
     */
    SubjectCreateResponse toCreateResponse(Subject subject);

    /**
     * Convierte un modelo de dominio a respuesta de consulta.
     *
     * @param subject el modelo de dominio
     * @return la respuesta de consulta
     */
    SubjectResponse toResponse(Subject subject);
}