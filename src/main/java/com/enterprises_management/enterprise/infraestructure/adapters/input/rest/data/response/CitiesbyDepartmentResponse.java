package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.DTO.CityDTO;
import lombok.*;

import java.util.List;

@Data
public class CitiesbyDepartmentResponse {
    private Long id;
    private String name;
    List<CityDTO> cities;
}
