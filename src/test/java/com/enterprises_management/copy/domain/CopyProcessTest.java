package com.enterprises_management.copy.domain;

import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyProcess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas unitarias para el modelo de dominio CopyProcess.
 * Verifica construcción válida, snapshot y campos obligatorios.
 */
class CopyProcessTest {

    private static final UUID EMPRESA_ORIGEN = UUID.randomUUID();
    private static final String INICIADO_POR = "user-sub-uuid-123";

    @Test
    @DisplayName("crear() debe generar un proceso con UUID, estado PENDIENTE y snapshotCorte asignado")
    void construccionValidaDebeCrearProceso() {
        LocalDateTime antes = LocalDateTime.now();

        CopyProcess proceso = CopyProcess.crear(
            CopyProcessType.DUPLICATE,
            EMPRESA_ORIGEN,
            "Empresa Destino S.A.S",
            null,
            INICIADO_POR
        );

        LocalDateTime despues = LocalDateTime.now();

        assertThat(proceso).isNotNull();
        assertThat(proceso.getId()).isNotNull();
        assertThat(proceso.getEstado()).isEqualTo(ProcessState.PENDIENTE);
        assertThat(proceso.getSnapshotCorte()).isNotNull();
        assertThat(proceso.getSnapshotCorte()).isAfterOrEqualTo(antes);
        assertThat(proceso.getSnapshotCorte()).isBeforeOrEqualTo(despues);
    }

    @Test
    @DisplayName("iniciadoPor no puede ser nulo en crear()")
    void iniciadoPorNoPuedeSerNulo() {
        assertThatThrownBy(() ->
            CopyProcess.crear(
                CopyProcessType.DUPLICATE,
                EMPRESA_ORIGEN,
                "Empresa Destino",
                null,
                null  // iniciado_por nulo
            )
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("cada invocación a crear() genera un ID diferente")
    void cadaProcesoTieneUUIDUnico() {
        CopyProcess p1 = CopyProcess.crear(CopyProcessType.BACKUP, EMPRESA_ORIGEN, null, null, INICIADO_POR);
        CopyProcess p2 = CopyProcess.crear(CopyProcessType.BACKUP, EMPRESA_ORIGEN, null, null, INICIADO_POR);

        assertThat(p1.getId()).isNotEqualTo(p2.getId());
    }

    @Test
    @DisplayName("snapshotCorte se asigna en el momento de la construcción")
    void snapshotCorteAsignadoEnConstructor() {
        CopyProcess proceso = CopyProcess.crear(
            CopyProcessType.BACKUP, EMPRESA_ORIGEN, null, null, INICIADO_POR
        );

        assertThat(proceso.getSnapshotCorte()).isNotNull();
    }
}
