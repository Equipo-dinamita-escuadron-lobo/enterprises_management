package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PersonTypeDto {
    private String type;
    private String name;
    private String surname;

    private String bussinessName;//Razon social
}
