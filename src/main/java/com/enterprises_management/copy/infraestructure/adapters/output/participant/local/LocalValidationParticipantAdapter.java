package com.enterprises_management.copy.infraestructure.adapters.output.participant.local;

import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.application.output.IParticipantClientPort;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyModuleExecution;
import com.enterprises_management.copy.domain.models.CopyProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Participante local de la fase CIERRE (fase 4).
 * Valida la integridad del proceso de copia comparando las equivalencias
 * generadas por cada módulo participante (Hito 8).
 *
 * <p>No realiza llamadas HTTP. Consulta directamente {@code copy_equivalencias}
 * para reportar cuántos registros fueron mapeados por módulo.
 * Registrado como @Bean en CopyOrchestratorWebConfig junto con el resto de participantes.
 */
public class LocalValidationParticipantAdapter implements IParticipantClientPort {

    private static final Logger log = LoggerFactory.getLogger(LocalValidationParticipantAdapter.class);
    private static final String NOMBRE_MODULO = "VALIDACION";

    private final IEquivalenceRepositoryPort equivalenciaRepo;

    public LocalValidationParticipantAdapter(IEquivalenceRepositoryPort equivalenciaRepo) {
        this.equivalenciaRepo = equivalenciaRepo;
    }

    @Override
    public String getNombreModulo() {
        return NOMBRE_MODULO;
    }

    @Override
    public ParticipantResult invoke(CopyProcess proceso, CopyModuleExecution ejecucion) {
        String idProceso = proceso.getId();
        log.info("[correlationId={}] Iniciando validación de integridad (fase CIERRE)", idProceso);

        List<CopyEquivalenceId> equivalencias =
                equivalenciaRepo.buscarConFiltros(idProceso, null, null, null);

        Map<String, Long> countPorModulo = equivalencias.stream()
                .collect(Collectors.groupingBy(CopyEquivalenceId::getModulo, Collectors.counting()));

        long total = equivalencias.size();

        log.info("[correlationId={}] Validación completada: {} equivalencias en {} módulos",
                idProceso, total, countPorModulo.size());

        if (total == 0) {
            log.warn("[correlationId={}] Ninguna equivalencia generada — empresa origen puede estar vacía", idProceso);
            return ParticipantResult.exitoConAdvertencias(
                    "Sin equivalencias registradas. La empresa origen puede estar vacía o todos los módulos fallaron silenciosamente.");
        }

        StringBuilder resumen = new StringBuilder("Integridad validada. Total=")
                .append(total)
                .append(" equivalencias |");

        countPorModulo.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> resumen.append(' ').append(e.getKey()).append(':').append(e.getValue()));

        List<String> modulosSinEquivalencias = countPorModulo.entrySet().stream()
                .filter(e -> e.getValue() == 0)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();

        log.info("[correlationId={}] {}", idProceso, resumen);

        if (!modulosSinEquivalencias.isEmpty()) {
            String advertencia = resumen + " | ADVERTENCIA: módulos con 0 equivalencias: " + modulosSinEquivalencias;
            log.warn("[correlationId={}] {}", idProceso, advertencia);
            return ParticipantResult.exitoConAdvertencias(advertencia);
        }

        return ParticipantResult.exitoConAdvertencias(resumen.toString());
    }
}
