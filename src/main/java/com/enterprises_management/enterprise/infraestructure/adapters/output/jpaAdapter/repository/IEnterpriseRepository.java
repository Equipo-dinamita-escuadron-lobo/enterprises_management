package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.projection.IEnterpriseInfoProjection;

public interface IEnterpriseRepository extends JpaRepository<EnterpriseEntity, Long>{
     @Query("SELECT e.id AS id, e.name AS name, e.nit AS nit, e.logo AS logo FROM EnterpriseEntity e")
     List<IEnterpriseInfoProjection> findEnterpriseInfo();   

     boolean existsByNit(String nit);
}
