package com.enterprises_management.copy.application;

import com.enterprises_management.copy.application.output.ICopyProcessRepositoryPort;
import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.application.services.EquivalenceRegistryService;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException;
import com.enterprises_management.copy.domain.exceptions.EquivalenceConflictException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para EquivalenceRegistryService (REQ-EQ-01, ADR-9).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EquivalenceRegistryService — registrar equivalencias")
class EquivalenceRegistryServiceTest {

    @Mock
    private ICopyProcessRepositoryPort procesoRepo;

    @Mock
    private IEquivalenceRepositoryPort equivalenciaRepo;

    private EquivalenceRegistryService sut;

    @BeforeEach
    void setUp() {
        sut = new EquivalenceRegistryService(procesoRepo, equivalenciaRepo);
    }

    // -------------------------------------------------------------------------
    // REQ-EQ-01: Registro exitoso (nueva equivalencia)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("registrar — inserta equivalencias nuevas y retorna el conteo")
    void registrar_insertaEquivalenciasNuevas() {
        // GIVEN
        String idProceso = UUID.randomUUID().toString();
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "sub");
        CopyEquivalenceId eq1 = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "1042");
        CopyEquivalenceId eq2 = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "43", "1043");

        when(procesoRepo.buscarPorId(idProceso)).thenReturn(Optional.of(proceso));
        when(equivalenciaRepo.buscarPorClave(eq(idProceso), eq("PRODUCTS"), eq("producto"), any()))
                .thenReturn(Optional.empty());
        when(equivalenciaRepo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        int resultado = sut.registrar(List.of(eq1, eq2));

        // THEN
        assertThat(resultado).isEqualTo(2);
        verify(equivalenciaRepo, times(2)).guardar(any());
    }

    // -------------------------------------------------------------------------
    // REQ-EQ-01 + ADR-9: Idempotencia — mismo idNuevo → no duplica
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("registrar — equivalencia duplicada con mismo idNuevo es idempotente (ADR-9)")
    void registrar_idempotenteSiIdNuevoIgual() {
        // GIVEN
        String idProceso = UUID.randomUUID().toString();
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "sub");
        CopyEquivalenceId eq = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "1042");
        CopyEquivalenceId existente = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "1042");

        when(procesoRepo.buscarPorId(idProceso)).thenReturn(Optional.of(proceso));
        when(equivalenciaRepo.buscarPorClave(idProceso, "PRODUCTS", "producto", "42"))
                .thenReturn(Optional.of(existente));

        // WHEN
        int resultado = sut.registrar(List.of(eq));

        // THEN — no se inserta ni actualiza
        assertThat(resultado).isEqualTo(0);
        verify(equivalenciaRepo, never()).guardar(any());
        verify(equivalenciaRepo, never()).actualizar(any());
    }

    // -------------------------------------------------------------------------
    // ADR-9: Conflicto — mismo idViejo pero diferente idNuevo → 409
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("registrar — lanza EquivalenceConflictException si idNuevo difiere (ADR-9)")
    void registrar_lanzaConflictoSiIdNuevoDifiere() {
        // GIVEN
        String idProceso = UUID.randomUUID().toString();
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "sub");
        CopyEquivalenceId eqNueva = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "9999");
        CopyEquivalenceId existente = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "1042");

        when(procesoRepo.buscarPorId(idProceso)).thenReturn(Optional.of(proceso));
        when(equivalenciaRepo.buscarPorClave(idProceso, "PRODUCTS", "producto", "42"))
                .thenReturn(Optional.of(existente));

        // WHEN / THEN
        assertThatThrownBy(() -> sut.registrar(List.of(eqNueva)))
                .isInstanceOf(EquivalenceConflictException.class);

        verify(equivalenciaRepo, never()).guardar(any());
    }

    // -------------------------------------------------------------------------
    // Proceso no encontrado
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("registrar — lanza CopyProcessNotFoundException si proceso no existe")
    void registrar_lanzaExcepcionSiProcesoNoExiste() {
        // GIVEN
        String idProceso = UUID.randomUUID().toString();
        CopyEquivalenceId eq = CopyEquivalenceId.crear(idProceso, "PRODUCTS", "producto", "42", "1042");
        when(procesoRepo.buscarPorId(idProceso)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> sut.registrar(List.of(eq)))
                .isInstanceOf(CopyProcessNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // Lista vacía
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("registrar — lista vacía retorna 0 sin llamar al repo")
    void registrar_listaVaciaRetornaCero() {
        // WHEN
        int resultado = sut.registrar(List.of());

        // THEN
        assertThat(resultado).isEqualTo(0);
        verify(procesoRepo, never()).buscarPorId(any());
        verify(equivalenciaRepo, never()).guardar(any());
    }
}
