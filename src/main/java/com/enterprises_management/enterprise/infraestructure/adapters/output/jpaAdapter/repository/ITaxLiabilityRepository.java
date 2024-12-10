package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;


import org.springframework.data.jpa.repository.JpaRepository;


import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxLiabilityEntity;

/**
 * Repositorio JPA para la entidad TaxLiabilityEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface ITaxLiabilityRepository extends JpaRepository<TaxLiabilityEntity, Long> {
    // Métodos adicionales personalizados pueden ser definidos aquí
}
