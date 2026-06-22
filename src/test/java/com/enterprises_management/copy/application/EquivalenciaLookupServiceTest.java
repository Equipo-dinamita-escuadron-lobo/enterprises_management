package com.enterprises_management.copy.application;

import com.enterprises_management.copy.application.input.IEquivalenciaLookupInputPort;
import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.application.services.EquivalenciaLookupService;
import com.enterprises_management.copy.application.services.LookupCacheService;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.EquivalenciaLookupRequest;
import com.enterprises_management.copy.domain.models.EquivalenciaLookupResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para EquivalenciaLookupService (REQ-LOOKUP-01, REQ-LOOKUP-02, ADR-35, ADR-36).
 * TDD ESTRICTO — task 1.1 RED.
 */
@ExtendWith(MockitoExtension.class)
class EquivalenciaLookupServiceTest {

    @Mock
    private IEquivalenceRepositoryPort equivalenciaRepo;

    private LookupCacheService lookupCacheService;
    private EquivalenciaLookupService service;

    private static final int MAX_BATCH = 1000;

    @BeforeEach
    void setUp() {
        lookupCacheService = new LookupCacheService(5);
        service = new EquivalenciaLookupService(equivalenciaRepo, MAX_BATCH, lookupCacheService);
    }

    // -------------------------------------------------------------------------
    // Happy path: batch con múltiples módulos/tablas → matches correctos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("lookup batch 3 items → mappings correctos, notFound vacío")
    void lookup_batchValido_retornaMappingsCorrectosNotFoundVacio() {
        String idProceso = UUID.randomUUID().toString();

        // GIVEN — repo responde con equivalencias para los IDs solicitados
        when(equivalenciaRepo.buscarPorClave(idProceso, "PRODUCTS", "producto", "42"))
            .thenReturn(Optional.of(equivalencia(idProceso, "PRODUCTS", "producto", "42", "1042")));
        when(equivalenciaRepo.buscarPorClave(idProceso, "PRODUCTS", "producto", "43"))
            .thenReturn(Optional.of(equivalencia(idProceso, "PRODUCTS", "producto", "43", "1043")));
        when(equivalenciaRepo.buscarPorClave(idProceso, "CATALOGUE", "cuenta", "10"))
            .thenReturn(Optional.of(equivalencia(idProceso, "CATALOGUE", "cuenta", "10", "2010")));

        EquivalenciaLookupRequest request = new EquivalenciaLookupRequest(
            List.of(
                new EquivalenciaLookupRequest.LookupItem("PRODUCTS", "producto", List.of(42L, 43L)),
                new EquivalenciaLookupRequest.LookupItem("CATALOGUE", "cuenta", List.of(10L))
            )
        );

        // WHEN
        EquivalenciaLookupResponse response = service.lookup(idProceso, request);

        // THEN
        assertThat(response.getMappings())
            .containsEntry("PRODUCTS:producto:42", 1042L)
            .containsEntry("PRODUCTS:producto:43", 1043L)
            .containsEntry("CATALOGUE:cuenta:10", 2010L);
        assertThat(response.getNotFound()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // IDs inexistentes → notFound contiene los IDs ausentes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("lookup con IDs no encontrados → notFound contiene los ausentes, mappings parciales")
    void lookup_idsNoEncontrados_notFoundContieneLosAusentes() {
        String idProceso = UUID.randomUUID().toString();

        // GIVEN — ID 42 existe, ID 99 no existe
        when(equivalenciaRepo.buscarPorClave(idProceso, "PRODUCTS", "producto", "42"))
            .thenReturn(Optional.of(equivalencia(idProceso, "PRODUCTS", "producto", "42", "1042")));
        when(equivalenciaRepo.buscarPorClave(idProceso, "PRODUCTS", "producto", "99"))
            .thenReturn(Optional.empty());

        EquivalenciaLookupRequest request = new EquivalenciaLookupRequest(
            List.of(
                new EquivalenciaLookupRequest.LookupItem("PRODUCTS", "producto", List.of(42L, 99L))
            )
        );

        // WHEN
        EquivalenciaLookupResponse response = service.lookup(idProceso, request);

        // THEN
        assertThat(response.getMappings()).containsOnlyKeys("PRODUCTS:producto:42");
        assertThat(response.getMappings().get("PRODUCTS:producto:42")).isEqualTo(1042L);

        assertThat(response.getNotFound()).hasSize(1);
        EquivalenciaLookupResponse.NotFoundItem notFound = response.getNotFound().get(0);
        assertThat(notFound.getModulo()).isEqualTo("PRODUCTS");
        assertThat(notFound.getTabla()).isEqualTo("producto");
        assertThat(notFound.getIdViejo()).isEqualTo(99L);
    }

    // -------------------------------------------------------------------------
    // Límite de 1000 IDs totales (REQ-LOOKUP-01)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("lookup con 1001 IDs totales → lanza IllegalArgumentException")
    void lookup_totalIdsExcedeBatchLimit_lanzaIllegalArgumentException() {
        String idProceso = UUID.randomUUID().toString();

        // GIVEN — 1001 IDs en un solo LookupItem
        List<Long> ids = new java.util.ArrayList<>();
        for (long i = 1; i <= 1001; i++) ids.add(i);

        EquivalenciaLookupRequest request = new EquivalenciaLookupRequest(
            List.of(new EquivalenciaLookupRequest.LookupItem("PRODUCTS", "producto", ids))
        );

        // WHEN / THEN
        assertThatThrownBy(() -> service.lookup(idProceso, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("1000");

        verifyNoInteractions(equivalenciaRepo);
    }

    // -------------------------------------------------------------------------
    // Cache: segunda llamada al mismo proceso → repo consultado 1 sola vez
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("lookup mismo proceso dos veces → equivalenciaRepo consultado solo en primera llamada (cache HIT)")
    void lookup_llamadaRepetidaMismoProceso_cacheHit() {
        String idProceso = UUID.randomUUID().toString();

        when(equivalenciaRepo.buscarPorClave(idProceso, "PRODUCTS", "producto", "5"))
            .thenReturn(Optional.of(equivalencia(idProceso, "PRODUCTS", "producto", "5", "1005")));

        EquivalenciaLookupRequest request = new EquivalenciaLookupRequest(
            List.of(new EquivalenciaLookupRequest.LookupItem("PRODUCTS", "producto", List.of(5L)))
        );

        // WHEN — primera llamada puebla el cache
        EquivalenciaLookupResponse first = service.lookup(idProceso, request);
        // Segunda llamada — misma clave, mismo idProceso
        EquivalenciaLookupResponse second = service.lookup(idProceso, request);

        // THEN — repo llamado exactamente 1 vez (segunda llamada usa cache)
        verify(equivalenciaRepo, times(1))
            .buscarPorClave(idProceso, "PRODUCTS", "producto", "5");
        assertThat(first.getMappings()).isEqualTo(second.getMappings());
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private CopyEquivalenceId equivalencia(String idProceso, String modulo, String tabla,
                                            String idViejo, String idNuevo) {
        return new CopyEquivalenceId(
            UUID.randomUUID().toString(),
            idProceso, modulo, tabla, idViejo, idNuevo,
            LocalDateTime.now()
        );
    }
}
