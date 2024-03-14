package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.LocationEntity;

public interface ILocationRepository extends JpaRepository<LocationEntity, Long>{
    
}
