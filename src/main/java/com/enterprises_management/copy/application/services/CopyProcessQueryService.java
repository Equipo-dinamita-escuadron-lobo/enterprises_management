package com.enterprises_management.copy.application.services;

import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.output.ICopyPhaseRepositoryPort;
import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.application.output.IProcessEventRepositoryPort;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;

import java.util.List;

/**
 * Servicio de aplicación para consultar el estado de un proceso de copia (REQ-API-03).
 */
public class CopyProcessQueryService implements ICopyProcessQueryPort {

    private final ICopyProcessRepositoryPort procesoRepo;
    private final ICopyPhaseRepositoryPort faseRepo;
    private final IProcessEventRepositoryPort eventoRepo;

    public CopyProcessQueryService(
            ICopyProcessRepositoryPort procesoRepo,
            ICopyPhaseRepositoryPort faseRepo,
            IProcessEventRepositoryPort eventoRepo
    ) {
        this.procesoRepo = procesoRepo;
        this.faseRepo = faseRepo;
        this.eventoRepo = eventoRepo;
    }

    @Override
    public CopyProcess consultarProceso(String idProceso) {
        return procesoRepo.buscarPorId(idProceso)
                .orElseThrow(() -> new CopyProcessNotFoundException(idProceso));
    }

    @Override
    public List<CopyPhase> consultarFases(String idProceso) {
        verificarExistencia(idProceso);
        return faseRepo.buscarPorProceso(idProceso);
    }

    @Override
    public List<CopyProcessEvent> consultarEventos(String idProceso) {
        verificarExistencia(idProceso);
        return eventoRepo.buscarPorProceso(idProceso);
    }

    @Override
    public List<CopyProcess> listarProcesos() {
        return procesoRepo.listarTodos();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void verificarExistencia(String idProceso) {
        procesoRepo.buscarPorId(idProceso)
                .orElseThrow(() -> new CopyProcessNotFoundException(idProceso));
    }
}
