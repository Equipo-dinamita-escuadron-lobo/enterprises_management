package com.enterprises_management.copy.infraestructure.adapters.input.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO de request para el endpoint de lookup selectivo de equivalencias.
 * POST /api/enterprises/copy/processes/{idProceso}/equivalences/lookup (REQ-LOOKUP-01, ADR-35).
 *
 * <p>El total de IDs en todos los ítems no puede superar el límite configurado
 * ({@code app.copy.orchestrator.lookup.max-batch}, por defecto 1000).
 */
public class LookupRequestDto {

    @NotEmpty(message = "requests no puede estar vacío")
    @Valid
    private List<LookupItemDto> requests;

    public LookupRequestDto() {}

    public LookupRequestDto(List<LookupItemDto> requests) {
        this.requests = requests;
    }

    public List<LookupItemDto> getRequests() { return requests; }
    public void setRequests(List<LookupItemDto> requests) { this.requests = requests; }

    /**
     * Ítem individual de lookup: módulo + tabla + lista de IDs viejos.
     */
    public static class LookupItemDto {

        private String modulo;
        private String tabla;
        private List<Long> idsViejos;

        public LookupItemDto() {}

        public LookupItemDto(String modulo, String tabla, List<Long> idsViejos) {
            this.modulo = modulo;
            this.tabla = tabla;
            this.idsViejos = idsViejos;
        }

        public String getModulo() { return modulo; }
        public void setModulo(String modulo) { this.modulo = modulo; }

        public String getTabla() { return tabla; }
        public void setTabla(String tabla) { this.tabla = tabla; }

        public List<Long> getIdsViejos() { return idsViejos; }
        public void setIdsViejos(List<Long> idsViejos) { this.idsViejos = idsViejos; }
    }
}
