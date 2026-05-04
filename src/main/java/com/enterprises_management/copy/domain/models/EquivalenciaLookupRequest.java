package com.enterprises_management.copy.domain.models;

import java.util.List;

/**
 * Modelo de dominio para la solicitud de lookup selectivo de equivalencias.
 * REQ-LOOKUP-01, ADR-35.
 *
 * <p>Permite consultar en batch múltiples combinaciones (modulo, tabla, idsViejos[]).
 * El total de IDs en todos los ítems no puede superar el límite configurado
 * (por defecto 1000, {@code app.copy.orchestrator.lookup.max-batch}).
 */
public class EquivalenciaLookupRequest {

    private final List<LookupItem> requests;

    public EquivalenciaLookupRequest(List<LookupItem> requests) {
        this.requests = requests != null ? List.copyOf(requests) : List.of();
    }

    public List<LookupItem> getRequests() {
        return requests;
    }

    /**
     * Calcula el total de IDs en la solicitud.
     * Usado para validar el límite de batch antes de consultar el repositorio.
     */
    public int totalIds() {
        return requests.stream()
            .mapToInt(item -> item.getIdsViejos().size())
            .sum();
    }

    /**
     * Ítem individual de lookup: módulo + tabla + lista de IDs viejos.
     */
    public static class LookupItem {

        private final String modulo;
        private final String tabla;
        private final List<Long> idsViejos;

        public LookupItem(String modulo, String tabla, List<Long> idsViejos) {
            this.modulo = modulo;
            this.tabla = tabla;
            this.idsViejos = idsViejos != null ? List.copyOf(idsViejos) : List.of();
        }

        public String getModulo() { return modulo; }
        public String getTabla() { return tabla; }
        public List<Long> getIdsViejos() { return idsViejos; }
    }
}
