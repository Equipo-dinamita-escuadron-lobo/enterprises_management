package com.enterprises_management.copy.infraestructure.adapters.input.rest.dto;

import com.enterprises_management.copy.domain.models.EquivalenciaLookupResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DTO de respuesta para el endpoint de lookup selectivo de equivalencias.
 * ADR-35: {mappings: Map&lt;"MODULO:tabla:idViejo", idNuevo&gt;, notFound: [...]}.
 *
 * <p>Los {@code mappings} son los IDs encontrados.
 * Los {@code notFound} son los IDs que no tienen equivalencia registrada.
 */
public class LookupResponseDto {

    private Map<String, Long> mappings;
    private List<NotFoundItemDto> notFound;

    public LookupResponseDto() {}

    public LookupResponseDto(Map<String, Long> mappings, List<NotFoundItemDto> notFound) {
        this.mappings = mappings;
        this.notFound = notFound;
    }

    /**
     * Construye un LookupResponseDto a partir del modelo de dominio.
     *
     * @param response modelo de dominio
     * @return DTO listo para serializar
     */
    public static LookupResponseDto fromDomain(EquivalenciaLookupResponse response) {
        List<NotFoundItemDto> notFoundDtos = response.getNotFound().stream()
            .map(nf -> new NotFoundItemDto(nf.getModulo(), nf.getTabla(), nf.getIdViejo()))
            .collect(Collectors.toList());
        return new LookupResponseDto(response.getMappings(), notFoundDtos);
    }

    public Map<String, Long> getMappings() { return mappings; }
    public void setMappings(Map<String, Long> mappings) { this.mappings = mappings; }

    public List<NotFoundItemDto> getNotFound() { return notFound; }
    public void setNotFound(List<NotFoundItemDto> notFound) { this.notFound = notFound; }

    /**
     * DTO de ítem no encontrado.
     */
    public static class NotFoundItemDto {

        private String modulo;
        private String tabla;
        private long idViejo;

        public NotFoundItemDto() {}

        public NotFoundItemDto(String modulo, String tabla, long idViejo) {
            this.modulo = modulo;
            this.tabla = tabla;
            this.idViejo = idViejo;
        }

        public String getModulo() { return modulo; }
        public void setModulo(String modulo) { this.modulo = modulo; }

        public String getTabla() { return tabla; }
        public void setTabla(String tabla) { this.tabla = tabla; }

        public long getIdViejo() { return idViejo; }
        public void setIdViejo(long idViejo) { this.idViejo = idViejo; }
    }
}
