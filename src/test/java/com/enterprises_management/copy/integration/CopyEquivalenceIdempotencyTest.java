package com.enterprises_management.copy.integration;

import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.IEquivalenceQueryPort;
import com.enterprises_management.copy.application.input.IEquivalenceRegistryPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.exceptions.EquivalenceConflictException;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyProcess;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integración E2E para la idempotencia de equivalencias (REQ-EQ-01, REQ-IDEM-01).
 *
 * <p>Verifica el comportamiento ADR-9:
 * <ul>
 *   <li>Mismo mapeo (clave + idNuevo) → idempotente, no duplica</li>
 *   <li>Mismo clave + idNuevo diferente → EquivalenceConflictException (409)</li>
 *   <li>GET con filtro idViejo retorna exactamente 1 fila</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "app.copy.orchestrator.enabled=true",
    "app.copy.orchestrator.bootstrap-config=true",
    "app.copy.orchestrator.stub.failure-rate=0",
    "app.copy.orchestrator.stub.latency-ms=0",
    "app.copy.orchestrator.default-retries=3",
    // Hito 2 — transporte stub para que StubParticipantClient sea el bean activo (ADR-19)
    "app.copy.orchestrator.participant.transport=stub",
    "app.copy.orchestrator.events.amqp.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:e2e_equiv;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
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
@DisplayName("E2E — Idempotencia de equivalencias (ADR-9, REQ-EQ-01, REQ-IDEM-01)")
class CopyEquivalenceIdempotencyTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ICopyProcessStartPort startPort;

    @Autowired
    private IEquivalenceRegistryPort registryPort;

    @Autowired
    private IEquivalenceQueryPort queryPort;

    // -------------------------------------------------------------------------
    // REQ-EQ-01, REQ-IDEM-01 — Idempotencia
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Registrar el mismo mapeo dos veces — segunda llamada idempotente, no duplica (ADR-9)")
    void registrarEquivalencia_dobleRegistro_idempotente() {
        // GIVEN — crear proceso para asociar la equivalencia
        CopyProcess proceso = crearProceso();
        String idProceso = proceso.getId();

        CopyEquivalenceId eq1 = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "58");
        CopyEquivalenceId eq1b = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "58");

        // WHEN — registrar dos veces el mismo mapeo
        int insertadas1 = registryPort.registrar(List.of(eq1));
        int insertadas2 = registryPort.registrar(List.of(eq1b));

        // THEN — primera inserción cuenta 1, segunda es idempotente (cuenta 0)
        assertThat(insertadas1)
                .withFailMessage("Primera inserción debe contar 1 equivalencia insertada")
                .isEqualTo(1);
        assertThat(insertadas2)
                .withFailMessage("Segunda inserción debe ser idempotente (0 insertadas), no duplicar")
                .isEqualTo(0);

        // Verificar que solo hay 1 fila en la BD
        List<CopyEquivalenceId> resultado = queryPort.consultar(idProceso, "PRODUCTS", "producto", "42");
        assertThat(resultado)
                .withFailMessage("Debe haber exactamente 1 fila, no duplicar por idempotencia")
                .hasSize(1);
        assertThat(resultado.get(0).getIdNuevo()).isEqualTo("58");
    }

    @Test
    @DisplayName("Registrar con idNuevo diferente para misma clave — EquivalenceConflictException (ADR-9)")
    void registrarEquivalencia_idNuevoDiferente_lanzaConflicto() {
        // GIVEN
        CopyProcess proceso = crearProceso();
        String idProceso = proceso.getId();

        CopyEquivalenceId original = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "58");
        registryPort.registrar(List.of(original));

        // Misma clave, idNuevo diferente (99 en lugar de 58)
        CopyEquivalenceId conflicto = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "99");

        // WHEN / THEN — debe lanzar EquivalenceConflictException (409)
        assertThatThrownBy(() -> registryPort.registrar(List.of(conflicto)))
                .isInstanceOf(EquivalenceConflictException.class);
    }

    @Test
    @DisplayName("GET con filtro idViejo retorna exactamente 1 fila con idNuevo correcto (REQ-EQ-02)")
    void consultarEquivalencias_filtroIdViejo_retornaUnaFila() {
        // GIVEN
        CopyProcess proceso = crearProceso();
        String idProceso = proceso.getId();

        // Registrar varias equivalencias
        registryPort.registrar(List.of(
                CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "58"),
                CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "100", "200"),
                CopyEquivalenceId.crear(idProceso, "CATALOGUE", "catalogo", "10", "20")
        ));

        // WHEN — consultar con filtro idViejo=42
        List<CopyEquivalenceId> resultado = queryPort.consultar(idProceso, null, null, "42");

        // THEN — exactamente 1 fila con idNuevo=58
        assertThat(resultado)
                .withFailMessage("Con filtro idViejo=42 debe retornar exactamente 1 fila")
                .hasSize(1);
        assertThat(resultado.get(0).getIdNuevo())
                .withFailMessage("La fila retornada debe tener idNuevo=58")
                .isEqualTo("58");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CopyProcess crearProceso() {
        return startPort.iniciar(new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE,
                UUID.randomUUID(),
                "Empresa Destino Equiv",
                null,
                "usuario-equiv",
                false
        ));
    }
}
