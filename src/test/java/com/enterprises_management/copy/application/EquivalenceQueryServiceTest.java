package com.enterprises_management.copy.application;

import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.application.services.EquivalenceRegistryService;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para consulta de equivalencias (REQ-EQ-02).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EquivalenceRegistryService — consultar equivalencias")
class EquivalenceQueryServiceTest {

    @Mock
    private ICopyProcessRepositoryPort procesoRepo;

    @Mock
    private IEquivalenceRepositoryPort equivalenciaRepo;

    private EquivalenceRegistryService sut;

    @BeforeEach
    void setUp() {
        sut = new EquivalenceRegistryService(procesoRepo, equivalenciaRepo);
    }

    @Test
    @DisplayName("consultar — retorna equivalencias con filtros aplicados (REQ-EQ-02)")
    void consultar_retornaEquivalenciasConFiltros() {
        // GIVEN
        String idProceso = UUID.randomUUID().toString();
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "D", null, "sub");
        CopyEquivalenceId eq = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "1042");

        when(procesoRepo.buscarPorId(idProceso)).thenReturn(Optional.of(proceso));
        when(equivalenciaRepo.buscarConFiltros(idProceso, "PRODUCTS", "producto", "42"))
                .thenReturn(List.of(eq));

        // WHEN
        List<CopyEquivalenceId> resultado = sut.consultar(idProceso, "PRODUCTS", "producto", "42");

        // THEN
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdViejo()).isEqualTo("42");
        assertThat(resultado.get(0).getIdNuevo()).isEqualTo("1042");
    }

    @Test
    @DisplayName("consultar — retorna lista vacía si no hay coincidencias (REQ-EQ-02 escenario 'no encontrada')")
    void consultar_retornaListaVaciaSiNoHayCoincidencias() {
        // GIVEN
        String idProceso = UUID.randomUUID().toString();
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "D", null, "sub");

        when(procesoRepo.buscarPorId(idProceso)).thenReturn(Optional.of(proceso));
        when(equivalenciaRepo.buscarConFiltros(idProceso, null, null, "999"))
                .thenReturn(List.of());

        // WHEN
        List<CopyEquivalenceId> resultado = sut.consultar(idProceso, null, null, "999");

        // THEN
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("consultar — lanza CopyProcessNotFoundException si proceso no existe")
    void consultar_lanzaExcepcionSiProcesoNoExiste() {
        // GIVEN
        when(procesoRepo.buscarPorId("x")).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> sut.consultar("x", null, null, null))
                .isInstanceOf(CopyProcessNotFoundException.class);
    }

    @Test
    @DisplayName("consultar — sin filtros retorna todas las equivalencias del proceso")
    void consultar_sinFiltrosRetornaTodasLasEquivalencias() {
        // GIVEN
        String idProceso = UUID.randomUUID().toString();
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "D", null, "sub");
        List<CopyEquivalenceId> todas = List.of(
                CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "1", "1001"),
                CopyEquivalenceId.crear(idProceso, "CATALOGUE", "cuenta", "5", "1005")
        );

        when(procesoRepo.buscarPorId(idProceso)).thenReturn(Optional.of(proceso));
        when(equivalenciaRepo.buscarConFiltros(idProceso, null, null, null)).thenReturn(todas);

        // WHEN
        List<CopyEquivalenceId> resultado = sut.consultar(idProceso, null, null, null);

        // THEN
        assertThat(resultado).hasSize(2);
    }
}
