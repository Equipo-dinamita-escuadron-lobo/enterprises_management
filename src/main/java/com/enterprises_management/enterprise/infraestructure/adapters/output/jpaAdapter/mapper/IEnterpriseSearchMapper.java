package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.projection.IEnterpriseInfoProjection;

@Mapper
public interface IEnterpriseSearchMapper {

    default List<EnterpriseInfoDto> toEnterpriseInfoDtoList(List<IEnterpriseInfoProjection> enterpriseInfo) {
        if (enterpriseInfo == null) {
            return null;
        }

        return enterpriseInfo.stream()
                .map(enterprise -> {
                    EnterpriseInfoDto enterpriseInfoDto = EnterpriseInfoDto.builder()
                        .id(enterprise.getId())
                        .name(enterprise.getName())
                        .nit(enterprise.getNit())
                        .logo(enterprise.getLogo())
                        .build();
                    return enterpriseInfoDto;
                })
                .toList();
    }

}
