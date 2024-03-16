package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.IEnterpriseCreateOutputPort;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseCreateMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseRepository;

import lombok.Data;

@Component
@Data
public class EnterpriseCreateJpaAdapter implements IEnterpriseCreateOutputPort {

    private final IEnterpriseRepository enterpriseRepository;
    private final IEnterpriseCreateMapper createMapper;

    @Override
    public Enterprise create(Enterprise enterprise) {
        EnterpriseEntity enterpriseEntity = createMapper.toEntity(enterprise);
        if (enterpriseEntity == null) {
            return null;          
        }
        enterpriseEntity =  enterpriseRepository.save(enterpriseEntity);
        return createMapper.toModel(enterpriseEntity);
    } 
}
