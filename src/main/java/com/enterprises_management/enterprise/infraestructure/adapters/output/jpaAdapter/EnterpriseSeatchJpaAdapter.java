package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.IEnterpriseSearchOutputPort;
import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseSearchMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.projection.IEnterpriseInfoProjection;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseRepository;

import lombok.Data;

/**
 * Adaptador para la búsqueda de entidades Enterprise usando JPA.
 * Implementa la interfaz IEnterpriseSearchOutputPort.
 */
@Component
@Data
public class EnterpriseSeatchJpaAdapter implements IEnterpriseSearchOutputPort{
    
    private final IEnterpriseRepository enterpriseRepository;
    private final IEnterpriseSearchMapper enterpriseMapper;
    
    /**
     * Obtiene todas las empresas.
     *
     * @return una lista de DTOs con la información de todas las empresas
     */
    @Override
    public List<EnterpriseInfoDto> getAllEnterprises() {
        List<IEnterpriseInfoProjection> enterpriseInfo = enterpriseRepository.findEnterpriseInfo();
        return enterpriseMapper.toEnterpriseInfoDtoList(enterpriseInfo);
    }

    /**
     * Obtiene todas las empresas inactivas.
     *
     * @return una lista de DTOs con la información de todas las empresas inactivas
     */
    @Override
    public List<EnterpriseInfoDto> getAllEnterprisesInactive() {
        List<IEnterpriseInfoProjection> enterpriseInfo = enterpriseRepository.findEnterpriseInfoInactive();
        return enterpriseMapper.toEnterpriseInfoDtoList(enterpriseInfo);
    }

    /**
     * Obtiene una empresa por su ID.
     *
     * @param id el UUID de la empresa a buscar
     * @return el modelo de dominio de la empresa, o null si no se encuentra
     */
    @Override
    public Enterprise getEnterpriseById(UUID id) {
        EnterpriseEntity enterpriseEntity = enterpriseRepository.findById(id).orElse(null);
        return enterpriseMapper.toEnterprise(enterpriseEntity);
    }
    

}
