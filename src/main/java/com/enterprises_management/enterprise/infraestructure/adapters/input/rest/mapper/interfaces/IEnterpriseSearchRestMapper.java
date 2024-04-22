package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.CityResponseDto;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.CountryResponseDto;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.DepartmentResponseDto;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.LocationResponseDto;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseByIdResponse;

@Mapper
public interface IEnterpriseSearchRestMapper {

    default EnterpriseByIdResponse toEnterpriseByIdResponse(Enterprise enterprise) {
        return EnterpriseByIdResponse.builder()
                .id(enterprise.getId())
                .name(enterprise.getName())
                .nit(enterprise.getNit())
                .DV(enterprise.getDV())
                .phone(enterprise.getPhone())
                .branch(enterprise.getBranch())
                .email(enterprise.getEmail())
                .logo(enterprise.getLogo())
                .state(enterprise.getState())

                .taxLiabilities(enterprise.getTaxLiabilities())

                .taxPayerType(enterprise.getTaxPayerType())

                .enterpriseType(enterprise.getEnterpriseType())

                .personType(enterprise.getPersonType())

                .location(
                    LocationResponseDto.builder()
                    .id(enterprise.getLocation().getId())
                    .address(enterprise.getLocation().getAddress())
                    .city(CityResponseDto.builder()
                        .id(enterprise.getLocation().getCity().getId())
                        .name(enterprise.getLocation().getCity().getName())
                        .build()
                    )
                    .country(CountryResponseDto.builder()
                        .id(enterprise.getLocation().getCountry().getId())
                        .name(enterprise.getLocation().getCountry().getName())
                        .build()
                    )
                    .department(DepartmentResponseDto.builder()
                        .id(enterprise.getLocation().getDepartment().getId())
                        .name(enterprise.getLocation().getDepartment().getName())
                        .build()
                    )           
                    .build()
                )           
                .build();
    }
}
