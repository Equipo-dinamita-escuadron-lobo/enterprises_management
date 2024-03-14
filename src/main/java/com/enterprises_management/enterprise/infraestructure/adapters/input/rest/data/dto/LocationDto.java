package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {

    private String address;
    private Long city;
    private Long department;
    private Long country; 
}
