package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CountryEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper para convertir entre entidades de país y el modelo de dominio.
 */
@Mapper(componentModel = "spring")
public interface ICountriesMapper {

    /**
     * Convierte una lista de CountryEntity a una lista de Country.
     *
     * @param countryEntityList Lista de entidades CountryEntity
     * @return Lista de modelos Country
     */
    List<Country> toModelList(List<CountryEntity> countryEntityList);
}
