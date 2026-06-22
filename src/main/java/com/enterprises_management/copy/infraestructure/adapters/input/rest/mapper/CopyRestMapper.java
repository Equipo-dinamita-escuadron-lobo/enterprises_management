package com.enterprises_management.copy.infraestructure.adapters.input.rest.mapper;

import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyPhaseConfig;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import com.enterprises_management.copy.infraestructure.adapters.input.rest.dto.*;

import java.util.List;

/**
 * Mapper manual de DTOs REST ↔ comandos y modelos de dominio.
 * Se evita MapStruct para no agregar dependencias de anotaciones en el dominio hexagonal.
 */
public final class CopyRestMapper {

    private CopyRestMapper() {}

    /**
     * Convierte el request de inicio a comando de aplicación.
     * El iniciadoPor se extrae del JWT en el controller y se pasa aquí.
     *
     * @param request     DTO de request validado
     * @param iniciadoPor claim 'sub' del JWT
     * @return comando listo para el puerto de entrada
     */
    public static IniciarProcesoCommand toCommand(IniciarProcesoRequest request, String iniciadoPor) {
        String nombreDestino = request.empresaDestino() != null
                ? request.empresaDestino().nombre()
                : null;
        return new IniciarProcesoCommand(
                request.tipo(),
                request.empresaOrigen(),
                nombreDestino,
                request.backupRef(),
                iniciadoPor,
                request.generateBackup()
        );
    }

    /**
     * Convierte un CopyProcess al DTO de respuesta consolidado con fases.
     */
    public static ProcesoCopiaResponse toResponse(CopyProcess proceso, List<CopyPhase> fases) {
        List<FaseCopiaResponse> fasesDto = fases.stream()
                .map(CopyRestMapper::toFaseResponse)
                .toList();
        return new ProcesoCopiaResponse(
                proceso.getId(),
                proceso.getTipo(),
                proceso.getEstado(),
                proceso.getEmpresaOrigen(),
                proceso.getEmpresaDestino(),
                proceso.getBackupRef(),
                proceso.getSnapshotCorte(),
                proceso.getIniciadoPor(),
                proceso.getFaseActual(),
                proceso.getFinalizadoEn(),
                proceso.getErrorResumen(),
                fasesDto
        );
    }

    /**
     * Convierte una CopyPhase al DTO de respuesta.
     */
    public static FaseCopiaResponse toFaseResponse(CopyPhase fase) {
        return new FaseCopiaResponse(
                fase.getId(),
                fase.getIdProceso(),
                fase.getNumero(),
                fase.getNombre(),
                fase.getEstado(),
                fase.getIniciadaEn(),
                fase.getFinalizadaEn(),
                fase.getErrorDetalle()
        );
    }

    /**
     * Convierte un CopyProcessEvent al DTO de respuesta.
     */
    public static EventoProcesoResponse toEventoResponse(CopyProcessEvent evento) {
        return new EventoProcesoResponse(
                evento.getId(),
                evento.getIdProceso(),
                evento.getTipoEvento(),
                evento.getOcurridoEn(),
                evento.getPayloadJson()
        );
    }

    /**
     * Convierte un item de equivalencia de request a modelo de dominio.
     *
     * @param idProceso ID del proceso padre (de la URL)
     * @param item      ítem del request
     * @return modelo de dominio CopyEquivalenceId
     */
    public static CopyEquivalenceId toEquivalencia(String idProceso, EquivalenciaItemRequest item) {
        return CopyEquivalenceId.crear(idProceso, item.modulo(), item.tabla(), item.idViejo(), item.idNuevo());
    }

    /**
     * Convierte una equivalencia de dominio al DTO de respuesta.
     */
    public static EquivalenciaResponse toEquivalenciaResponse(CopyEquivalenceId eq) {
        return new EquivalenciaResponse(
                eq.getId(),
                eq.getIdProceso(),
                eq.getModulo(),
                eq.getTabla(),
                eq.getIdViejo(),
                eq.getIdNuevo(),
                eq.getRegistradoEn()
        );
    }

    /**
     * Convierte una configuración de fase al DTO de respuesta.
     */
    public static ConfiguracionFaseResponse toConfigResponse(CopyPhaseConfig config) {
        return new ConfiguracionFaseResponse(
                config.getId(),
                config.getNumeroFase(),
                config.getModulo(),
                config.getOrden(),
                config.isActivo(),
                config.getParametrosJson()
        );
    }
}
