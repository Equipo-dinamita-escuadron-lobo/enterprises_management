package com.enterprises_management.copy.infraestructure.adapters.output.httpclient;

import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.application.output.IParticipantClientPort;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyModuleExecution;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto.CopyEquivalenceDto;
import com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto.CopyPhaseRequestDto;
import com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto.CopyPhaseResponseDto;
import com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto.HttpToParticipantResultMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de salida que invoca el endpoint {@code POST /api/<modulo>/copy/phase}
 * del microservicio participante mediante WebClient (ADR-17, REQ-CLIENT-01, REQ-CLIENT-02).
 *
 * <p>No anotado con @Component — el bean se registra mediante
 * {@link HttpParticipantClientConfig} (activado solo con {@code enabled=true AND transport=http})
 * para tener control total sobre el WebClient (@LoadBalanced, baseUrl lb://) y timeout.
 *
 * <p>En perfil test, {@code application-test.properties} sobreescribe a {@code transport=stub},
 * por lo que este bean NO se instancia durante los tests del Hito 1 (ADR-19, REQ-FLAG-01).
 *
 * <p>Headers propagados en cada llamada (ADR-18, ADR-29):
 * <ul>
 *   <li>{@code X-Process-Id} — ID del proceso de copia para correlación</li>
 *   <li>{@code X-Internal-Source: enterprises-management} — identificación del llamador interno</li>
 *   <li>{@code Authorization: Bearer <token>} — Bearer del request entrante si está disponible (ADR-29)</li>
 * </ul>
 *
 * <p>Tabla de traducción HTTP → ParticipantResult (REQ-CLIENT-03, ADR-26):
 * <pre>
 *   200 COMPLETADO                  → exitoso=true
 *   200 COMPLETADO_CON_ADVERTENCIAS → exitoso=true, conAdvertencias=true
 *   200 ERROR_REINTENTABLE          → exitoso=false, reintentable=true
 *   200 ERROR_NO_REINTENTABLE       → exitoso=false, reintentable=false
 *   4xx                             → exitoso=false, reintentable=false
 *   5xx                             → exitoso=false, reintentable=true
 *   timeout / connection refused    → exitoso=false, reintentable=true
 * </pre>
 *
 * <p>Mapa de dependencias de equivalencias previas por módulo (ADR-28):
 * PRODUCTS depende de CATALOGUE; CATALOGUE, ENTERPRISES y THIRDS no dependen de nada.
 */
public class HttpParticipantClientAdapter implements IParticipantClientPort {

    private static final Logger log = LoggerFactory.getLogger(HttpParticipantClientAdapter.class);

    /** Header de identificación interna — no expuesto al exterior por el gateway (ADR-18). */
    private static final String HEADER_INTERNAL_SOURCE = "X-Internal-Source";

    /** Header de correlación del proceso de copia. */
    private static final String HEADER_PROCESS_ID = "X-Process-Id";

    /** Header de autorización para propagar el Bearer al participante (ADR-29). */
    private static final String HEADER_AUTHORIZATION = "Authorization";

    /** Valor del header de fuente interna. */
    private static final String FUENTE_INTERNA = "enterprises-management";

    /**
     * Mapa estático de dependencias de equivalencias previas por módulo (ADR-28).
     * Clave: módulo destino. Valor: conjunto de módulos fuente cuyas equivalencias se deben pasar.
     */
    static final Map<String, Set<String>> PARTICIPANT_DEPENDENCIES = Map.of(
            "PRODUCTS",       Set.of("CATALOGUE"),
            "CATALOGUE",      Set.of(),
            "ENTERPRISES",    Set.of(),
            "THIRDS",         Set.of(),
            "TREASURY",       Set.of("CATALOGUE"),
            "STOCK",          Set.of("PRODUCTS"),
            "KARDEX",         Set.of("PRODUCTS"),
            "FACTURES",       Set.of("PRODUCTS", "THIRDS"),
            "INVENTORYPEPS",  Set.of("PRODUCTS"),
            "AUXILIARY-BOOK", Set.of("CATALOGUE", "THIRDS")
    );

    private final String nombreModulo;
    private final String nombreModuloUrl;
    private final WebClient webClient;
    private final long timeoutMs;
    private final IEquivalenceRepositoryPort equivalenciaRepo;
    private final MeterRegistry meterRegistry;

    /**
     * Constructor principal — usado por el contenedor Spring vía {@link HttpParticipantClientConfig}.
     * También es el constructor que usan los tests con un WebClient directo (Opción C, ADR-25).
     *
     * @param nombreModulo    nombre del módulo Eureka (ej: "CATALOGUE") — para getNombreModulo()
     * @param nombreModuloUrl segmento de URL del módulo (ej: "accountCatalogue") — para el path
     * @param webClient       cliente HTTP ya configurado (con o sin @LoadBalanced)
     * @param timeoutMs       timeout en ms para cada invocación (REQ-CLIENT-04, ADR-26)
     * @param equivalenciaRepo repositorio para cargar equivalencias previas (REQ-EQUIVPREV-01, ADR-28)
     * @param meterRegistry   registro de métricas para temporización de invocaciones (REQ-METRICS-02)
     */
    public HttpParticipantClientAdapter(
            String nombreModulo,
            String nombreModuloUrl,
            WebClient webClient,
            long timeoutMs,
            IEquivalenceRepositoryPort equivalenciaRepo,
            MeterRegistry meterRegistry
    ) {
        this.nombreModulo = nombreModulo;
        this.nombreModuloUrl = nombreModuloUrl;
        this.webClient = webClient;
        this.timeoutMs = timeoutMs;
        this.equivalenciaRepo = equivalenciaRepo;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Constructor de compatibilidad para tests E2E de Hito 1+2 que no requieren equivalencias.
     * Usa un repositorio NOP que retorna listas vacías y un SimpleMeterRegistry (ADR-55).
     *
     * @param nombreModulo    nombre del módulo Eureka
     * @param nombreModuloUrl segmento de URL del módulo
     * @param webClient       cliente HTTP ya configurado
     * @param timeoutMs       timeout en ms
     */
    public HttpParticipantClientAdapter(
            String nombreModulo,
            String nombreModuloUrl,
            WebClient webClient,
            long timeoutMs
    ) {
        this(nombreModulo, nombreModuloUrl, webClient, timeoutMs, new IEquivalenceRepositoryPort() {
            @Override
            public CopyEquivalenceId guardar(CopyEquivalenceId eq) { return eq; }
            @Override
            public CopyEquivalenceId actualizar(CopyEquivalenceId eq) { return eq; }
            @Override
            public java.util.Optional<CopyEquivalenceId> buscarPorClave(String idProceso, String modulo, String tabla, String idViejo) {
                return java.util.Optional.empty();
            }
            @Override
            public List<CopyEquivalenceId> buscarConFiltros(String idProceso, String modulo, String tabla, String idViejo) {
                return List.of();
            }
        }, new SimpleMeterRegistry());
    }

    @Override
    public String getNombreModulo() {
        return nombreModulo;
    }

    /**
     * Invoca al participante enviando un {@code POST /api/{modulo}/copy/phase}.
     *
     * <p>Bloquea el hilo del pool de saga con {@code .block(timeout)} ya que
     * {@link com.enterprises_management.copy.application.services.SagaEngineService}
     * corre en un pool de hilos fijo (ADR-17, ADR-26).
     *
     * @param proceso   proceso de copia (para contexto y construcción del request)
     * @param ejecucion ejecución de módulo que se está lanzando
     * @return resultado normalizado de la invocación
     */
    @Override
    public ParticipantResult invoke(CopyProcess proceso, CopyModuleExecution ejecucion) {
        String correlacionId = proceso.getId();
        String urlPath = "/api/" + nombreModuloUrl + "/copy/phase";

        log.info("[correlationId={}] HTTP POST {} → {}", correlacionId, nombreModulo, urlPath);

        CopyPhaseRequestDto requestBody = construirRequest(proceso, ejecucion);

        // Construir el request HTTP con los headers de correlación y Bearer opcional (ADR-29)
        WebClient.RequestBodySpec requestSpec = webClient.post()
                .uri(urlPath)
                .header(HEADER_INTERNAL_SOURCE, FUENTE_INTERNA)
                .header(HEADER_PROCESS_ID, correlacionId);

        // Propagar Bearer solo si está disponible en el proceso (ADR-29)
        String bearer = proceso.getBearerToken();
        if (bearer != null && !bearer.isBlank()) {
            long remainingSecs = JwtExpiryUtil.secondsUntilExpiry(bearer);
            if (remainingSecs < 0) {
                log.warn("[JWT-EXPIRY][correlationId={}] Token vencido hace {}s — llamada a {} fallará con 401 (H2-R1)",
                        correlacionId, -remainingSecs, nombreModulo);
                meterRegistry.counter("copy.participant.auth.token.expired", "module", nombreModulo.toLowerCase()).increment();
            } else if (remainingSecs < 120) {
                log.warn("[JWT-EXPIRY][correlationId={}] Token vence en {}s — riesgo de 401 en {} (H2-R1)",
                        correlacionId, remainingSecs, nombreModulo);
                meterRegistry.counter("copy.participant.auth.token.near.expiry", "module", nombreModulo.toLowerCase()).increment();
            }
            requestSpec = requestSpec.header(HEADER_AUTHORIZATION, "Bearer " + bearer);
        }

        Timer.Sample sample = Timer.start(meterRegistry);
        ParticipantResult resultado;
        try {
            // retrieve() deserializa el body en 2xx y lanza WebClientResponseException en 4xx/5xx,
            // que capturamos abajo para traducir al mapper (REQ-CLIENT-03, ADR-26).
            resultado = requestSpec
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(CopyPhaseResponseDto.class)
                    .map(body -> HttpToParticipantResultMapper.from(body, 200))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (resultado == null) {
                log.warn("[correlationId={}] Respuesta sin body del participante {}", correlacionId, nombreModulo);
                resultado = ParticipantResult.errorNoReintentable("Respuesta sin body");
            } else {
                log.info("[correlationId={}] Participante {} respondió: exitoso={}, reintentable={}",
                        correlacionId, nombreModulo, resultado.exitoso(), resultado.reintentable());
            }

        } catch (WebClientResponseException ex) {
            int status = ex.getStatusCode().value();
            if (status == 401) {
                log.error("[JWT-EXPIRY][correlationId={}] Participante {} devolvió 401 — Bearer vencido o ausente (H2-R1)",
                        correlacionId, nombreModulo);
                meterRegistry.counter("copy.participant.auth.error",
                        "module", nombreModulo.toLowerCase(), "status", "401").increment();
            } else {
                log.warn("[correlationId={}] HTTP {} del participante {} — {}",
                        correlacionId, status, nombreModulo, ex.getMessage());
            }
            resultado = HttpToParticipantResultMapper.fromHttpError(status, ex.getMessage());
        } catch (WebClientRequestException ex) {
            // Timeout de WebClient o connection refused (REQ-CLIENT-04, REQ-CLIENT-02)
            log.warn("[correlationId={}] Error de conexión con participante {} — {}",
                    correlacionId, nombreModulo, ex.getMessage());
            resultado = HttpToParticipantResultMapper.fromTimeout(
                    "Error de conexión con " + nombreModulo + ": " + ex.getMessage()
            );
        } catch (Exception ex) {
            // Captura genérica para RuntimeException que envuelven TimeoutException (reactor)
            Throwable causa = desenredarCausa(ex);
            if (causa instanceof java.util.concurrent.TimeoutException
                    || (causa != null && causa.getMessage() != null && causa.getMessage().contains("Did not observe any item or terminal signal within"))) {
                log.warn("[correlationId={}] Timeout (envuelto) tras {}ms con participante {}",
                        correlacionId, timeoutMs, nombreModulo);
                resultado = HttpToParticipantResultMapper.fromTimeout(
                        "timeout tras " + timeoutMs + "ms con " + nombreModulo
                );
            } else if (esCausaConexionRechazada(ex)) {
                log.warn("[correlationId={}] Conexión rechazada por participante {} — {}",
                        correlacionId, nombreModulo, ex.getMessage());
                resultado = HttpToParticipantResultMapper.fromTimeout(
                        "servicio no disponible: " + nombreModulo
                );
            } else {
                log.error("[correlationId={}] Error inesperado invocando participante {} — {}",
                        correlacionId, nombreModulo, ex.getMessage(), ex);
                resultado = ParticipantResult.errorReintentable(
                        "Error inesperado: " + ex.getMessage()
                );
            }
        }
        sample.stop(meterRegistry.timer(
                "copy.participant.invoke.duration",
                "module", nombreModulo.toLowerCase(),
                "exitoso", String.valueOf(resultado.exitoso())
        ));
        return resultado;
    }

    // -------------------------------------------------------------------------
    // Métodos privados
    // -------------------------------------------------------------------------

    /**
     * Construye el body del request a partir del proceso y la ejecución actual.
     *
     * <p>Usa {@code proceso.getFaseActual()} para el campo {@code fase} (REQ-FIX-01, ADR-27).
     * Carga equivalencias previas filtradas según {@link #PARTICIPANT_DEPENDENCIES} (REQ-EQUIVPREV-01, ADR-28).
     *
     * @param proceso   proceso de copia
     * @param ejecucion ejecución del módulo actual
     * @return DTO del request listo para enviar
     */
    private CopyPhaseRequestDto construirRequest(CopyProcess proceso, CopyModuleExecution ejecucion) {
        // REQ-FIX-01: usar la fase real del proceso, no un literal hardcodeado
        int fase = proceso.getFaseActual();

        // REQ-EQUIVPREV-01, ADR-28: cargar equivalencias previas filtradas por dependencias del módulo
        List<CopyEquivalenceDto> equivalenciasPrev = cargarEquivalenciasPrev(proceso.getId());

        return new CopyPhaseRequestDto(
                UUID.fromString(proceso.getId()),
                fase,
                proceso.getEmpresaOrigen() != null ? proceso.getEmpresaOrigen().toString() : "",
                proceso.getEmpresaDestino() != null ? proceso.getEmpresaDestino() : "",
                proceso.getSnapshotCorte() != null ? proceso.getSnapshotCorte().toInstant(
                        java.time.ZoneOffset.UTC) : java.time.Instant.now(),
                equivalenciasPrev
        );
    }

    /**
     * Carga las equivalencias previas pertinentes para este módulo (ADR-28).
     *
     * <p>Consulta todas las equivalencias del proceso y filtra solo las generadas
     * por los módulos de los que este módulo depende, según {@link #PARTICIPANT_DEPENDENCIES}.
     *
     * @param idProceso ID del proceso
     * @return lista de DTOs de equivalencias filtradas (puede ser vacía)
     */
    private List<CopyEquivalenceDto> cargarEquivalenciasPrev(String idProceso) {
        Set<String> modulosDependencia = PARTICIPANT_DEPENDENCIES.getOrDefault(nombreModulo, Set.of());

        if (modulosDependencia.isEmpty()) {
            return List.of();
        }

        // Obtener todas las equivalencias del proceso y filtrar por módulos fuente
        List<CopyEquivalenceId> todasEquivalencias =
                equivalenciaRepo.buscarConFiltros(idProceso, null, null, null);

        return todasEquivalencias.stream()
                .filter(eq -> modulosDependencia.contains(eq.getModulo()))
                .map(eq -> new CopyEquivalenceDto(
                        eq.getModulo(),
                        eq.getTabla(),
                        eq.getIdViejo(),
                        eq.getIdNuevo()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Desenvuelve la cadena de causas de una excepción buscando la raíz real.
     * Útil para detectar TimeoutException envuelto por reactor en RuntimeException.
     */
    private Throwable desenredarCausa(Throwable ex) {
        Throwable causa = ex;
        int profundidad = 0;
        while (causa.getCause() != null && profundidad < 10) {
            causa = causa.getCause();
            profundidad++;
        }
        return causa;
    }

    /**
     * Determina si la excepción (o su causa raíz) indica conexión rechazada.
     */
    private boolean esCausaConexionRechazada(Exception ex) {
        Throwable causa = ex;
        while (causa != null) {
            if (causa instanceof java.net.ConnectException
                    || (causa.getMessage() != null && causa.getMessage().contains("Connection refused"))) {
                return true;
            }
            causa = causa.getCause();
        }
        return false;
    }
}
