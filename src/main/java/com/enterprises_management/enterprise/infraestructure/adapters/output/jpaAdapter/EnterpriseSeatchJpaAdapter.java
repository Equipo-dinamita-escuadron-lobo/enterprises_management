package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.IEnterpriseSearchOutputPort;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseSearchMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseRepository;

import lombok.Data;

@Component
@Data
public class EnterpriseSeatchJpaAdapter implements IEnterpriseSearchOutputPort{
    
    private final IEnterpriseRepository enterpriseRepository;
    private final IEnterpriseSearchMapper enterpriseMapper;
    
    @Override
    public List<Enterprise> getAllEnterprises() {
        List<EnterpriseEntity> enterpriseEntities = enterpriseRepository.findAll();
        List<Enterprise> enterprises = enterpriseMapper.toListModel(enterpriseEntities);
        return enterprises;
    }
    

}
