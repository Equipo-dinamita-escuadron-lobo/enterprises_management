package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.enterprises_management.config.JwtUtils;
import com.enterprises_management.enterprise.application.ports.output.IEnterpriseSearchOutputPort;
import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseSearchMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.projection.IEnterpriseInfoProjection;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseRepository;

import lombok.Data;

@Component
@Data
public class EnterpriseSeatchJpaAdapter implements IEnterpriseSearchOutputPort{
    
    private final IEnterpriseRepository enterpriseRepository;
    private final IEnterpriseSearchMapper enterpriseMapper;
    private final JwtUtils jwtUtils;
    
    @Override
    public List<EnterpriseInfoDto> getAllEnterprises() {
        String idUser = jwtUtils.getId();
        List<IEnterpriseInfoProjection> enterpriseInfo = enterpriseRepository.findEnterpriseInfo(idUser);
        return enterpriseMapper.toEnterpriseInfoDtoList(enterpriseInfo);
    }

    @Override
    public List<EnterpriseInfoDto> getAllEnterprisesInactive() {
        List<IEnterpriseInfoProjection> enterpriseInfo = enterpriseRepository.findEnterpriseInfoInactive();
        return enterpriseMapper.toEnterpriseInfoDtoList(enterpriseInfo);
    }

    @Override
    public Enterprise getEnterpriseById(UUID id) {
        EnterpriseEntity enterpriseEntity = enterpriseRepository.findById(id).orElse(null);
        return enterpriseMapper.toEnterprise(enterpriseEntity);
    }
    

}
