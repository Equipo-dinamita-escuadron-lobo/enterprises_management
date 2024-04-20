package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.projection.IEnterpriseInfoProjection;

public interface IEnterpriseRepository extends JpaRepository<EnterpriseEntity, UUID>{
     //filtro por state = 0 (activo)
     @Query("SELECT e.id AS id, e.name AS name, e.nit AS nit, e.logo AS logo, e.state AS state FROM EnterpriseEntity e WHERE e.state = 0")
     List<IEnterpriseInfoProjection> findEnterpriseInfo();   

     boolean existsByNit(String nit);

     //filtro por state = 1 (inactivo)
     @Query("SELECT e.id AS id, e.name AS name, e.nit AS nit, e.logo AS logo, e.state AS state FROM EnterpriseEntity e WHERE e.state = 1")
     List<IEnterpriseInfoProjection> findEnterpriseInfoInactive();
}
