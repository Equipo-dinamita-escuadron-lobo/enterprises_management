package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;

import lombok.*;

import java.util.List;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.CityResponseDto;

/**
 * Clas que representa la respuesta de las ciudades por departamento.
 * Se utiliza para transferir la información de un departamento junto con
 * su lista de ciudades asociadas desde y hacia las interfaces REST.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
public class CitiesbyDepartmentResponse {
    
    /**
     * Identificador único del departamento.
     */
    private Long id;

    /**
     * Nombre del departamento.
     */
    private String name;

    /**
     * Lista de ciudades que pertenecen al departamento.
     */
    List<CityResponseDto> cities;
}
