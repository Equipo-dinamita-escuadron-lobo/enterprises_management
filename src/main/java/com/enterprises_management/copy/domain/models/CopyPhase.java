package com.enterprises_management.copy.domain.models;

import com.enterprises_management.copy.domain.enums.PhaseState;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de dominio de una fase de la saga de copia.
 * Cada proceso tiene exactamente 4 fases numeradas 1-4 (REQ-FASE-01).
 */
public class CopyPhase {

    private final String id;
    private final String idProceso;
    private final int numero;
    private final String nombre;
    private PhaseState estado;
    private LocalDateTime iniciadaEn;
    private LocalDateTime finalizadaEn;
    private String errorDetalle;

    public CopyPhase(String id, String idProceso, int numero, String nombre) {
        this.id = id;
        this.idProceso = idProceso;
        this.numero = numero;
        this.nombre = nombre;
        this.estado = PhaseState.PENDIENTE;
    }

    /**
     * Factory method que crea una fase nueva con ID único.
     *
     * @param idProceso ID del proceso padre
     * @param numero    número de la fase (1-4)
     * @param nombre    nombre descriptivo (BASE, INTERNAS, EXTERNAS, CIERRE)
     * @return nueva fase en estado PENDIENTE
     */
    public static CopyPhase crear(String idProceso, int numero, String nombre) {
        return new CopyPhase(UUID.randomUUID().toString(), idProceso, numero, nombre);
    }

    // Getters

    public String getId() { return id; }
    public String getIdProceso() { return idProceso; }
    public int getNumero() { return numero; }
    public String getNombre() { return nombre; }
    public PhaseState getEstado() { return estado; }
    public LocalDateTime getIniciadaEn() { return iniciadaEn; }
    public LocalDateTime getFinalizadaEn() { return finalizadaEn; }
    public String getErrorDetalle() { return errorDetalle; }

    // Setters controlados

    public void setEstado(PhaseState estado) { this.estado = estado; }
    public void setIniciadaEn(LocalDateTime iniciadaEn) { this.iniciadaEn = iniciadaEn; }
    public void setFinalizadaEn(LocalDateTime finalizadaEn) { this.finalizadaEn = finalizadaEn; }
    public void setErrorDetalle(String errorDetalle) { this.errorDetalle = errorDetalle; }
}
