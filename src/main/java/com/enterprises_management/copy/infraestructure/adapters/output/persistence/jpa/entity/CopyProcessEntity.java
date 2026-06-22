package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.entity;

import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ProcessState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA del proceso raíz de la saga de copia.
 * Mapea la tabla copy_process. ADR-2: prefijo copy_.
 * ADR-3: UUID generado en Java, no en BD.
 * ADR-6: @Enumerated(STRING) para estado y tipo.
 * ADR-11: @Version para lock optimista.
 */
@Entity
@Table(name = "copy_process")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyProcessEntity {

    /** ID UUID generado en Java (ADR-3). */
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /** Tipo de proceso. @Enumerated STRING para legibilidad (ADR-6). */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private CopyProcessType tipo;

    /** Estado actual. CHECK constraint en BD; @Enumerated STRING (ADR-6). */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private ProcessState estado;

    @Column(name = "empresa_origen", nullable = false, length = 36)
    private String empresaOrigen;

    @Column(name = "empresa_destino", length = 255)
    private String empresaDestino;

    @Column(name = "backup_ref", length = 255)
    private String backupRef;

    @Column(name = "snapshot_corte", nullable = false)
    private LocalDateTime snapshotCorte;

    @Column(name = "iniciado_por", nullable = false, length = 255)
    private String iniciadoPor;

    @Column(name = "fase_actual")
    private Integer faseActual;

    @Column(name = "finalizado_en")
    private LocalDateTime finalizadoEn;

    @Column(name = "error_resumen", length = 2000)
    private String errorResumen;

    /**
     * Lock optimista. ADR-11: @Version en CopyProcessEntity.
     * Se incrementa automáticamente en cada update por JPA.
     */
    @Version
    @Column(name = "version_proceso", nullable = false)
    private Long versionProceso;
}
