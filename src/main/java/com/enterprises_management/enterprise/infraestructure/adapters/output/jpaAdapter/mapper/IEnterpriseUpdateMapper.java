package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.EnterpriseType;
import com.enterprises_management.enterprise.domain.models.Location;
import com.enterprises_management.enterprise.domain.models.PersonType;
import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.domain.models.TaxPayerType;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseTypeEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.LocationEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.PersonTypeEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxLiabilityEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxPayerTypeEntity;

@Mapper
public interface IEnterpriseUpdateMapper {
    
    List<TaxLiabilityEntity> toTaxLiabilityEntity(List<TaxLiability> taxLiabilities);

    TaxPayerTypeEntity toTaxPayerTypeEntity(TaxPayerType taxPayerType);

    EnterpriseTypeEntity toEnterpriseTypeEntity(EnterpriseType enterpriseType);

    PersonTypeEntity toPersonTypeEntity(PersonType personType);

    LocationEntity toLocationEntity(Location location);
}
