package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.request;

import java.util.List;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.LocationDto;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.PersonTypeDto;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnterpriseCreateRequest {

    //El id no es necesario ya que es autogenerado
    @JsonIgnore
    private Long id;

    private String name;
    private String nit;
    private String DV;
    private String phone;
    private String branch; 
    private String email;
    private String logo;

    List<Long> taxLiabilities;

    Long taxPayerType;

    Long enterpriseType; 

    PersonTypeDto personType;

    LocationDto location;
}
