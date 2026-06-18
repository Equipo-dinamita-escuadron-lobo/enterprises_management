package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa;

import com.enterprises_management.copy.application.output.ICopyProcessDeleteRepositoryPort;
import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.repository.CopyProcessJpaRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CopyProcessJpaDeleteAdapter implements ICopyProcessDeleteRepositoryPort {

    private final EntityManager em;
    private final CopyProcessJpaRepository processRepo;

    @Override
    @Transactional
    public void deleteById(String id) {
        em.createQuery("DELETE FROM CopyProcessEventEntity e WHERE e.idProceso = :id")
                .setParameter("id", id).executeUpdate();
        em.createQuery("DELETE FROM CopyEquivalenceIdEntity e WHERE e.idProceso = :id")
                .setParameter("id", id).executeUpdate();
        em.createQuery("DELETE FROM CopyModuleExecutionEntity e WHERE e.idProceso = :id")
                .setParameter("id", id).executeUpdate();
        em.createQuery("DELETE FROM CopyPhaseEntity e WHERE e.idProceso = :id")
                .setParameter("id", id).executeUpdate();
        processRepo.deleteById(id);
    }
}
