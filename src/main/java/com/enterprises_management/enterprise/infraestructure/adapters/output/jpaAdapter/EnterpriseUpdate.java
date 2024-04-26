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

@Component
public class EnterpriseUpdate implements IEnterpriseUpdateOutputPort {

    @Autowired
    private IEnterpriseRepository enterpriseRepository;

    @Autowired
    private IEnterpriseUpdateMapper updateMapper;


    @Override
    public void updateEnterprise(UUID id, Enterprise enterprise) {

        //actulizar empresa por el id
        EnterpriseEntity enterpriseEntity = enterpriseRepository.findById(id).get();

        if (enterpriseEntity == null) {
            throw new RuntimeException("Enterprise not found");
        }

        enterpriseEntity.setName(enterprise.getName());
        enterpriseEntity.setNit(enterprise.getNit());
        enterpriseEntity.setDV(enterprise.getDV());
        enterpriseEntity.setPhone(enterprise.getPhone());
        enterpriseEntity.setBranch(enterprise.getBranch());
        enterpriseEntity.setEmail(enterprise.getEmail());
        enterpriseEntity.setLogo(enterprise.getLogo());

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
