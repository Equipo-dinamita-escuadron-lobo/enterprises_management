package com.enterprises_management.copy.application.services;

import com.enterprises_management.copy.application.input.ICopyProcessDeletePort;
import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.output.ICopyProcessDeleteRepositoryPort;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyProcess;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.copy.orchestrator.enabled", havingValue = "true")
public class CopyProcessDeleteService implements ICopyProcessDeletePort {

    private final ICopyProcessQueryPort queryPort;
    private final ICopyProcessDeleteRepositoryPort deleteRepositoryPort;

    private static final Set<ProcessState> TERMINAL = Set.of(
            ProcessState.COMPLETADO, ProcessState.ERROR, ProcessState.CANCELADO
    );

    @Override
    public void eliminar(String id) {
        CopyProcess process = queryPort.consultarProceso(id);
        if (!TERMINAL.contains(process.getEstado())) {
            throw new IllegalStateException(
                    "Solo se pueden eliminar procesos en estado terminal. Estado actual: " + process.getEstado()
            );
        }
        deleteRepositoryPort.deleteById(id);
    }
}
