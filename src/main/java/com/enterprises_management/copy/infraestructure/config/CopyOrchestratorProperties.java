package com.enterprises_management.copy.infraestructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración del orquestador de copia (ADR-14, REQ-FLAG-01, REQ-FLAG-02).
 * Prefijo: {@code app.copy.orchestrator}.
 *
 * <p>Valores por defecto seguros para producción:
 * <ul>
 *   <li>{@code enabled = false} — feature flag desactivado por defecto</li>
 *   <li>{@code securityPermissiveMode = false} — permisos estrictos por defecto</li>
 * </ul>
 *
 * <p>Activar en {@code application-dev.yml}:
 * <pre>
 * app.copy.orchestrator.enabled: true
 * app.copy.orchestrator.security.permissive-mode: true
 * </pre>
 */
@ConfigurationProperties(prefix = "app.copy.orchestrator")
public class CopyOrchestratorProperties {

    /** Feature flag principal — cuando false los controllers no se cargan (ADR-14). */
    private boolean enabled = false;

    /** Número de reintentos por módulo participante. */
    private int defaultRetries = 3;

    /** Si true, corre PhaseConfigBootstrap al arranque (REQ-CFG-02). */
    private boolean bootstrapConfig = false;

    /** Si true, usa StubParticipantClient en lugar de clientes reales. */
    private boolean stubMode = true;

    /** Configuración del stub de participantes. */
    private Stub stub = new Stub();

    /** Configuración del transporte HTTP hacia participantes (ADR-17, ADR-19). */
    private Participant participant = new Participant();

    /** Configuración de eventos AMQP (ADR-23, REQ-EVENT-01). */
    private Events events = new Events();

    /** Configuración de seguridad del bounded context copy. */
    private Security security = new Security();

    /** Configuración del endpoint pull selectivo de equivalencias (ADR-35, REQ-LOOKUP-01). */
    private Lookup lookup = new Lookup();

    /** Configuración de serialización/restauración de backups ZIP (ADR-44, REQ-BACKUP-01). */
    private Backup backup = new Backup();

    /**
     * Cuando true, los módulos de una fase con más de un participante se invocan en paralelo
     * via CompletableFuture (H3-R3). false por defecto para mantener semántica secuencial segura.
     */
    private boolean parallelExecution = false;

    // -------------------------------------------------------------------------
    // Getters y setters
    // -------------------------------------------------------------------------

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getDefaultRetries() { return defaultRetries; }
    public void setDefaultRetries(int defaultRetries) { this.defaultRetries = defaultRetries; }

    public boolean isBootstrapConfig() { return bootstrapConfig; }
    public void setBootstrapConfig(boolean bootstrapConfig) { this.bootstrapConfig = bootstrapConfig; }

    public boolean isStubMode() { return stubMode; }
    public void setStubMode(boolean stubMode) { this.stubMode = stubMode; }

    public Stub getStub() { return stub; }
    public void setStub(Stub stub) { this.stub = stub; }

    public Participant getParticipant() { return participant; }
    public void setParticipant(Participant participant) { this.participant = participant; }

    public Events getEvents() { return events; }
    public void setEvents(Events events) { this.events = events; }

    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }

    public Lookup getLookup() { return lookup; }
    public void setLookup(Lookup lookup) { this.lookup = lookup; }

    public Backup getBackup() { return backup; }
    public void setBackup(Backup backup) { this.backup = backup; }

    public boolean isParallelExecution() { return parallelExecution; }
    public void setParallelExecution(boolean parallelExecution) { this.parallelExecution = parallelExecution; }

    // -------------------------------------------------------------------------
    // Tipos anidados
    // -------------------------------------------------------------------------

    /**
     * Configuración del stub de participantes (ADR-13).
     */
    public static class Stub {
        /** Latencia simulada en milisegundos. */
        private long latencyMs = 0;

        /** Tasa de fallo simulada (0.0 = sin fallo, 1.0 = siempre falla). */
        private double failureRate = 0.0;

        public long getLatencyMs() { return latencyMs; }
        public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }

        public double getFailureRate() { return failureRate; }
        public void setFailureRate(double failureRate) { this.failureRate = failureRate; }
    }

    /**
     * Configuración del transporte HTTP hacia microservicios participantes (ADR-17, ADR-19).
     */
    public static class Participant {
        /** Modo de transporte: {@code stub} (Hito 1 y tests) o {@code http} (Hito 2+). */
        private String transport = "http";

        /** Timeout máximo por llamada HTTP en milisegundos (ADR-26, REQ-CLIENT-04). */
        private long timeoutMs = 30000;

        public String getTransport() { return transport; }
        public void setTransport(String transport) { this.transport = transport; }

        public long getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
    }

    /**
     * Configuración de eventos AMQP (ADR-23, REQ-EVENT-01).
     */
    public static class Events {
        /** Agrupa propiedades del broker AMQP. */
        private Amqp amqp = new Amqp();

        public Amqp getAmqp() { return amqp; }
        public void setAmqp(Amqp amqp) { this.amqp = amqp; }

        /**
         * Propiedades específicas de AMQP para eventos de copia.
         */
        public static class Amqp {
            /** Habilita la publicación de eventos vía RabbitMQ. false en perfil test. */
            private boolean enabled = true;

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
        }
    }

    /**
     * Configuración de seguridad del bounded context copy (ADR-15).
     */
    public static class Security {
        /**
         * Cuando true, los endpoints copy no requieren permisos Backup_*,
         * solo autenticación JWT (para entornos de desarrollo).
         * DEBE SER false en producción.
         */
        private boolean permissiveMode = false;

        public boolean isPermissiveMode() { return permissiveMode; }
        public void setPermissiveMode(boolean permissiveMode) { this.permissiveMode = permissiveMode; }
    }

    /**
     * Configuración de backups ZIP (ADR-44, ADR-45, REQ-BACKUP-01).
     * Prefijo: {@code app.copy.orchestrator.backup}.
     */
    public static class Backup {
        /** Directorio donde se escriben los archivos ZIP de backup. */
        private String dir = "uploads/backups";

        /** Tamaño máximo permitido por archivo ZIP en bytes (default 500 MB). */
        private long maxSizeBytes = 524_288_000L;

        public String getDir() { return dir; }
        public void setDir(String dir) { this.dir = dir; }

        public long getMaxSizeBytes() { return maxSizeBytes; }
        public void setMaxSizeBytes(long maxSizeBytes) { this.maxSizeBytes = maxSizeBytes; }
    }

    /**
     * Configuración del endpoint pull selectivo de equivalencias (ADR-35, REQ-LOOKUP-01).
     * Prefijo: {@code app.copy.orchestrator.lookup}.
     */
    public static class Lookup {
        /**
         * Límite máximo de IDs totales por request de lookup (REQ-LOOKUP-01).
         * Si se supera, el servicio lanza IllegalArgumentException → 400 Bad Request.
         * Default: 1000 (configurable en application.properties).
         */
        private int maxBatch = 1000;

        /**
         * TTL en minutos para las entradas del cache de lookup (REQ-LOOKUP-02).
         * Default: 5 minutos (configurable en application.properties).
         * Propiedad: {@code app.copy.orchestrator.lookup.cache-ttl-minutes}.
         */
        private int cacheTtlMinutes = 5;

        public int getMaxBatch() { return maxBatch; }
        public void setMaxBatch(int maxBatch) { this.maxBatch = maxBatch; }

        public int getCacheTtlMinutes() { return cacheTtlMinutes; }
        public void setCacheTtlMinutes(int cacheTtlMinutes) { this.cacheTtlMinutes = cacheTtlMinutes; }
    }
}
