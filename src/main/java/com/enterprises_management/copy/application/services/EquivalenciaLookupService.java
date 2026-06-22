package com.enterprises_management.copy.application.services;

import com.enterprises_management.copy.application.input.IEquivalenciaLookupInputPort;
import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.EquivalenciaLookupRequest;
import com.enterprises_management.copy.domain.models.EquivalenciaLookupResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de aplicación para el lookup selectivo de equivalencias (REQ-LOOKUP-01, REQ-LOOKUP-02, ADR-35, ADR-36).
 *
 * <p>Delega la lógica de cache a {@link LookupCacheService}, que implementa un
 * {@link java.util.concurrent.ConcurrentHashMap} con TTL configurable.
 *
 * <p>La primera llamada con una clave nueva consulta el repositorio y popula el cache.
 * Llamadas subsiguientes para la misma clave en el mismo proceso usan el cache (ADR-36).
 *
 * <p>El límite de IDs por request es configurable via {@code app.copy.orchestrator.lookup.max-batch}
 * (por defecto 1000). Si se supera, se lanza {@link IllegalArgumentException} antes de
 * consultar el repositorio.
 */
public class EquivalenciaLookupService implements IEquivalenciaLookupInputPort {

    private final IEquivalenceRepositoryPort equivalenciaRepo;
    private final int maxBatch;
    private final LookupCacheService cacheService;

    /**
     * Constructor completo con LookupCacheService externo (para tests o inyección DI).
     *
     * @param equivalenciaRepo repositorio de equivalencias
     * @param maxBatch         límite máximo de IDs por request
     * @param cacheService     servicio de cache
     */
    public EquivalenciaLookupService(
            IEquivalenceRepositoryPort equivalenciaRepo,
            int maxBatch,
            LookupCacheService cacheService
    ) {
        this.equivalenciaRepo = equivalenciaRepo;
        this.maxBatch = maxBatch;
        this.cacheService = cacheService;
    }

    /**
     * Constructor de conveniencia para tests (crea LookupCacheService con TTL 5 min).
     *
     * @param equivalenciaRepo repositorio de equivalencias
     * @param maxBatch         límite máximo de IDs por request
     */
    public EquivalenciaLookupService(IEquivalenceRepositoryPort equivalenciaRepo, int maxBatch) {
        this(equivalenciaRepo, maxBatch, new LookupCacheService(5));
    }

    /**
     * Consulta selectiva de equivalencias con validación de límite y cache TTL.
     *
     * @param idProceso ID del proceso de copia
     * @param request   solicitud con ítems (modulo, tabla, idsViejos[])
     * @return respuesta con mappings y notFound
     * @throws IllegalArgumentException si el total de IDs supera {@code maxBatch}
     */
    @Override
    public EquivalenciaLookupResponse lookup(String idProceso, EquivalenciaLookupRequest request) {
        int totalIds = request.totalIds();
        if (totalIds > maxBatch) {
            throw new IllegalArgumentException(
                "El total de IDs en el request (" + totalIds + ") supera el límite permitido (" + maxBatch + ")."
            );
        }

        Map<String, Long> mappings = new HashMap<>();
        List<EquivalenciaLookupResponse.NotFoundItem> notFound = new ArrayList<>();

        for (EquivalenciaLookupRequest.LookupItem item : request.getRequests()) {
            String modulo = item.getModulo();
            String tabla = item.getTabla();

            for (Long idViejo : item.getIdsViejos()) {
                String cacheKey = LookupCacheService.buildKey(idProceso, modulo, tabla, idViejo);
                String responseKey = modulo + ":" + tabla + ":" + idViejo;

                // Cache HIT
                Optional<Long> cached = cacheService.get(cacheKey);
                if (cached.isPresent()) {
                    mappings.put(responseKey, cached.get());
                    continue;
                }

                // Cache MISS → consultar repositorio
                Optional<CopyEquivalenceId> equivalencia =
                    equivalenciaRepo.buscarPorClave(idProceso, modulo, tabla, String.valueOf(idViejo));

                if (equivalencia.isPresent()) {
                    long idNuevo = Long.parseLong(equivalencia.get().getIdNuevo());
                    cacheService.put(cacheKey, idNuevo);
                    mappings.put(responseKey, idNuevo);
                } else {
                    notFound.add(new EquivalenciaLookupResponse.NotFoundItem(modulo, tabla, idViejo));
                }
            }
        }

        return new EquivalenciaLookupResponse(mappings, notFound);
    }
}
