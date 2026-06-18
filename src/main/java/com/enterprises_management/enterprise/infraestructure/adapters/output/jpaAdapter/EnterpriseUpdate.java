package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.IEnterpriseUpdateOutputPort;
import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseUpdateMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.SubjectEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseRepository;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ISubjectRepository;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ITaxLiabilityRepository;

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

    @Autowired
    private ITaxLiabilityRepository taxLiabilityRepository;

    @Autowired
    private ISubjectRepository subjectRepository;

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
        List<Long> taxIds = enterprise.getTaxLiabilities() == null ? List.of() :
            enterprise.getTaxLiabilities().stream().map(t -> t.getId()).collect(Collectors.toList());
        enterpriseRepository.deleteTaxLiabilitiesByEnterpriseId(id);
        for (Long taxId : taxIds) {
            enterpriseRepository.insertTaxLiability(id, taxId);
        }

        //taxPayerType (tipo de contribuyente)
        enterpriseEntity.setTaxPayerType(updateMapper.toTaxPayerTypeEntity(enterprise.getTaxPayerType()));

        //enterpriseType (tipo de empresa)
        enterpriseEntity.setEnterpriseType(updateMapper.toEnterpriseTypeEntity(enterprise.getEnterpriseType()));

        //personType (tipo de persona)
        enterpriseEntity.setPersonType(updateMapper.toPersonTypeEntity(enterprise.getPersonType()));

        //location
        enterpriseEntity.setLocation(updateMapper.toLocationEntity(enterprise.getLocation()));

        //inventoryMethods (método de inventario)
        enterpriseEntity.setInventoryMethods(enterprise.getInventoryMethods());

        //subjects (materias asociadas)
        List<UUID> subjectIds = enterprise.getSubjects() == null ? List.of() :
            enterprise.getSubjects().stream().map(s -> s.getId()).collect(Collectors.toList());
        List<SubjectEntity> subjectEntities = subjectRepository.findAllById(subjectIds);
        enterpriseEntity.setSubjects(subjectEntities);

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

    /**
     * Elimina permanentemente una empresa por su ID.
     */
    @Override
    public void deleteEnterprise(UUID id) {
        if (!enterpriseRepository.existsById(id)) {
            throw new RuntimeException("Enterprise not found");
        }
        enterpriseRepository.deleteById(id);
    }
}
