package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxLiabilityEntity;

public interface ITaxLiabilityRepository extends JpaRepository<TaxLiabilityEntity, Long>{

}