package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationResponseDto {
    
        private Long id;
        private String address;
        private CityResponseDto city;
        private DepartmentResponseDto department;
        private CountryResponseDto country;
}
