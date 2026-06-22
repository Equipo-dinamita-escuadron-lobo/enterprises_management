package com.enterprises_management.copy.application.services;

import com.enterprises_management.copy.application.input.ICopyProcessCancelPort;
import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.application.output.IProcessEventRepositoryPort;
import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.exceptions.CopyProcessCancelDeniedException;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import com.enterprises_management.copy.domain.policy.PhaseTransitionPolicy;

import java.time.LocalDateTime;

/**
 * Servicio de aplicación para cancelar un proceso de copia activo.
 * Solo permite cancelar procesos en estado PENDIENTE o EN_PROCESO.
 */
public class CopyProcessCancelService implements ICopyProcessCancelPort {

    private final ICopyProcessRepositoryPort procesoRepo;
    private final IProcessEventRepositoryPort eventoRepo;

    public CopyProcessCancelService(
            ICopyProcessRepositoryPort procesoRepo,
            IProcessEventRepositoryPort eventoRepo
    ) {
        this.procesoRepo = procesoRepo;
        this.eventoRepo = eventoRepo;
    }

    @Override
    public void cancelar(String idProceso, String canceladoPor) {
        // 1. Verificar existencia
        CopyProcess proceso = procesoRepo.buscarPorId(idProceso)
                .orElseThrow(() -> new CopyProcessNotFoundException(idProceso));

        // 2. Verificar que no sea estado terminal (antes de validar transición)
        if (proceso.getEstado().isTerminal()) {
            throw new CopyProcessCancelDeniedException(idProceso, proceso.getEstado());
        }

        // 3. Validar transición mediante política (lanza InvalidPhaseTransitionException si inválido)
        PhaseTransitionPolicy.requireValidProcess(proceso.getEstado(), ProcessState.CANCELADO);

        // 4. Aplicar cancelación
        proceso.setEstado(ProcessState.CANCELADO);
        proceso.setFinalizadoEn(LocalDateTime.now());
        procesoRepo.actualizar(proceso);

        // 5. Emitir evento (REQ-EVT-01)
        CopyProcessEvent evento = CopyProcessEvent.crear(
                idProceso,
                CopyEventType.PROCESO_CANCELADO,
                "{\"canceladoPor\":\"" + canceladoPor + "\"}"
        );
        eventoRepo.guardar(evento);
    }
}
