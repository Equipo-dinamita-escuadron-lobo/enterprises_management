package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;

import java.util.List;


import com.enterprises_management.enterprise.domain.models.Location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseCreateResponse {
    
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

    Long personType;

    Location location;  

}
