package com.enterprises_management.copy.domain.models;

import java.util.UUID;

/**
 * Modelo de dominio de la configuración de un módulo en una fase.
 * Permite cambiar los módulos participantes sin modificar código (REQ-CFG-01).
 */
public class CopyPhaseConfig {

    private final String id;
    private final int numeroFase;
    private final String modulo;
    private final int orden;
    private final boolean activo;
    private final String parametrosJson;

    public CopyPhaseConfig(
            String id,
            int numeroFase,
            String modulo,
            int orden,
            boolean activo,
            String parametrosJson
    ) {
        this.id = id;
        this.numeroFase = numeroFase;
        this.modulo = modulo;
        this.orden = orden;
        this.activo = activo;
        this.parametrosJson = parametrosJson;
    }

    /**
     * Factory method que crea una configuración nueva con ID único.
     *
     * @param numeroFase    número de la fase (1-4)
     * @param modulo        nombre del módulo participante
     * @param orden         orden de ejecución dentro de la fase
     * @param activo        si el módulo está activo en esta fase
     * @param parametrosJson configuración adicional en JSON (puede ser null)
     * @return nueva configuración de fase
     */
    public static CopyPhaseConfig crear(
            int numeroFase,
            String modulo,
            int orden,
            boolean activo,
            String parametrosJson
    ) {
        return new CopyPhaseConfig(
            UUID.randomUUID().toString(),
            numeroFase,
            modulo,
            orden,
            activo,
            parametrosJson
        );
    }

    // Getters

    public String getId() { return id; }
    public int getNumeroFase() { return numeroFase; }
    public String getModulo() { return modulo; }
    public int getOrden() { return orden; }
    public boolean isActivo() { return activo; }
    public String getParametrosJson() { return parametrosJson; }
}
