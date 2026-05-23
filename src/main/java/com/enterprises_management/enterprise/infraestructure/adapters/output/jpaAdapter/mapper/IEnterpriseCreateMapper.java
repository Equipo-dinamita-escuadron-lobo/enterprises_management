package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;

@Mapper(componentModel = "spring")
public interface IEnterpriseCreateMapper {

    @Mappings({@Mapping(target = "tenantId", ignore = true)})
    EnterpriseEntity toEntity(Enterprise enterprise);

    Enterprise toModel(EnterpriseEntity enterpriseEntity);

    default Long taxLiabilityToId(TaxLiability tax) {
        return tax == null ? null : tax.getId();
    }

    default TaxLiability idToTaxLiability(Long id) {
        return id == null ? null : TaxLiability.builder().id(id).build();
    }
}
