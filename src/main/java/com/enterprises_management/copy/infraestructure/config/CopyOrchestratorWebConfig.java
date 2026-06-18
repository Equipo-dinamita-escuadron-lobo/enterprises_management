package com.enterprises_management.copy.infraestructure.config;

import com.enterprises_management.copy.application.input.IEquivalenciaLookupInputPort;
import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.ICopyRestoreInputPort;
import com.enterprises_management.copy.application.output.*;
import io.micrometer.core.instrument.MeterRegistry;
import com.enterprises_management.copy.application.services.*;
import com.enterprises_management.copy.infraestructure.adapters.output.amqp.LoggingEventPublisherAdapter;
import com.enterprises_management.copy.infraestructure.adapters.output.backup.ZipBackupReaderAdapter;
import com.enterprises_management.copy.infraestructure.adapters.output.backup.ZipBackupSerializerAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import com.enterprises_management.copy.infraestructure.adapters.output.notifier.NotificadorEstadoSseAdapter;
import com.enterprises_management.copy.infraestructure.adapters.output.notifier.SseEmitterRegistry;
import com.enterprises_management.copy.infraestructure.adapters.output.participant.local.LocalValidationParticipantAdapter;
import com.enterprises_management.copy.infraestructure.adapters.output.participant.stub.StubParticipantClient;
import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.adapter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Configuración de Spring Beans para el orquestador de copia.
 * Solo se activa cuando {@code app.copy.orchestrator.enabled=true} (ADR-14, REQ-FLAG-01).
 *
 * <p>Cuando el flag está en false, ningún controller ni service de copy se registra,
 * y las peticiones a /api/enterprises/copy/** devuelven 404.
 *
 * <p>Orden de declaración de beans:
 * <ol>
 *   <li>Infrastructure: SseEmitterRegistry, StubParticipantClient(s), adapters JPA</li>
 *   <li>Application services (dependen de los output ports)</li>
 * </ol>
 */
@Configuration
@ConditionalOnProperty(name = "app.copy.orchestrator.enabled", havingValue = "true")
@EnableConfigurationProperties(CopyOrchestratorProperties.class)
public class CopyOrchestratorWebConfig {

    private static final Logger log = LoggerFactory.getLogger(CopyOrchestratorWebConfig.class);

    // -------------------------------------------------------------------------
    // Infrastructure — SSE
    // -------------------------------------------------------------------------

    @Bean
    public SseEmitterRegistry sseEmitterRegistry() {
        log.info("Registrando SseEmitterRegistry para el orquestador de copia");
        return new SseEmitterRegistry();
    }

    /**
     * @Primary: desambigua IProcessNotifierPort cuando CopyProcessStreamService también lo implementa.
     * El SagaEngineService usa este adaptador para enviar eventos SSE a clientes conectados.
     * ADR-24: inyecta ObjectMapper para serialización JSON segura (REQ-SSE-01).
     */
    @Bean
    @Primary
    public NotificadorEstadoSseAdapter notificadorEstadoSseAdapter(SseEmitterRegistry registry, ObjectMapper objectMapper) {
        return new NotificadorEstadoSseAdapter(registry, objectMapper);
    }

    // -------------------------------------------------------------------------
    // Infrastructure — Stub participants (ADR-13)
    // Tres beans con el mismo tipo pero diferentes calificadores (uno por módulo)
    // -------------------------------------------------------------------------

    /**
     * Stub ENTERPRISES — activo únicamente cuando transport=stub (ADR-19, REQ-CLIENT-01).
     */
    @Bean("stubEnterprisesClient")
    @ConditionalOnProperty(name = "app.copy.orchestrator.participant.transport", havingValue = "stub")
    public StubParticipantClient stubEnterprisesClient(CopyOrchestratorProperties props) {
        return new StubParticipantClient(
                "ENTERPRISES",
                (int) props.getStub().getLatencyMs(),
                props.getStub().getFailureRate()
        );
    }

    /**
     * Stub CATALOGUE — activo únicamente cuando transport=stub (ADR-19, REQ-CLIENT-01).
     */
    @Bean("stubCatalogueClient")
    @ConditionalOnProperty(name = "app.copy.orchestrator.participant.transport", havingValue = "stub")
    public StubParticipantClient stubCatalogueClient(CopyOrchestratorProperties props) {
        return new StubParticipantClient(
                "CATALOGUE",
                (int) props.getStub().getLatencyMs(),
                props.getStub().getFailureRate()
        );
    }

    /**
     * Stub PRODUCTS — activo únicamente cuando transport=stub (ADR-19, REQ-CLIENT-01).
     */
    @Bean("stubProductsClient")
    @ConditionalOnProperty(name = "app.copy.orchestrator.participant.transport", havingValue = "stub")
    public StubParticipantClient stubProductsClient(CopyOrchestratorProperties props) {
        return new StubParticipantClient(
                "PRODUCTS",
                (int) props.getStub().getLatencyMs(),
                props.getStub().getFailureRate()
        );
    }

    /**
     * Stub THIRDS — activo únicamente cuando transport=stub (ADR-19, REQ-CLIENT-01).
     */
    @Bean("stubThirdsClient")
    @ConditionalOnProperty(name = "app.copy.orchestrator.participant.transport", havingValue = "stub")
    public StubParticipantClient stubThirdsClient(CopyOrchestratorProperties props) {
        return new StubParticipantClient(
                "THIRDS",
                (int) props.getStub().getLatencyMs(),
                props.getStub().getFailureRate()
        );
    }

    /** Stub TREASURY — activo cuando transport=stub Y enabled.treasury=true (ADR-19, REQ-FLAG-01). */
    @Bean("stubTreasuryClient")
    @ConditionalOnExpression("'${app.copy.orchestrator.participant.transport:}' == 'stub' && '${app.copy.orchestrator.participants.enabled.treasury:false}' == 'true'")
    public StubParticipantClient stubTreasuryClient(CopyOrchestratorProperties props) {
        return new StubParticipantClient("TREASURY",
                (int) props.getStub().getLatencyMs(), props.getStub().getFailureRate());
    }

    /** Stub STOCK — activo cuando transport=stub Y enabled.stock=true (ADR-19, REQ-FLAG-01). */
    @Bean("stubStockClient")
    @ConditionalOnExpression("'${app.copy.orchestrator.participant.transport:}' == 'stub' && '${app.copy.orchestrator.participants.enabled.stock:false}' == 'true'")
    public StubParticipantClient stubStockClient(CopyOrchestratorProperties props) {
        return new StubParticipantClient("STOCK",
                (int) props.getStub().getLatencyMs(), props.getStub().getFailureRate());
    }

    /** Stub KARDEX (weighted-average) — activo cuando transport=stub Y enabled.kardex=true (ADR-19, REQ-FLAG-01). */
    @Bean("stubKardexClient")
    @ConditionalOnExpression("'${app.copy.orchestrator.participant.transport:}' == 'stub' && '${app.copy.orchestrator.participants.enabled.kardex:false}' == 'true'")
    public StubParticipantClient stubKardexClient(CopyOrchestratorProperties props) {
        return new StubParticipantClient("KARDEX",
                (int) props.getStub().getLatencyMs(), props.getStub().getFailureRate());
    }

    /** Stub FACTURES — activo cuando transport=stub Y enabled.factures=true (ADR-19, REQ-FLAG-01). */
    @Bean("stubFacturesClient")
    @ConditionalOnExpression("'${app.copy.orchestrator.participant.transport:}' == 'stub' && '${app.copy.orchestrator.participants.enabled.factures:false}' == 'true'")
    public StubParticipantClient stubFacturesClient(CopyOrchestratorProperties props) {
        return new StubParticipantClient("FACTURES",
                (int) props.getStub().getLatencyMs(), props.getStub().getFailureRate());
    }

    /** Stub INVENTORYPEPS — activo cuando transport=stub Y enabled.inventorypeps=true (ADR-19, REQ-FLAG-01). */
    @Bean("stubInventoryPepsClient")
    @ConditionalOnExpression("'${app.copy.orchestrator.participant.transport:}' == 'stub' && '${app.copy.orchestrator.participants.enabled.inventorypeps:false}' == 'true'")
    public StubParticipantClient stubInventoryPepsClient(CopyOrchestratorProperties props) {
        return new StubParticipantClient("INVENTORYPEPS",
                (int) props.getStub().getLatencyMs(), props.getStub().getFailureRate());
    }

    /** Stub AUXILIARY-BOOK — activo cuando transport=stub Y enabled.auxiliarybook=true (ADR-19, REQ-FLAG-01). */
    @Bean("stubAuxiliaryBookClient")
    @ConditionalOnExpression("'${app.copy.orchestrator.participant.transport:}' == 'stub' && '${app.copy.orchestrator.participants.enabled.auxiliarybook:false}' == 'true'")
    public StubParticipantClient stubAuxiliaryBookClient(CopyOrchestratorProperties props) {
        return new StubParticipantClient("AUXILIARY-BOOK",
                (int) props.getStub().getLatencyMs(), props.getStub().getFailureRate());
    }

    /**
     * Participante local VALIDACION — siempre activo en fase CIERRE (Hito 8).
     * No depende del transport (stub vs http): consulta copy_equivalencias directamente.
     */
    @Bean("localValidationClient")
    public LocalValidationParticipantAdapter localValidationParticipantAdapter(
            IEquivalenceRepositoryPort equivalenciaRepo) {
        return new LocalValidationParticipantAdapter(equivalenciaRepo);
    }

    // -------------------------------------------------------------------------
    // Infrastructure — JPA/JDBC adapters
    // -------------------------------------------------------------------------

    @Bean
    public ICopyProcessRepositoryPort copyProcessRepositoryPort(JdbcTemplate jdbcTemplate) {
        return new CopyProcessJpaAdapter(jdbcTemplate);
    }

    @Bean
    public ICopyPhaseRepositoryPort copyPhaseRepositoryPort(JdbcTemplate jdbcTemplate) {
        return new CopyPhaseJpaAdapter(jdbcTemplate);
    }

    @Bean
    public IModuleExecutionRepositoryPort moduleExecutionRepositoryPort(JdbcTemplate jdbcTemplate) {
        return new CopyModuleExecutionJpaAdapter(jdbcTemplate);
    }

    @Bean
    public IEquivalenceRepositoryPort equivalenceRepositoryPort(JdbcTemplate jdbcTemplate) {
        return new CopyEquivalenceIdJpaAdapter(jdbcTemplate);
    }

    @Bean
    public IPhaseConfigRepositoryPort phaseConfigRepositoryPort(JdbcTemplate jdbcTemplate) {
        return new CopyPhaseConfigJpaAdapter(jdbcTemplate);
    }

    @Bean
    public IProcessEventRepositoryPort processEventRepositoryPort(JdbcTemplate jdbcTemplate) {
        return new CopyProcessEventJpaAdapter(jdbcTemplate);
    }

    // -------------------------------------------------------------------------
    // Application services
    // -------------------------------------------------------------------------

    @Bean
    public CopyProcessStartService copyProcessStartService(
            ICopyProcessRepositoryPort procesoRepo,
            ICopyPhaseRepositoryPort faseRepo,
            IProcessEventRepositoryPort eventoRepo
    ) {
        return new CopyProcessStartService(procesoRepo, faseRepo, eventoRepo);
    }

    @Bean
    public CopyProcessQueryService copyProcessQueryService(
            ICopyProcessRepositoryPort procesoRepo,
            ICopyPhaseRepositoryPort faseRepo,
            IProcessEventRepositoryPort eventoRepo
    ) {
        return new CopyProcessQueryService(procesoRepo, faseRepo, eventoRepo);
    }

    @Bean
    public CopyProcessCancelService copyProcessCancelService(
            ICopyProcessRepositoryPort procesoRepo,
            IProcessEventRepositoryPort eventoRepo
    ) {
        return new CopyProcessCancelService(procesoRepo, eventoRepo);
    }

    @Bean
    public EquivalenceRegistryService equivalenceRegistryService(
            ICopyProcessRepositoryPort procesoRepo,
            IEquivalenceRepositoryPort equivalenciaRepo
    ) {
        return new EquivalenceRegistryService(procesoRepo, equivalenciaRepo);
    }

    @Bean
    public CopyProcessStreamService copyProcessStreamService(
            ICopyProcessRepositoryPort procesoRepo
    ) {
        return new CopyProcessStreamService(procesoRepo);
    }

    /**
     * Publicador de eventos fallback (no-op, solo loguea).
     * Activo cuando RabbitEventPublisherAdapter NO está en el contexto
     * (perfil test o amqp.enabled=false) (REQ-EVENT-03, ADR-23).
     */
    @Bean
    @ConditionalOnMissingBean(IProcessEventPublisherPort.class)
    public LoggingEventPublisherAdapter loggingEventPublisherAdapter() {
        return new LoggingEventPublisherAdapter();
    }

    /**
     * Adaptador de serialización ZIP — escribe backup al finalizar proceso BACKUP/DUPLICATE
     * con generateBackup=true (REQ-BACKUP-01, ADR-44, ADR-45).
     */
    @Bean
    public IBackupSerializerPort backupSerializerPort(CopyOrchestratorProperties props, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        return new ZipBackupSerializerAdapter(props, objectMapper, jdbcTemplate);
    }

    /**
     * Adaptador de lectura ZIP — lee manifest y equivalencias desde un backup existente (ADR-46).
     */
    @Bean
    public IBackupReaderPort backupReaderPort(CopyOrchestratorProperties props, ObjectMapper objectMapper) {
        return new ZipBackupReaderAdapter(props, objectMapper);
    }

    /**
     * Servicio de aplicación que orquesta la restauración desde un backup (REQ-RESTORE-01, ADR-46).
     */
    @Bean
    public RestoreService restoreService(
            IBackupReaderPort backupReaderPort,
            ICopyProcessStartPort copyProcessStartService,
            IEquivalenceRepositoryPort equivalenceRepositoryPort
    ) {
        return new RestoreService(backupReaderPort, copyProcessStartService, equivalenceRepositoryPort);
    }

    /**
     * Cache de lookup de equivalencias con TTL configurable (REQ-LOOKUP-02, ADR-36).
     * TTL leído de {@code app.copy.orchestrator.lookup.cache-ttl-minutes} (default 5 min).
     */
    @Bean
    public LookupCacheService lookupCacheService(CopyOrchestratorProperties props) {
        return new LookupCacheService(props.getLookup().getCacheTtlMinutes());
    }

    /**
     * Servicio de lookup selectivo de equivalencias (REQ-LOOKUP-01, REQ-LOOKUP-02, ADR-35, ADR-36).
     *
     * <p>El límite de batch se lee de {@code app.copy.orchestrator.lookup.max-batch} (default 1000).
     * El cache per-instancia vive en el bean singleton hasta que el bean sea destruido (ADR-36).
     */
    @Bean
    public IEquivalenciaLookupInputPort equivalenciaLookupService(
            IEquivalenceRepositoryPort equivalenciaRepo,
            CopyOrchestratorProperties props,
            LookupCacheService lookupCacheService
    ) {
        return new EquivalenciaLookupService(
            equivalenciaRepo,
            props.getLookup().getMaxBatch(),
            lookupCacheService
        );
    }

    @Bean
    public SagaEngineService sagaEngineService(
            ICopyProcessRepositoryPort procesoRepo,
            ICopyPhaseRepositoryPort faseRepo,
            IModuleExecutionRepositoryPort moduloRepo,
            IEquivalenceRepositoryPort equivalenciaRepo,
            IPhaseConfigRepositoryPort configRepo,
            IProcessEventRepositoryPort eventoRepo,
            IProcessNotifierPort notificador,
            IProcessEventPublisherPort eventPublisher,
            List<IParticipantClientPort> participantes,
            CopyOrchestratorProperties props,
            IBackupSerializerPort backupSerializer,
            MeterRegistry meterRegistry
    ) {
        return new SagaEngineService(
                procesoRepo, faseRepo, moduloRepo, equivalenciaRepo, configRepo, eventoRepo,
                notificador, eventPublisher, participantes, props.getDefaultRetries(),
                backupSerializer, meterRegistry, props.isParallelExecution()
        );
    }
}
