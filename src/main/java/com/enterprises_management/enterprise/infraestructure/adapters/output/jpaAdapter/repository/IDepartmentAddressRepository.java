package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDepartmentAddressRepository extends JpaRepository<DepartmentEntity,Long>{
    DepartmentEntity findAllById(Long id);
}
