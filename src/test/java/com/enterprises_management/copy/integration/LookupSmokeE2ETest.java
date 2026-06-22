package com.enterprises_management.copy.integration;

import com.enterprises_management.copy.application.input.IEquivalenciaLookupInputPort;
import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.EquivalenciaLookupRequest;
import com.enterprises_management.copy.domain.models.EquivalenciaLookupResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test E2E del endpoint pull selectivo de equivalencias (REQ-LOOKUP-01, ADR-35).
 *
 * <p>Workaround sub-hito 4a (sin participante real consumiendo el endpoint):
 * <ul>
 *   <li>Seed manual de 100 equivalencias para un proceso de prueba.</li>
 *   <li>Llamada directa al servicio de aplicación {@link IEquivalenciaLookupInputPort}.</li>
 *   <li>Verificación de que los 100 matches están en la respuesta y notFound está vacío.</li>
 * </ul>
 *
 * <p>Task 1.6 — validación básica del endpoint disponible en 4a.
 * El ejercicio E2E real con participante se realizará en 4b (Fase 11).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "app.copy.orchestrator.enabled=true",
    "app.copy.orchestrator.bootstrap-config=true",
    "app.copy.orchestrator.stub.failure-rate=0",
    "app.copy.orchestrator.stub.latency-ms=0",
    "app.copy.orchestrator.default-retries=3",
    "app.copy.orchestrator.participant.transport=stub",
    "app.copy.orchestrator.events.amqp.enabled=false",
    "app.copy.orchestrator.lookup.max-batch=1000",
    "app.copy.orchestrator.lookup.cache-ttl-minutes=5",
    "spring.datasource.url=jdbc:h2:mem:e2e_lookup_smoke;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
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
@DisplayName("Smoke E2E — Endpoint pull lookup: 100 equivalencias seed → 100 matches (REQ-LOOKUP-01)")
class LookupSmokeE2ETest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ICopyProcessStartPort startPort;

    @Autowired
    private IEquivalenceRepositoryPort equivalenciaRepo;

    @Autowired
    private IEquivalenciaLookupInputPort lookupPort;

    // -------------------------------------------------------------------------
    // Smoke test: 100 equivalencias seed → lookup retorna 100 matches
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Smoke: seed 100 equivalencias → lookup batch 100 IDs → 100 matches, notFound vacío")
    void smoke_seed100Equivalencias_lookup100Matches() {
        // GIVEN — crear un proceso y seedear 100 equivalencias PRODUCTS:producto
        CopyProcess proceso = startPort.iniciar(new IniciarProcesoCommand(
            CopyProcessType.BACKUP,
            UUID.randomUUID(),
            null,
            "backup-smoke-lookup-001",
            "usuario-smoke-001",
            false
        ));
        String idProceso = proceso.getId();

        int totalEquivalencias = 100;
        List<Long> idsViejos = new ArrayList<>();
        for (long i = 1; i <= totalEquivalencias; i++) {
            equivalenciaRepo.guardar(
                com.enterprises_management.copy.domain.models.CopyEquivalenceId.crear(
                    idProceso, "PRODUCTS", "producto",
                    String.valueOf(i),
                    String.valueOf(i + 1000)  // idNuevo = idViejo + 1000
                )
            );
            idsViejos.add(i);
        }

        EquivalenciaLookupRequest request = new EquivalenciaLookupRequest(
            List.of(new EquivalenciaLookupRequest.LookupItem("PRODUCTS", "producto", idsViejos))
        );

        // WHEN
        EquivalenciaLookupResponse response = lookupPort.lookup(idProceso, request);

        // THEN — 100 matches, notFound vacío
        assertThat(response.getMappings())
            .hasSize(totalEquivalencias);
        assertThat(response.getNotFound())
            .isEmpty();

        // Verificar que la clave tiene el formato correcto y los valores son correctos
        for (long i = 1; i <= totalEquivalencias; i++) {
            String key = "PRODUCTS:producto:" + i;
            assertThat(response.getMappings()).containsKey(key);
            assertThat(response.getMappings().get(key)).isEqualTo(i + 1000);
        }
    }

    // -------------------------------------------------------------------------
    // Triangulación: lookup con IDs parcialmente no encontrados
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Smoke triangulación: 50 equivalencias seed + 50 IDs inexistentes → 50 matches + 50 notFound")
    void smoke_50Seed50Inexistentes_50Matches50NotFound() {
        // GIVEN — proceso nuevo con 50 equivalencias
        CopyProcess proceso = startPort.iniciar(new IniciarProcesoCommand(
            CopyProcessType.DUPLICATE,
            UUID.randomUUID(),
            "empresa-destino-smoke",
            null,
            "usuario-smoke-002",
            false
        ));
        String idProceso = proceso.getId();

        // Seed 50 equivalencias (IDs 1..50)
        for (long i = 1; i <= 50; i++) {
            equivalenciaRepo.guardar(
                com.enterprises_management.copy.domain.models.CopyEquivalenceId.crear(
                    idProceso, "CATALOGUE", "cuenta",
                    String.valueOf(i),
                    String.valueOf(i + 2000)
                )
            );
        }

        // Request: IDs 1..100 (50 existen, 51..100 no existen)
        List<Long> idsViejos = new ArrayList<>();
        for (long i = 1; i <= 100; i++) idsViejos.add(i);

        EquivalenciaLookupRequest request = new EquivalenciaLookupRequest(
            List.of(new EquivalenciaLookupRequest.LookupItem("CATALOGUE", "cuenta", idsViejos))
        );

        // WHEN
        EquivalenciaLookupResponse response = lookupPort.lookup(idProceso, request);

        // THEN
        assertThat(response.getMappings()).hasSize(50);
        assertThat(response.getNotFound()).hasSize(50);

        // Verificar que los notFound son los IDs 51..100
        List<Long> notFoundIds = response.getNotFound().stream()
            .map(EquivalenciaLookupResponse.NotFoundItem::getIdViejo)
            .sorted()
            .toList();
        for (int i = 0; i < 50; i++) {
            assertThat(notFoundIds.get(i)).isEqualTo(51 + i);
        }
    }
}
