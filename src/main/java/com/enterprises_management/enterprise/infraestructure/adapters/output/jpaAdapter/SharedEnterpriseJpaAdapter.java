package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import com.enterprises_management.enterprise.application.ports.output.ISharedEnterprisePort;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.SharedEnterpriseEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ISharedEnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SharedEnterpriseJpaAdapter implements ISharedEnterprisePort {

    private final ISharedEnterpriseRepository repository;

    @Override
    public void saveShare(UUID enterpriseId, String sharedWithUserId) {
        if (!repository.existsByEnterpriseIdAndSharedWithUserId(enterpriseId, sharedWithUserId)) {
            SharedEnterpriseEntity entity = new SharedEnterpriseEntity();
            entity.setEnterpriseId(enterpriseId);
            entity.setSharedWithUserId(sharedWithUserId);
            repository.save(entity);
        }
    }
}
