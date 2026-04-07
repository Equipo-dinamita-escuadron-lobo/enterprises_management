package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad DepartmentEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface IDepartmentAddressRepository extends JpaRepository<DepartmentEntity,Long> {
    
    /**
     * Encuentra todos los departamentos que pertenecen a un país.
     *
     * @param countryId el identificador del país
     * @return lista de entidades DepartmentEntity
     */
    List<DepartmentEntity> findByCountry_Id(Long countryId);
}
