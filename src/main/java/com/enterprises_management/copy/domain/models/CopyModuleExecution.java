package com.enterprises_management.copy.domain.models;

import com.enterprises_management.copy.domain.enums.ModuleExecutionState;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de dominio de la ejecución de un módulo participante en una fase.
 * Clave de idempotencia: (idProceso, idFase, modulo) — REQ-IDEM-01.
 */
public class CopyModuleExecution {

    private final String id;
    private final String idProceso;
    private final String idFase;
    private final String modulo;
    private ModuleExecutionState estado;
    private int intentos;
    private LocalDateTime iniciadoEn;
    private LocalDateTime finalizadoEn;
    private String errorDetalle;

    public CopyModuleExecution(String id, String idProceso, String idFase, String modulo) {
        this.id = id;
        this.idProceso = idProceso;
        this.idFase = idFase;
        this.modulo = modulo;
        this.estado = ModuleExecutionState.PENDIENTE;
        this.intentos = 0;
    }

    /**
     * Factory method que crea una ejecución nueva con ID único.
     *
     * @param idProceso ID del proceso padre
     * @param idFase    ID de la fase padre
     * @param modulo    nombre del módulo participante
     * @return nueva ejecución en estado PENDIENTE con intentos=0
     */
    public static CopyModuleExecution crear(String idProceso, String idFase, String modulo) {
        return new CopyModuleExecution(UUID.randomUUID().toString(), idProceso, idFase, modulo);
    }

    // Getters

    public String getId() { return id; }
    public String getIdProceso() { return idProceso; }
    public String getIdFase() { return idFase; }
    public String getModulo() { return modulo; }
    public ModuleExecutionState getEstado() { return estado; }
    public int getIntentos() { return intentos; }
    public LocalDateTime getIniciadoEn() { return iniciadoEn; }
    public LocalDateTime getFinalizadoEn() { return finalizadoEn; }
    public String getErrorDetalle() { return errorDetalle; }

    // Setters controlados

    public void setEstado(ModuleExecutionState estado) { this.estado = estado; }
    public void setIntentos(int intentos) { this.intentos = intentos; }
    public void incrementarIntentos() { this.intentos++; }
    public void setIniciadoEn(LocalDateTime iniciadoEn) { this.iniciadoEn = iniciadoEn; }
    public void setFinalizadoEn(LocalDateTime finalizadoEn) { this.finalizadoEn = finalizadoEn; }
    public void setErrorDetalle(String errorDetalle) { this.errorDetalle = errorDetalle; }
}
