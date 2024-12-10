package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.PersonTypeEntity;

/**
 * Repositorio JPA para la entidad PersonTypeEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface IPersonTypeRepository extends JpaRepository<PersonTypeEntity, Long> {
    // Métodos adicionales personalizados pueden ser definidos aquí
}
