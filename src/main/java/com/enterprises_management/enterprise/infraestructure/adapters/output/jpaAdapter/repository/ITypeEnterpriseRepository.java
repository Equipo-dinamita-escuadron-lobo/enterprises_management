package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseTypeEntity;

public interface ITypeEnterpriseRepository extends JpaRepository<EnterpriseTypeEntity, Long>{

     List<EnterpriseTypeEntity> findAll();

}
