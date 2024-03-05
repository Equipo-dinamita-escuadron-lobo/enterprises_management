package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;

public interface IEnterpriseRepository extends JpaRepository<EnterpriseEntity, Long>{
    
}
