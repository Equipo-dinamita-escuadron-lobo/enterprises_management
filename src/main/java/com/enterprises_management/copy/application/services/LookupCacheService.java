package com.enterprises_management.copy.application.services;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de cache en memoria para el lookup selectivo de equivalencias (REQ-LOOKUP-02, ADR-36).
 *
 * <p>Implementa un {@link ConcurrentHashMap} con TTL configurable por entrada.
 * La clave de cache es {@code "idProceso:MODULO:tabla:idViejo"} → {@code idNuevo}.
 *
 * <p>Estrategia de TTL: cada entrada almacena su timestamp de creación. Al acceder,
 * si la entrada es más antigua que el TTL configurado, se descarta y se retorna
 * {@link Optional#empty()} (cache MISS forzado → relecture del repositorio).
 *
 * <p>ADR-36: ConcurrentHashMap simple, scope per-bean, GC al fin del bean.
 * Rechazadas: Hibernate L1 (TX-bound), Caffeine (over-engineering), Redis (Hito 7).
 */
public class LookupCacheService {

    private final long ttlMillis;

    /** Entrada del cache con valor y timestamp de inserción. */
    private static class CacheEntry {
        final long value;
        final Instant insertadoEn;

        CacheEntry(long value) {
            this.value = value;
            this.insertadoEn = Instant.now();
        }

        boolean expirada(long ttlMillis) {
            return Instant.now().isAfter(insertadoEn.plusMillis(ttlMillis));
        }
    }

    private final ConcurrentHashMap<String, CacheEntry> store = new ConcurrentHashMap<>();

    /**
     * Construye el servicio con un TTL configurable.
     *
     * @param cacheTtlMinutes TTL en minutos (default 5 min, {@code app.copy.orchestrator.lookup.cache-ttl-minutes})
     */
    public LookupCacheService(int cacheTtlMinutes) {
        this.ttlMillis = (long) cacheTtlMinutes * 60 * 1000;
    }

    /**
     * Construye la clave de cache.
     *
     * @param idProceso ID del proceso
     * @param modulo    nombre del módulo
     * @param tabla     nombre de la tabla
     * @param idViejo   ID original
     * @return clave normalizada
     */
    public static String buildKey(String idProceso, String modulo, String tabla, long idViejo) {
        return idProceso + ":" + modulo + ":" + tabla + ":" + idViejo;
    }

    /**
     * Obtiene el valor del cache si existe y no expiró.
     *
     * @param key clave construida con {@link #buildKey}
     * @return Optional con el idNuevo, o empty si no existe o expiró
     */
    public Optional<Long> get(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expirada(ttlMillis)) {
            store.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value);
    }

    /**
     * Almacena un valor en el cache.
     *
     * @param key   clave construida con {@link #buildKey}
     * @param value idNuevo a cachear
     */
    public void put(String key, long value) {
        store.put(key, new CacheEntry(value));
    }

    /**
     * Verifica si una clave existe en el cache (sin considerar TTL, para tests).
     *
     * @param key clave a verificar
     * @return true si existe (puede estar expirada)
     */
    public boolean containsKey(String key) {
        return store.containsKey(key);
    }

    /** Tamaño actual del cache (solo para diagnóstico y tests). */
    public int size() {
        return store.size();
    }

    /** Limpia todo el cache (para tests). */
    public void clear() {
        store.clear();
    }
}
