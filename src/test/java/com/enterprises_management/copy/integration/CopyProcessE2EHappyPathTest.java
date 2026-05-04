package com.enterprises_management.copy.integration;

import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.application.services.SagaEngineService;
import com.enterprises_management.copy.domain.enums.PhaseState;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración E2E del flujo feliz del orquestador de saga (REQ-PROC-01, REQ-FASE-01).
 *
 * <p>Estrategia: @SpringBootTest(webEnvironment=NONE) carga el contexto completo
 * sin servidor HTTP. Los servicios de aplicación se inyectan directamente.
 * La BD es H2 en memoria (mode=create-drop por @SpringBootTest).
 * Los módulos usan StubParticipantClient con failure-rate=0 (siempre exitosos).
 * PhaseConfigBootstrap inserta la configuración de módulos al arrancar.
 *
 * <p>Verifica:
 * <ul>
 *   <li>Proceso pasa PENDIENTE → EN_PROCESO → COMPLETADO</li>
 *   <li>4 fases completadas en orden</li>
 *   <li>Tabla copy_process_event tiene ≥ 10 filas (REQ-EVT-01)</li>
 * </ul>
 *
 * <p>ADR-12: SagaEngineService.avanzarFase() se llama 4 veces (una por fase).
 * ADR-14: Flag enabled=true activa todos los beans del orquestador.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    // Activar orquestador con config bootstrap y sin latencia
    "app.copy.orchestrator.enabled=true",
    "app.copy.orchestrator.bootstrap-config=true",
    "app.copy.orchestrator.stub.failure-rate=0",
    "app.copy.orchestrator.stub.latency-ms=0",
    "app.copy.orchestrator.default-retries=3",
    // Hito 2 — transporte stub para que StubParticipantClient sea el bean activo (ADR-19)
    "app.copy.orchestrator.participant.transport=stub",
    "app.copy.orchestrator.events.amqp.enabled=false",
    // Usar H2 en memoria — ddl-auto=none evita que Hibernate genere DDL
    // para las entidades enterprise (sintaxis PostgreSQL incompatible con H2).
    // Solo se ejecuta schema-copy.sql (data.sql se omite: no aplica a H2).
    "spring.datasource.url=jdbc:h2:mem:e2e_happy;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    // Desactivar data.sql (solo aplica a PostgreSQL/empresa) — solo se necesita schema-copy.sql
    "spring.sql.init.data-locations=",
    // Propiedades JWT para JwtAuthConverter (evitar @Value null)
    "jwt.auth.converter.principle-attribute=preferred_username",
    "jwt.auth.converter.resource-id=microservices_client",
    // Eureka desactivado para entorno de test
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
@DisplayName("E2E — Flujo feliz del orquestador de saga (4 fases, stub, H2)")
class CopyProcessE2EHappyPathTest {

    /**
     * Mock del JwtDecoder — evita que Spring intente conectar a Keycloak al arrancar.
     * El test no usa HTTP, pero SecurityConfig sigue cargando el bean OAuth2.
     */
    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ICopyProcessStartPort startPort;

    @Autowired
    private SagaEngineService sagaEngineService;

    @Autowired
    private ICopyProcessQueryPort queryPort;

    @Autowired
    private IEquivalenceRepositoryPort equivalenciaRepo;

    // -------------------------------------------------------------------------
    // REQ-PROC-01, REQ-FASE-01, REQ-EVT-01, ADR-12
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Flujo completo: PENDIENTE → EN_PROCESO → 4 fases COMPLETADAS → COMPLETADO")
    void flujoFeliz_cuatroFases_procesoCompletado() {
        // GIVEN — proceso de duplicación con empresa origen
        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                empresaOrigen,
                "Empresa Destino E2E",
                null,
                "usuario-e2e-001",
                false
        );

        // WHEN — paso 1: iniciar proceso
        CopyProcess proceso = startPort.iniciar(comando);

        // THEN — proceso creado en PENDIENTE
        assertThat(proceso.getEstado()).isEqualTo(ProcessState.PENDIENTE);
        assertThat(proceso.getId()).isNotBlank();
        assertThat(proceso.getEmpresaOrigen()).isEqualTo(empresaOrigen);

        String idProceso = proceso.getId();

        // WHEN — pasos 2-5: avanzar las 4 fases (ADR-12: llamar avanzarFase para cada fase)
        sagaEngineService.avanzarFase(idProceso, 1);
        sagaEngineService.avanzarFase(idProceso, 2);
        sagaEngineService.avanzarFase(idProceso, 3);
        sagaEngineService.avanzarFase(idProceso, 4);

        // THEN — proceso COMPLETADO
        CopyProcess procesoFinal = queryPort.consultarProceso(idProceso);
        assertThat(procesoFinal.getEstado())
                .withFailMessage("El proceso debería estar COMPLETADO tras 4 fases exitosas")
                .isEqualTo(ProcessState.COMPLETADO);
        assertThat(procesoFinal.getFinalizadoEn()).isNotNull();
    }

    @Test
    @DisplayName("Fases 1-4 completadas en orden y en estado COMPLETADA")
    void flujoFeliz_cuatroFasesEnEstadoCompletada() {
        // GIVEN
        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.BACKUP,
                empresaOrigen,
                null,
                "backup-ref-001",
                "usuario-e2e-002",
                false
        );

        // WHEN
        CopyProcess proceso = startPort.iniciar(comando);
        String idProceso = proceso.getId();

        sagaEngineService.avanzarFase(idProceso, 1);
        sagaEngineService.avanzarFase(idProceso, 2);
        sagaEngineService.avanzarFase(idProceso, 3);
        sagaEngineService.avanzarFase(idProceso, 4);

        // THEN — las 4 fases deben estar COMPLETADAS
        List<com.enterprises_management.copy.domain.models.CopyPhase> fases =
                queryPort.consultarFases(idProceso);

        assertThat(fases).hasSize(4);
        assertThat(fases)
                .allSatisfy(fase ->
                    assertThat(fase.getEstado())
                        .withFailMessage("Fase %d debería estar COMPLETADA pero está %s",
                                fase.getNumero(), fase.getEstado())
                        .isEqualTo(PhaseState.COMPLETADA)
                );
    }

    @Test
    @DisplayName("Tabla copy_process_event tiene ≥ 10 filas tras flujo completo (REQ-EVT-01)")
    void flujoFeliz_eventosPersistidos() {
        // GIVEN
        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                empresaOrigen,
                "Empresa Destino Eventos",
                null,
                "usuario-e2e-003",
                false
        );

        // WHEN
        CopyProcess proceso = startPort.iniciar(comando);
        String idProceso = proceso.getId();

        sagaEngineService.avanzarFase(idProceso, 1);
        sagaEngineService.avanzarFase(idProceso, 2);
        sagaEngineService.avanzarFase(idProceso, 3);
        sagaEngineService.avanzarFase(idProceso, 4);

        // THEN — debe haber al menos 10 eventos:
        // 1 PROCESO_COPIA_INICIADO + 4 FASE_INICIADA + 4 FASE_COMPLETADA + N MODULO_* + 1 PROCESO_COMPLETADO
        List<CopyProcessEvent> eventos = queryPort.consultarEventos(idProceso);

        assertThat(eventos)
                .withFailMessage("Se esperaban ≥ 10 eventos, encontrados: %d — tipos: %s",
                        eventos.size(),
                        eventos.stream().map(e -> e.getTipoEvento().name()).toList())
                .hasSizeGreaterThanOrEqualTo(10);
    }

    @Test
    @DisplayName("Equivalencias: ≥ 9 registros en copy_equivalence_id tras flujo completo (ADR-13, 3 stubs × 3 eq)")
    void flujoFeliz_equivalenciasPersistidas_nineMasRegistros() {
        // GIVEN — proceso que corre la Fase 1 (3 módulos stub × 3 equivalencias = 9 mínimo)
        UUID empresaOrigen = UUID.randomUUID();
        IniciarProcesoCommand comando = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                empresaOrigen,
                "Empresa Equivalencias E2E",
                null,
                "usuario-e2e-equiv",
                false
        );

        // WHEN
        CopyProcess proceso = startPort.iniciar(comando);
        String idProceso = proceso.getId();

        // Ejecutar solo Fase 1 — tiene 3 módulos stub (ENTERPRISES, CATALOGUE, PRODUCTS)
        // Cada stub genera 3 equivalencias → total ≥ 9
        sagaEngineService.avanzarFase(idProceso, 1);
        sagaEngineService.avanzarFase(idProceso, 2);
        sagaEngineService.avanzarFase(idProceso, 3);
        sagaEngineService.avanzarFase(idProceso, 4);

        // THEN — al menos 9 equivalencias en total para el proceso
        List<CopyEquivalenceId> equivalencias = equivalenciaRepo.buscarConFiltros(idProceso, null, null, null);

        assertThat(equivalencias)
                .withFailMessage("Se esperaban ≥ 9 equivalencias (3 stubs × 3 eq), encontradas: %d", equivalencias.size())
                .hasSizeGreaterThanOrEqualTo(9);
    }
}
