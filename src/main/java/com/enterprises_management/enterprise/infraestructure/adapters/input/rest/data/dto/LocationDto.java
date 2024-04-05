package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {

    private String address;
    private Long city;
    private Long department;
    private Long country; 
}
