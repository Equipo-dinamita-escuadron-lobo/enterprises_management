package com.enterprises_management.copy.application.services;

import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.output.ICopyPhaseRepositoryPort;
import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.application.output.IProcessEventRepositoryPort;
import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.exceptions.DuplicateActiveProcessException;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;

/**
 * Servicio de aplicación para iniciar un proceso de copia (REQ-PROC-01).
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>Verificar que no exista proceso activo para la misma empresa origen (→ 409)</li>
 *   <li>Crear el proceso con UUID propio y estado PENDIENTE</li>
 *   <li>Crear las 4 fases en estado PENDIENTE (REQ-FASE-01)</li>
 *   <li>Emitir evento ProcesoCopia.iniciado (REQ-EVT-01)</li>
 * </ul>
 */
public class CopyProcessStartService implements ICopyProcessStartPort {

    /** Nombres canónicos de las 4 fases, indexados 0-3. */
    private static final String[] NOMBRES_FASE = {"BASE", "INTERNAS", "EXTERNAS", "CIERRE"};

    private final ICopyProcessRepositoryPort procesoRepo;
    private final ICopyPhaseRepositoryPort faseRepo;
    private final IProcessEventRepositoryPort eventoRepo;

    public CopyProcessStartService(
            ICopyProcessRepositoryPort procesoRepo,
            ICopyPhaseRepositoryPort faseRepo,
            IProcessEventRepositoryPort eventoRepo
    ) {
        this.procesoRepo = procesoRepo;
        this.faseRepo = faseRepo;
        this.eventoRepo = eventoRepo;
    }

    @Override
    public CopyProcess iniciar(IniciarProcesoCommand command) {
        // 1. Verificar unicidad de proceso activo (ADR-4)
        if (procesoRepo.existeProcesoActivoPara(command.empresaOrigen())) {
            throw new DuplicateActiveProcessException(command.empresaOrigen());
        }

        // 2. Crear el proceso en estado PENDIENTE
        CopyProcess proceso = CopyProcess.crear(
                command.tipo(),
                command.empresaOrigen(),
                command.empresaDestino(),
                command.backupRef(),
                command.iniciadoPor()
        );
        proceso.setGenerateBackup(command.generateBackup());
        proceso = procesoRepo.guardar(proceso);

        // 3. Crear las 4 fases en estado PENDIENTE (REQ-FASE-01)
        for (int i = 0; i < NOMBRES_FASE.length; i++) {
            CopyPhase fase = CopyPhase.crear(proceso.getId(), i + 1, NOMBRES_FASE[i]);
            faseRepo.guardar(fase);
        }

        // 4. Emitir evento de inicio (REQ-EVT-01)
        CopyProcessEvent evento = CopyProcessEvent.crear(
                proceso.getId(),
                CopyEventType.PROCESO_COPIA_INICIADO,
                "{\"tipo\":\"" + proceso.getTipo().name() + "\"}"
        );
        eventoRepo.guardar(evento);

        return proceso;
    }
}
