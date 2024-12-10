package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.LocationEntity;

/**
 * Repositorio JPA para la entidad LocationEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface ILocationRepository extends JpaRepository<LocationEntity, Long> {
    // Métodos adicionales pueden ser definidos aquí
}
