package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.IEnterpriseUpdateOutputPort;
import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseUpdateMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseRepository;

/**
 * Adaptador para la actualización de entidades Enterprise usando JPA.
 * Implementa la interfaz IEnterpriseUpdateOutputPort.
 */
@Component
public class EnterpriseUpdate implements IEnterpriseUpdateOutputPort {

    @Autowired
    private IEnterpriseRepository enterpriseRepository;

    @Autowired
    private IEnterpriseUpdateMapper updateMapper;

    /**
     * Actualiza una empresa por su ID.
     *
     * @param id el UUID de la empresa a actualizar
     * @param enterprise el modelo de dominio con la nueva información de la empresa
     */
    @Override
    public void updateEnterprise(UUID id, Enterprise enterprise) {

        // Buscar la empresa por su ID
        EnterpriseEntity enterpriseEntity = enterpriseRepository.findById(id).get();

        if (enterpriseEntity == null) {
            throw new RuntimeException("Enterprise not found");
        }

        // Actualizar los campos de la entidad empresa con la nueva información
        enterpriseEntity.setName(enterprise.getName());
        enterpriseEntity.setNit(enterprise.getNit());
        enterpriseEntity.setDV(enterprise.getDV());
        enterpriseEntity.setPhone(enterprise.getPhone());
        enterpriseEntity.setBranch(enterprise.getBranch());
        enterpriseEntity.setEmail(enterprise.getEmail());
        enterpriseEntity.setLogo(enterprise.getLogo());
        enterpriseEntity.setMainActivity(enterprise.getMainActivity());
        enterpriseEntity.setSecondaryActivity(enterprise.getSecondaryActivity());

        //taxLiabilities (reponsabilidades tributarias)
        enterpriseEntity.setTaxLiabilities(updateMapper.toTaxLiabilityEntity(enterprise.getTaxLiabilities()));
        
        //taxPayerType (tipo de contribuyente)
        enterpriseEntity.setTaxPayerType(updateMapper.toTaxPayerTypeEntity(enterprise.getTaxPayerType()));

        //enterpriseType (tipo de empresa)
        enterpriseEntity.setEnterpriseType(updateMapper.toEnterpriseTypeEntity(enterprise.getEnterpriseType()));

        //personType (tipo de persona)
        enterpriseEntity.setPersonType(updateMapper.toPersonTypeEntity(enterprise.getPersonType()));

        //location
        enterpriseEntity.setLocation(updateMapper.toLocationEntity(enterprise.getLocation()));

        enterpriseRepository.save(enterpriseEntity);
    }
    
    /**
     * Actualiza el estado de una empresa por su ID.
     *
     * @param id el UUID de la empresa a actualizar
     * @param state el nuevo estado de la empresa
     */
    @Override
    public void updateEnterpriseStatus(UUID id, StateEnum state) {
        EnterpriseEntity enterpriseEntity = enterpriseRepository.findById(id).get();

        if (enterpriseEntity == null) {
            throw new RuntimeException("Enterprise not found");
        }

        enterpriseEntity.setState(state);

        enterpriseRepository.save(enterpriseEntity);
    }
}
