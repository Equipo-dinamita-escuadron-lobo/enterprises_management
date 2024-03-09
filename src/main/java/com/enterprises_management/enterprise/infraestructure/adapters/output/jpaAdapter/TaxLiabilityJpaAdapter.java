package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.ITaxLiabilityOutputPort;
import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxLiabilityEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ITaxLiabilityMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ITaxLiabilityRepository;

import lombok.Data;

@Component
@Data
public class TaxLiabilityJpaAdapter implements ITaxLiabilityOutputPort{

    private final ITaxLiabilityRepository taxLiabilityRepository;
    private final ITaxLiabilityMapper taxLiabilityMapper;

    @Override
    public List<TaxLiability> getAll() {
        List<TaxLiabilityEntity> taxLiabilityEntities = taxLiabilityRepository.findAll();
        List<TaxLiability> taxLiabilities = taxLiabilityMapper.toModel(taxLiabilityEntities);
        return taxLiabilities ;     
    }  
}
