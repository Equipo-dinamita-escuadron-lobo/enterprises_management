package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.EnterpriseType;
import com.enterprises_management.enterprise.domain.models.Location;
import com.enterprises_management.enterprise.domain.models.PersonType;
import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.domain.models.TaxPayerType;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseTypeEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.LocationEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.PersonTypeEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxLiabilityEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxPayerTypeEntity;

/**
 * Interfaz de mapeo para convertir modelos del dominio a entidades JPA durante actualizaciones.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper
public interface IEnterpriseUpdateMapper {
    
    /**
     * Convierte una lista de responsabilidades fiscales del dominio a entidades JPA.
     *
     * @param taxLiabilities lista de responsabilidades fiscales del dominio
     * @return lista de entidades TaxLiabilityEntity
     */
    List<TaxLiabilityEntity> toTaxLiabilityEntity(List<TaxLiability> taxLiabilities);

    /**
     * Convierte un tipo de contribuyente del dominio a entidad JPA.
     *
     * @param taxPayerType tipo de contribuyente del dominio
     * @return entidad TaxPayerTypeEntity
     */
    TaxPayerTypeEntity toTaxPayerTypeEntity(TaxPayerType taxPayerType);

    /**
     * Convierte un tipo de empresa del dominio a entidad JPA.
     *
     * @param enterpriseType tipo de empresa del dominio
     * @return entidad EnterpriseTypeEntity
     */
    EnterpriseTypeEntity toEnterpriseTypeEntity(EnterpriseType enterpriseType);

    /**
     * Convierte un tipo de persona del dominio a entidad JPA.
     *
     * @param personType tipo de persona del dominio
     * @return entidad PersonTypeEntity
     */
    PersonTypeEntity toPersonTypeEntity(PersonType personType);

    /**
     * Convierte una ubicación del dominio a entidad JPA.
     *
     * @param location ubicación del dominio
     * @return entidad LocationEntity
     */
    LocationEntity toLocationEntity(Location location);
}
