package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.SharedEnterpriseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ISharedEnterpriseRepository extends JpaRepository<SharedEnterpriseEntity, UUID> {
    boolean existsByEnterpriseIdAndSharedWithUserId(UUID enterpriseId, String sharedWithUserId);
}
