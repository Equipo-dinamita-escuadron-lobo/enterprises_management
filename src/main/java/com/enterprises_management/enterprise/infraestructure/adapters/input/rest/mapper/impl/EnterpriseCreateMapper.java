package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.impl;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.domain.models.EnterpriseType;
import com.enterprises_management.enterprise.domain.models.Location;
import com.enterprises_management.enterprise.domain.models.PersonType;
import com.enterprises_management.enterprise.domain.models.TaxPayerType;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.request.EnterpriseCreateRequest;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseCreateResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.IEnterpriseCreateRestMapper;

import lombok.AllArgsConstructor;


@Component
@AllArgsConstructor
public class EnterpriseCreateMapper implements IEnterpriseCreateRestMapper{


    @Override
    public Enterprise toDomain(EnterpriseCreateRequest enterpriseCreateResponse) {
        Enterprise enterprise = Enterprise.builder()
        .name(enterpriseCreateResponse.getName())
        .nit(enterpriseCreateResponse.getNit())
        .DV(enterpriseCreateResponse.getDV())
        .phone(enterpriseCreateResponse.getPhone())
        .branch(enterpriseCreateResponse.getBranch())
        .email(enterpriseCreateResponse.getEmail())
        .logo(enterpriseCreateResponse.getLogo())

        .taxPayerType(TaxPayerType.builder().id(enterpriseCreateResponse.getTaxPayerType()).build())

        .enterpriseType(EnterpriseType.builder().id(enterpriseCreateResponse.getEnterpriseType()).build())
        
        .personType(enterpriseCreateResponse.getPersonType() != null ? PersonType.builder().id(enterpriseCreateResponse.getPersonType()).build() : null)

        .location(
            Location.builder()
            .address(enterpriseCreateResponse.getLocation().getAddress())
            .city(City.builder().id(enterpriseCreateResponse.getLocation().getCity()).build())
            .country(Country.builder().id(enterpriseCreateResponse.getLocation().getCountry()).build())
            .department(Department.builder().id(enterpriseCreateResponse.getLocation().getDepartment()).build())
            .build()
        )     

        .build();  //fin de enterprise
        
        return enterprise;    
    }

    @Override
    public EnterpriseCreateResponse toCreateResponse(Enterprise enterprise) {
        EnterpriseCreateResponse enterpriseCreateResponse = EnterpriseCreateResponse.builder()
        .id(enterprise.getId())
        .name(enterprise.getName())
        .nit(enterprise.getNit())
        .DV(enterprise.getDV())
        .phone(enterprise.getPhone())
        .branch(enterprise.getBranch())
        .email(enterprise.getEmail())
        .logo(enterprise.getLogo())

        .taxPayerType(enterprise.getTaxPayerType().getId())

        .enterpriseType(enterprise.getEnterpriseType().getId())

        .location(enterprise.getLocation())

        .personType(enterprise.getPersonType() != null ? enterprise.getPersonType().getId() : null)
        
        .build();
        return enterpriseCreateResponse;
    }
    
}
