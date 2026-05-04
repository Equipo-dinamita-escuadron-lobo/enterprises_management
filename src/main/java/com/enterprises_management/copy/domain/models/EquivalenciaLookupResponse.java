package com.enterprises_management.copy.domain.models;

import java.util.List;
import java.util.Map;

/**
 * Modelo de dominio para la respuesta del lookup selectivo de equivalencias.
 * REQ-LOOKUP-01, ADR-35.
 *
 * <p>Estructura de respuesta:
 * <ul>
 *   <li>{@code mappings}: Map de {@code "MODULO:tabla:idViejo"} → {@code idNuevo} para los encontrados.</li>
 *   <li>{@code notFound}: lista de items para los que no existe equivalencia.</li>
 * </ul>
 */
public class EquivalenciaLookupResponse {

    private final Map<String, Long> mappings;
    private final List<NotFoundItem> notFound;

    public EquivalenciaLookupResponse(Map<String, Long> mappings, List<NotFoundItem> notFound) {
        this.mappings = mappings != null ? Map.copyOf(mappings) : Map.of();
        this.notFound = notFound != null ? List.copyOf(notFound) : List.of();
    }

    public Map<String, Long> getMappings() { return mappings; }
    public List<NotFoundItem> getNotFound() { return notFound; }

    /**
     * Item de equivalencia no encontrada (ID solicitado que no tiene mapeo registrado).
     */
    public static class NotFoundItem {

        private final String modulo;
        private final String tabla;
        private final long idViejo;

        public NotFoundItem(String modulo, String tabla, long idViejo) {
            this.modulo = modulo;
            this.tabla = tabla;
            this.idViejo = idViejo;
        }

        public String getModulo() { return modulo; }
        public String getTabla() { return tabla; }
        public long getIdViejo() { return idViejo; }
    }
}
