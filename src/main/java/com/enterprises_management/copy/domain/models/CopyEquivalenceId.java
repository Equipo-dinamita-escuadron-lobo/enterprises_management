package com.enterprises_management.copy.domain.models;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de dominio de una equivalencia entre ID viejo e ID nuevo.
 * Clave de negocio: (idProceso, modulo, tabla, idViejo) — REQ-EQ-01.
 * idViejo e idNuevo son VARCHAR(80) para soportar UUID y bigint (ADR-5).
 */
public class CopyEquivalenceId {

    private final String id;
    private final String idProceso;
    private final String modulo;
    private final String tabla;
    private final String idViejo;
    private String idNuevo;
    private final LocalDateTime registradoEn;

    public CopyEquivalenceId(
            String id,
            String idProceso,
            String modulo,
            String tabla,
            String idViejo,
            String idNuevo,
            LocalDateTime registradoEn
    ) {
        this.id = id;
        this.idProceso = idProceso;
        this.modulo = modulo;
        this.tabla = tabla;
        this.idViejo = idViejo;
        this.idNuevo = idNuevo;
        this.registradoEn = registradoEn;
    }

    /**
     * Factory method que crea una equivalencia nueva con ID único.
     *
     * @param idProceso ID del proceso padre
     * @param modulo    nombre del módulo que registra la equivalencia
     * @param tabla     nombre de la tabla de la entidad
     * @param idViejo   ID original en la empresa origen
     * @param idNuevo   ID nuevo en la empresa destino
     * @return nueva equivalencia con timestamp de registro
     */
    public static CopyEquivalenceId crear(
            String idProceso,
            String modulo,
            String tabla,
            String idViejo,
            String idNuevo
    ) {
        return new CopyEquivalenceId(
            UUID.randomUUID().toString(),
            idProceso,
            modulo,
            tabla,
            idViejo,
            idNuevo,
            LocalDateTime.now()
        );
    }

    // Getters

    public String getId() { return id; }
    public String getIdProceso() { return idProceso; }
    public String getModulo() { return modulo; }
    public String getTabla() { return tabla; }
    public String getIdViejo() { return idViejo; }
    public String getIdNuevo() { return idNuevo; }
    public LocalDateTime getRegistradoEn() { return registradoEn; }

    // Setter controlado para upsert

    public void setIdNuevo(String idNuevo) { this.idNuevo = idNuevo; }
}
