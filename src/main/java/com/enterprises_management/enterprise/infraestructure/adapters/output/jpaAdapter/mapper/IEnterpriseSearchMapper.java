package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;
import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.domain.models.EnterpriseType;
import com.enterprises_management.enterprise.domain.models.Location;
import com.enterprises_management.enterprise.domain.models.PersonType;
import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.domain.models.TaxPayerType;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.projection.IEnterpriseInfoProjection;

/**
 * Interfaz de mapeo para convertir entre entidades de empresa y DTOs de información.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper
public interface IEnterpriseSearchMapper {

    /**
     * Convierte una lista de proyecciones de información de empresa a una lista de DTOs.
     *
     * @param enterpriseInfo lista de proyecciones con información básica de empresas
     * @return lista de DTOs con información resumida de empresas
     */
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

    /**
     * Convierte una entidad de empresa a su representación en el dominio.
     * Realiza el mapeo completo incluyendo todas las relaciones y objetos anidados.
     *
     * @param enterpriseEntity la entidad a convertir
     * @return el objeto de dominio Enterprise correspondiente
     */
    default Enterprise toEnterprise(EnterpriseEntity enterpriseEntity){
        if (enterpriseEntity == null) {
            return null;
        }
        return Enterprise.builder()
            .id(enterpriseEntity.getId())
            .name(enterpriseEntity.getName())
            .nit(enterpriseEntity.getNit())
            .DV(enterpriseEntity.getDV())
            .phone(enterpriseEntity.getPhone())
            .branch(enterpriseEntity.getBranch())
            .email(enterpriseEntity.getEmail())
            .logo(enterpriseEntity.getLogo())
            .state(enterpriseEntity.getState())
            .mainActivity(enterpriseEntity.getMainActivity())
            .secondaryActivity(enterpriseEntity.getSecondaryActivity())
            .taxLiabilities(
                enterpriseEntity.getTaxLiabilities().stream()
                .map(taxLiability -> {
                    return TaxLiability.builder()
                    .id(taxLiability.getId())
                    .name(taxLiability.getName())
                    .build();
                })
                .toList()
            )
            .taxPayerType(
                TaxPayerType.builder()
                .id(enterpriseEntity.getTaxPayerType().getId())
                .name(enterpriseEntity.getTaxPayerType().getName())
                .build()
            )
            .enterpriseType(
                EnterpriseType.builder()
                .id(enterpriseEntity.getEnterpriseType().getId())
                .name(enterpriseEntity.getEnterpriseType().getName())
                .build()
            )
            .personType(
                PersonType.builder()
                .id(enterpriseEntity.getPersonType().getId())
                .name(enterpriseEntity.getPersonType().getName())
                .surname(enterpriseEntity.getPersonType().getSurname())
                .bussinessName(enterpriseEntity.getPersonType().getBussinessName())
                .type(enterpriseEntity.getPersonType().getType())
                .build()
            )
            .location(
                Location.builder()
                .id(enterpriseEntity.getLocation().getId())
                .address(enterpriseEntity.getLocation().getAddress())
                .city(City.builder()
                    .id(enterpriseEntity.getLocation().getCity().getId())
                    .name(enterpriseEntity.getLocation().getCity().getName())
                    .build())
                .country(Country.builder()
                    .id(enterpriseEntity.getLocation().getCountry().getId())
                    .name(enterpriseEntity.getLocation().getCountry().getName())
                    .build())
                .department(Department.builder()
                    .id(enterpriseEntity.getLocation().getDepartment().getId())
                    .name(enterpriseEntity.getLocation().getDepartment().getName())
                    .build())
                .build()
            )
            .build();
    }
}
