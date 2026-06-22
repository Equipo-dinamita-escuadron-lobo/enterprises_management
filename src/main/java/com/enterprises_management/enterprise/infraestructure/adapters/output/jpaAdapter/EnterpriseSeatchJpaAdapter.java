package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.IEnterpriseSearchOutputPort;
import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseSearchMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.multitenancy.util.TenantContext;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.projection.IEnterpriseInfoProjection;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseRepository;

import lombok.Data;

/**
 * Adaptador para la búsqueda de entidades Enterprise usando JPA.
 * Implementa la interfaz IEnterpriseSearchOutputPort.
 */
@Component
@Data
public class EnterpriseSeatchJpaAdapter implements IEnterpriseSearchOutputPort {

    private final IEnterpriseRepository enterpriseRepository;
    private final IEnterpriseSearchMapper enterpriseMapper;

    /**
     * Obtiene todas las empresas.
     *
     * @return una lista de DTOs con la información de todas las empresas
     */
    @Override
    public List<EnterpriseInfoDto> getAllEnterprises() {
        List<IEnterpriseInfoProjection> enterpriseInfo = isAdmin()
            ? enterpriseRepository.findEnterpriseInfoAll()
            : enterpriseRepository.findEnterpriseInfoForUser(TenantContext.getTenantId());
        return enterpriseMapper.toEnterpriseInfoDtoList(enterpriseInfo);
    }

    /**
     * Obtiene todas las empresas inactivas.
     *
     * @return una lista de DTOs con la información de todas las empresas inactivas
     */
    @Override
    public List<EnterpriseInfoDto> getAllEnterprisesInactive() {
        List<IEnterpriseInfoProjection> enterpriseInfo = isAdmin()
            ? enterpriseRepository.findEnterpriseInfoInactiveAll()
            : enterpriseRepository.findEnterpriseInfoInactiveForUser(TenantContext.getTenantId());
        return enterpriseMapper.toEnterpriseInfoDtoList(enterpriseInfo);
    }

    private boolean isAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_admin_client"));
    }

    /**
     * Obtiene una empresa por su ID.
     *
     * @param id el UUID de la empresa a buscar
     * @return el modelo de dominio de la empresa, o null si no se encuentra
     */
    @Override
    public Enterprise getEnterpriseById(UUID id) {
        EnterpriseEntity enterpriseEntity = isAdmin()
            ? enterpriseRepository.findByIdNative(id.toString()).orElse(null)
            : enterpriseRepository.findById(id).orElse(null);
        return enterpriseMapper.toEnterprise(enterpriseEntity);
    }

    /**
     * Obtiene una empresa por el código de la materia asociada.
     *
     * @param subjectCode el código de la materia asociada a la empresa
     * @return el modelo de dominio de la empresa, o null si no se encuentra
     */
    @Override
    public Enterprise getEnterpriseBySubjectCode(String subjectCode) {
        List<EnterpriseEntity> results = enterpriseRepository.findBySubjectsCode(subjectCode);
        if (results.isEmpty()) return null;
        return enterpriseMapper.toEnterprise(results.get(0));
    }

    @Override
    public List<EnterpriseInfoDto> searchEnterprises(String q) {
        return enterpriseRepository.searchByNameNitOrSubject(q)
                .stream()
                .map(e -> EnterpriseInfoDto.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .nit(e.getNit())
                        .logo(e.getLogo())
                        .build())
                .toList();
    }

}
