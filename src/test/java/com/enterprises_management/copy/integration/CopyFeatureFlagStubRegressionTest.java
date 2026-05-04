package com.enterprises_management.copy.integration;

import com.enterprises_management.copy.application.output.IParticipantClientPort;
import com.enterprises_management.copy.infraestructure.adapters.output.participant.stub.StubParticipantClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de regresión del feature flag transport=stub (7.3.1, REQ-FLAG-01, REQ-CLIENT-01).
 *
 * <p>Verifica explícitamente que con {@code app.copy.orchestrator.participant.transport=stub}
 * el contexto Spring activa únicamente {@link StubParticipantClient} y NO registra
 * ningún {@code HttpParticipantClientAdapter}.
 *
 * <p>Este test garantiza que los 162 tests del Hito 1 seguirán pasando en cualquier
 * escenario donde el transporte sea stub — incluso después de la integración del
 * adaptador HTTP en Hito 2 (ADR-19, REQ-FLAG-01).
 *
 * <p>Cubre el escenario "flag stub preserva tests Hito 1" de REQ-CLIENT-01 (spec).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "app.copy.orchestrator.enabled=true",
    "app.copy.orchestrator.bootstrap-config=true",
    "app.copy.orchestrator.stub.failure-rate=0",
    "app.copy.orchestrator.stub.latency-ms=0",
    "app.copy.orchestrator.default-retries=3",
    // Flag crítico: transport=stub debe activar solo StubParticipantClient (ADR-19)
    "app.copy.orchestrator.participant.transport=stub",
    "app.copy.orchestrator.events.amqp.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:e2e_flag_stub;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.data-locations=",
    "jwt.auth.converter.principle-attribute=preferred_username",
    "jwt.auth.converter.resource-id=microservices_client",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
@DisplayName("Regresión Flag — transport=stub activa StubParticipantClient, no HttpParticipantClientAdapter")
class CopyFeatureFlagStubRegressionTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private List<IParticipantClientPort> participantes;

    // -------------------------------------------------------------------------
    // 7.3.1 — REQ-FLAG-01: con transport=stub, solo StubParticipantClient está activo
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("7.3.1 — transport=stub: todos los beans IParticipantClientPort son StubParticipantClient")
    void flagStub_todosLosBeansSonStubs() {
        // THEN — la lista de participantes no debe estar vacía
        assertThat(participantes)
                .withFailMessage("Con transport=stub deben existir beans IParticipantClientPort (stubs)")
                .isNotEmpty();

        // THEN — todos los beans deben ser StubParticipantClient (no HttpParticipantClientAdapter)
        participantes.forEach(participante ->
                assertThat(participante)
                        .withFailMessage("Con transport=stub, TODOS los participantes deben ser StubParticipantClient. " +
                                "Encontrado: %s (%s)", participante, participante.getClass().getSimpleName())
                        .isInstanceOf(StubParticipantClient.class)
        );
    }

    @Test
    @DisplayName("7.3.1 — transport=stub: no existe bean HttpParticipantClientAdapter en el contexto")
    void flagStub_noExisteHttpAdapter() {
        // THEN — ningún participante debe ser HttpParticipantClientAdapter
        boolean hayHttpAdapter = participantes.stream()
                .anyMatch(p -> p.getClass().getSimpleName().equals("HttpParticipantClientAdapter"));

        assertThat(hayHttpAdapter)
                .withFailMessage("Con transport=stub, NO debe existir ningún HttpParticipantClientAdapter en el contexto. " +
                        "Participantes encontrados: %s",
                        participantes.stream().map(p -> p.getClass().getSimpleName()).toList())
                .isFalse();
    }

    @Test
    @DisplayName("7.3.1 — transport=stub: 4 stubs activos (ENTERPRISES, CATALOGUE, PRODUCTS, THIRDS) — Hito 3")
    void flagStub_tresStubsActivos() {
        // THEN — 4 stubs: ENTERPRISES, CATALOGUE, PRODUCTS, THIRDS (añadido en Hito 3 task 5.3)
        List<String> modulos = participantes.stream()
                .map(IParticipantClientPort::getNombreModulo)
                .toList();

        assertThat(modulos)
                .withFailMessage("Con transport=stub, deben existir 4 stubs (ENTERPRISES, CATALOGUE, PRODUCTS, THIRDS). " +
                        "Módulos encontrados: %s", modulos)
                .containsExactlyInAnyOrder("ENTERPRISES", "CATALOGUE", "PRODUCTS", "THIRDS");
    }
}
