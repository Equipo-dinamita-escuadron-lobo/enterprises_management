package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.IEnterpriseSearchOutputPort;
import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseSearchMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.projection.IEnterpriseInfoProjection;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseRepository;

import lombok.Data;

@Component
@Data
public class EnterpriseSeatchJpaAdapter implements IEnterpriseSearchOutputPort{
    
    private final IEnterpriseRepository enterpriseRepository;
    private final IEnterpriseSearchMapper enterpriseMapper;
    
    @Override
    public List<EnterpriseInfoDto> getAllEnterprises() {
        List<IEnterpriseInfoProjection> enterpriseInfo = enterpriseRepository.findEnterpriseInfo();
        return enterpriseMapper.toEnterpriseInfoDtoList(enterpriseInfo);
    }

    @Override
    public List<EnterpriseInfoDto> getAllEnterprisesInactive() {
        List<IEnterpriseInfoProjection> enterpriseInfo = enterpriseRepository.findEnterpriseInfoInactive();
        return enterpriseMapper.toEnterpriseInfoDtoList(enterpriseInfo);
    }
    

}
