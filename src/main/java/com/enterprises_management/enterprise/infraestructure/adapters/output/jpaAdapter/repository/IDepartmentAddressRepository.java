package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para la entidad DepartmentEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface IDepartmentAddressRepository extends JpaRepository<DepartmentEntity,Long> {
    
    /**
     * Encuentra un departamento por su identificador.
     *
     * @param id el identificador del departamento
     * @return la entidad DepartmentEntity correspondiente
     */
    DepartmentEntity findAllById(Long id);
}
