package com.enterprises_management.copy.infraestructure.adapters.output.participant.local;

import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.application.output.IParticipantClientPort;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyModuleExecution;
import com.enterprises_management.copy.domain.models.CopyProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para LocalValidationParticipantAdapter (Hito 8 — fase CIERRE).
 */
class LocalValidationParticipantAdapterTest {

    private IEquivalenceRepositoryPort equivalenciaRepo;
    private LocalValidationParticipantAdapter adapter;

    @BeforeEach
    void setUp() {
        equivalenciaRepo = mock(IEquivalenceRepositoryPort.class);
        adapter = new LocalValidationParticipantAdapter(equivalenciaRepo);
    }

    // -------------------------------------------------------------------------
    // getNombreModulo
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getNombreModulo retorna VALIDACION")
    void getNombreModulo_retornaValidacion() {
        assertThat(adapter.getNombreModulo()).isEqualTo("VALIDACION");
    }

    // -------------------------------------------------------------------------
    // invoke — sin equivalencias
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("sin equivalencias retorna exitoso con advertencia descriptiva")
    void invoke_sinEquivalencias_retornaExitosoConAdvertencia() {
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "user-test");

        when(equivalenciaRepo.buscarConFiltros(proceso.getId(), null, null, null))
                .thenReturn(List.of());

        CopyModuleExecution ejecucion = CopyModuleExecution.crear(proceso.getId(), UUID.randomUUID().toString(), "VALIDACION");

        IParticipantClientPort.ParticipantResult resultado = adapter.invoke(proceso, ejecucion);

        assertThat(resultado.exitoso()).isTrue();
        assertThat(resultado.conAdvertencias()).isTrue();
        assertThat(resultado.errorDetalle()).isNotBlank();
        assertThat(resultado.equivalencias()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // invoke — con equivalencias de un solo módulo
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("con equivalencias de un módulo retorna exitoso con advertencia con resumen")
    void invoke_conEquivalenciasUnModulo_retornaExitosoConResumen() {
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "user-test");
        String idProceso = proceso.getId();

        List<CopyEquivalenceId> equivalencias = List.of(
                CopyEquivalenceId.crear(idProceso, "ENTERPRISES", "empresa", "1", "100"),
                CopyEquivalenceId.crear(idProceso, "ENTERPRISES", "empresa", "2", "101")
        );

        when(equivalenciaRepo.buscarConFiltros(idProceso, null, null, null))
                .thenReturn(equivalencias);

        CopyModuleExecution ejecucion = CopyModuleExecution.crear(idProceso, UUID.randomUUID().toString(), "VALIDACION");

        IParticipantClientPort.ParticipantResult resultado = adapter.invoke(proceso, ejecucion);

        assertThat(resultado.exitoso()).isTrue();
        assertThat(resultado.conAdvertencias()).isTrue();
        assertThat(resultado.errorDetalle()).contains("ENTERPRISES");
        assertThat(resultado.errorDetalle()).contains("2");
        assertThat(resultado.equivalencias()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // invoke — con equivalencias de múltiples módulos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("con equivalencias de múltiples módulos el resumen incluye todos los módulos")
    void invoke_conMultiplesModulos_resumenContieneTodasLasClaves() {
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "user-test");
        String idProceso = proceso.getId();

        List<CopyEquivalenceId> equivalencias = List.of(
                CopyEquivalenceId.crear(idProceso, "ENTERPRISES", "empresa",   "1", "100"),
                CopyEquivalenceId.crear(idProceso, "CATALOGUE",   "cuenta",    "1", "200"),
                CopyEquivalenceId.crear(idProceso, "CATALOGUE",   "cuenta",    "2", "201"),
                CopyEquivalenceId.crear(idProceso, "PRODUCTS",    "producto",  "1", "300"),
                CopyEquivalenceId.crear(idProceso, "PRODUCTS",    "producto",  "2", "301"),
                CopyEquivalenceId.crear(idProceso, "PRODUCTS",    "producto",  "3", "302")
        );

        when(equivalenciaRepo.buscarConFiltros(idProceso, null, null, null))
                .thenReturn(equivalencias);

        CopyModuleExecution ejecucion = CopyModuleExecution.crear(idProceso, UUID.randomUUID().toString(), "VALIDACION");

        IParticipantClientPort.ParticipantResult resultado = adapter.invoke(proceso, ejecucion);

        assertThat(resultado.exitoso()).isTrue();
        String detalle = resultado.errorDetalle();
        assertThat(detalle).contains("ENTERPRISES");
        assertThat(detalle).contains("CATALOGUE");
        assertThat(detalle).contains("PRODUCTS");
        assertThat(detalle).contains("6");  // total
    }

    // -------------------------------------------------------------------------
    // invoke — consulta correcta al repositorio
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("invoke consulta equivalencias usando el ID del proceso sin otros filtros")
    void invoke_consultaRepositorioConIdProcesoYNulos() {
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.BACKUP, UUID.randomUUID(), "Destino", null, "user-test");
        when(equivalenciaRepo.buscarConFiltros(proceso.getId(), null, null, null))
                .thenReturn(List.of());

        CopyModuleExecution ejecucion = CopyModuleExecution.crear(proceso.getId(), UUID.randomUUID().toString(), "VALIDACION");

        adapter.invoke(proceso, ejecucion);

        verify(equivalenciaRepo, times(1)).buscarConFiltros(proceso.getId(), null, null, null);
        verifyNoMoreInteractions(equivalenciaRepo);
    }

    // -------------------------------------------------------------------------
    // invoke — resultado nunca contiene equivalencias propias
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("invoke no genera equivalencias propias (VALIDACION no copia datos)")
    void invoke_noGeneraEquivalenciasPropias() {
        CopyProcess proceso = CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "user-test");
        when(equivalenciaRepo.buscarConFiltros(anyString(), any(), any(), any()))
                .thenReturn(List.of(
                        CopyEquivalenceId.crear(proceso.getId(), "ENTERPRISES", "empresa", "1", "100")
                ));

        CopyModuleExecution ejecucion = CopyModuleExecution.crear(proceso.getId(), UUID.randomUUID().toString(), "VALIDACION");

        IParticipantClientPort.ParticipantResult resultado = adapter.invoke(proceso, ejecucion);

        assertThat(resultado.equivalencias()).isEmpty();
    }
}
