package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;

import lombok.*;

import java.util.List;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.CityResponseDto;

@Data
public class CitiesbyDepartmentResponse {
    private Long id;
    private String name;
    List<CityResponseDto> cities;
}
