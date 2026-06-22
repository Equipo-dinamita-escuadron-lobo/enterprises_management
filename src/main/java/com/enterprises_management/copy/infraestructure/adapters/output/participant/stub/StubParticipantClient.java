package com.enterprises_management.copy.infraestructure.adapters.output.participant.stub;

import com.enterprises_management.copy.application.output.IParticipantClientPort;
import com.enterprises_management.copy.domain.models.CopyModuleExecution;
import com.enterprises_management.copy.domain.models.CopyProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Stub configurable del cliente participante para Hito 1 (ADR-13).
 * Simula un módulo participante de la saga con latencia configurable y tasa de fallo.
 *
 * <p>Propiedades de configuración:
 * <ul>
 *   <li>{@code app.copy.orchestrator.stub.latency-ms} — retardo en milisegundos (default 100)</li>
 *   <li>{@code app.copy.orchestrator.stub.failure-rate} — tasa de fallo 0.0..1.0 (default 0.0)</li>
 * </ul>
 *
 * <p>Genera 3 equivalencias ficticias: idViejo=1..3, idNuevo=1001..1003 (idViejo+1000).
 *
 * @implNote Hito 1 — reemplazar por cliente HTTP real en Hito 2.
 */
public class StubParticipantClient implements IParticipantClientPort {

    private static final Logger log = LoggerFactory.getLogger(StubParticipantClient.class);

    private final String nombreModulo;
    private final int latencyMs;
    private final double failureRate;

    /**
     * Crea un stub con el nombre de módulo, latencia y tasa de fallo indicados.
     *
     * @param nombreModulo nombre del módulo (ej: "ENTERPRISES", "CATALOGUE", "PRODUCTS")
     * @param latencyMs    retardo en ms antes de responder (simula latencia de red)
     * @param failureRate  probabilidad de fallo 0.0..1.0 (0.0 = nunca falla, 1.0 = siempre falla)
     */
    public StubParticipantClient(String nombreModulo, int latencyMs, double failureRate) {
        this.nombreModulo = nombreModulo;
        this.latencyMs = latencyMs;
        this.failureRate = failureRate;
    }

    @Override
    public String getNombreModulo() {
        return nombreModulo;
    }

    @Override
    public ParticipantResult invoke(CopyProcess proceso, CopyModuleExecution ejecucion) {
        String correlationId = proceso.getId();
        log.info("[correlationId={}] Stub {} invocado. Latencia={}ms, failureRate={}",
                correlationId, nombreModulo, latencyMs, failureRate);

        // Simular latencia de red
        if (latencyMs > 0) {
            try {
                Thread.sleep(latencyMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("[correlationId={}] Stub {} interrumpido durante latencia", correlationId, nombreModulo);
            }
        }

        // Determinar si falla según failure-rate
        if (failureRate > 0.0 && Math.random() < failureRate) {
            log.warn("[correlationId={}] Stub {} retorna error reintentable (failure-rate={})",
                    correlationId, nombreModulo, failureRate);
            return ParticipantResult.errorReintentable(
                    "Stub: fallo simulado para módulo " + nombreModulo
            );
        }

        log.info("[correlationId={}] Stub {} retorna éxito con 3 equivalencias ficticias", correlationId, nombreModulo);
        return ParticipantResult.exitoConEquivalencias(generarEquivalenciasFicticias());
    }

    /**
     * Genera 3 equivalencias ficticias con la relación idViejo → idViejo+1000 (ADR-13).
     * Simula las equivalencias que generaría un módulo real al copiar entidades.
     *
     * @return lista de 3 equivalencias ficticias para tablas stub_table_a/b/c
     */
    private List<IParticipantClientPort.EquivalenciaResultado> generarEquivalenciasFicticias() {
        return List.of(
            new IParticipantClientPort.EquivalenciaResultado(nombreModulo, "stub_table_a", "1", "1001"),
            new IParticipantClientPort.EquivalenciaResultado(nombreModulo, "stub_table_b", "2", "1002"),
            new IParticipantClientPort.EquivalenciaResultado(nombreModulo, "stub_table_c", "3", "1003")
        );
    }
}
