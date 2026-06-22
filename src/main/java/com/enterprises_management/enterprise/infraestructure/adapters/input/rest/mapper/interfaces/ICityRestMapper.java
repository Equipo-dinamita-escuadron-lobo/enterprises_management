package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.CityResponseDto;
import com.enterprises_management.enterprise.domain.models.City;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper para convertir entidades City a DTOs de respuesta.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface ICityRestMapper {

    /**
     * Convierte una lista de entidades City a una lista de CityResponseDto.
     *
     * @param cities Lista de entidades City
     * @return Lista de CityResponseDto
     */
    List<CityResponseDto> toResponseList(List<City> cities);
}