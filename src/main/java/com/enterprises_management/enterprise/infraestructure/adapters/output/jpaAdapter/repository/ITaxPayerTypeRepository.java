package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;


import org.springframework.data.jpa.repository.JpaRepository;


import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxPayerTypeEntity;

public interface ITaxPayerTypeRepository extends JpaRepository<TaxPayerTypeEntity, Long> {
}
