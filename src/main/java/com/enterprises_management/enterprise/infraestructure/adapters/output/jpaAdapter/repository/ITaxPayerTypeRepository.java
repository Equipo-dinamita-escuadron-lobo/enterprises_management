package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;


import org.springframework.data.jpa.repository.JpaRepository;


import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxPayerTypeEntity;

/**
 * Repositorio JPA para la entidad TaxPayerTypeEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface ITaxPayerTypeRepository extends JpaRepository<TaxPayerTypeEntity, Long> {
    // Métodos adicionales personalizados pueden ser definidos aquí
}
