package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad CountryEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface IAddressRepository extends JpaRepository<CountryEntity, Long> {

}
