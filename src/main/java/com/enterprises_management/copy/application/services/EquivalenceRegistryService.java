package com.enterprises_management.copy.application.services;

import com.enterprises_management.copy.application.input.IEquivalenceQueryPort;
import com.enterprises_management.copy.application.input.IEquivalenceRegistryPort;
import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.exceptions.EquivalenceConflictException;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de aplicación para registrar y consultar equivalencias de IDs (REQ-EQ-01, REQ-EQ-02).
 * Implementa la lógica de idempotencia y detección de conflictos (ADR-9).
 */
public class EquivalenceRegistryService implements IEquivalenceRegistryPort, IEquivalenceQueryPort {

    private final ICopyProcessRepositoryPort procesoRepo;
    private final IEquivalenceRepositoryPort equivalenciaRepo;

    public EquivalenceRegistryService(
            ICopyProcessRepositoryPort procesoRepo,
            IEquivalenceRepositoryPort equivalenciaRepo
    ) {
        this.procesoRepo = procesoRepo;
        this.equivalenciaRepo = equivalenciaRepo;
    }

    @Override
    public int registrar(List<CopyEquivalenceId> equivalencias) {
        if (equivalencias == null || equivalencias.isEmpty()) {
            return 0;
        }

        // Verificar existencia del proceso (todas las equivalencias deben pertenecer al mismo proceso)
        String idProceso = equivalencias.get(0).getIdProceso();
        procesoRepo.buscarPorId(idProceso)
                .orElseThrow(() -> new CopyProcessNotFoundException(idProceso));

        int insertadas = 0;
        for (CopyEquivalenceId eq : equivalencias) {
            Optional<CopyEquivalenceId> existente = equivalenciaRepo.buscarPorClave(
                    eq.getIdProceso(), eq.getModulo(), eq.getTabla(), eq.getIdViejo());

            if (existente.isPresent()) {
                // ADR-9: mismo idNuevo → idempotente (omitir); idNuevo diferente → conflicto
                if (!existente.get().getIdNuevo().equals(eq.getIdNuevo())) {
                    throw new EquivalenceConflictException(
                            eq.getIdProceso(), eq.getModulo(), eq.getTabla(), eq.getIdViejo());
                }
                // mismo idNuevo → no hacer nada (idempotente)
            } else {
                equivalenciaRepo.guardar(eq);
                insertadas++;
            }
        }
        return insertadas;
    }

    @Override
    public List<CopyEquivalenceId> consultar(String idProceso, String modulo, String tabla, String idViejo) {
        procesoRepo.buscarPorId(idProceso)
                .orElseThrow(() -> new CopyProcessNotFoundException(idProceso));
        return equivalenciaRepo.buscarConFiltros(idProceso, modulo, tabla, idViejo);
    }
}
