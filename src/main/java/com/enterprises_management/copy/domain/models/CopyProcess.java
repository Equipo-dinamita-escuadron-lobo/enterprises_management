package com.enterprises_management.copy.domain.models;

import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ProcessState;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de dominio del proceso raíz de la saga de copia.
 * POJO sin dependencias de Spring. El ID se genera en el dominio
 * con UUID.randomUUID() (ADR-3).
 */
public class CopyProcess {

    private final String id;
    private final CopyProcessType tipo;
    private ProcessState estado;
    private final UUID empresaOrigen;
    private String empresaDestino;
    private String backupRef;
    private final LocalDateTime snapshotCorte;
    private final String iniciadoPor;
    private int faseActual;
    private LocalDateTime finalizadoEn;
    private String errorResumen;
    private long versionProceso;
    /**
     * Indica si se debe generar un ZIP de backup al completar el proceso.
     * Solo aplica para tipo DUPLICATE — los BACKUP siempre generan backup (ADR-44, REQ-BACKUP-02).
     */
    private boolean generateBackup = false;

    /**
     * Token Bearer del request HTTP que inició el proceso.
     * Transient — NO persiste en BD (ADR-29).
     * Se usa para propagar el Authorization header a los participantes.
     */
    private transient String bearerToken;

    private CopyProcess(
            String id,
            CopyProcessType tipo,
            UUID empresaOrigen,
            String empresaDestino,
            String backupRef,
            LocalDateTime snapshotCorte,
            String iniciadoPor
    ) {
        this.id = id;
        this.tipo = tipo;
        this.empresaOrigen = empresaOrigen;
        this.empresaDestino = empresaDestino;
        this.backupRef = backupRef;
        this.snapshotCorte = snapshotCorte;
        this.iniciadoPor = iniciadoPor;
        this.estado = ProcessState.PENDIENTE;
        this.faseActual = 0;
        this.versionProceso = 0;
    }

    /**
     * Factory method que crea un proceso nuevo con ID único y estado PENDIENTE.
     *
     * @param tipo           tipo de proceso (BACKUP, RESTORE, DUPLICATE)
     * @param empresaOrigen  UUID de la empresa origen
     * @param empresaDestino nombre de la empresa destino (puede ser null para BACKUP)
     * @param backupRef      referencia al backup (requerido para RESTORE)
     * @param iniciadoPor    claim 'sub' del JWT del usuario que inicia el proceso
     * @return nuevo proceso en estado PENDIENTE
     * @throws IllegalArgumentException si iniciadoPor es null
     */
    public static CopyProcess crear(
            CopyProcessType tipo,
            UUID empresaOrigen,
            String empresaDestino,
            String backupRef,
            String iniciadoPor
    ) {
        if (iniciadoPor == null || iniciadoPor.isBlank()) {
            throw new IllegalArgumentException("iniciadoPor no puede ser nulo o vacío");
        }
        return new CopyProcess(
            UUID.randomUUID().toString(),
            tipo,
            empresaOrigen,
            empresaDestino,
            backupRef,
            LocalDateTime.now(),
            iniciadoPor
        );
    }

    /**
     * Factory method para reconstituir un proceso desde persistencia.
     * Usa el ID existente en lugar de generar uno nuevo (a diferencia de crear()).
     * Solo debe llamarse desde adaptadores de persistencia.
     *
     * @param id            ID existente en la BD
     * @param tipo          tipo de proceso
     * @param empresaOrigen UUID de la empresa origen
     * @param empresaDestino nombre de la empresa destino
     * @param backupRef      referencia al backup
     * @param snapshotCorte  fecha de corte del snapshot
     * @param iniciadoPor    claim sub del JWT
     * @return proceso reconstituido desde persistencia
     */
    public static CopyProcess restaurar(
            String id,
            CopyProcessType tipo,
            UUID empresaOrigen,
            String empresaDestino,
            String backupRef,
            LocalDateTime snapshotCorte,
            String iniciadoPor
    ) {
        return new CopyProcess(id, tipo, empresaOrigen, empresaDestino, backupRef, snapshotCorte, iniciadoPor);
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public String getId() { return id; }

    public CopyProcessType getTipo() { return tipo; }

    public ProcessState getEstado() { return estado; }

    public UUID getEmpresaOrigen() { return empresaOrigen; }

    public String getEmpresaDestino() { return empresaDestino; }

    public String getBackupRef() { return backupRef; }

    public LocalDateTime getSnapshotCorte() { return snapshotCorte; }

    public String getIniciadoPor() { return iniciadoPor; }

    public int getFaseActual() { return faseActual; }

    public LocalDateTime getFinalizadoEn() { return finalizadoEn; }

    public String getErrorResumen() { return errorResumen; }

    public long getVersionProceso() { return versionProceso; }

    // -----------------------------------------------------------------------
    // Setters controlados (sin setters directos de estado — se usan transiciones)
    // -----------------------------------------------------------------------

    public void setEstado(ProcessState estado) { this.estado = estado; }

    public void setEmpresaDestino(String empresaDestino) { this.empresaDestino = empresaDestino; }

    public void setFaseActual(int faseActual) { this.faseActual = faseActual; }

    public void setFinalizadoEn(LocalDateTime finalizadoEn) { this.finalizadoEn = finalizadoEn; }

    public void setErrorResumen(String errorResumen) { this.errorResumen = errorResumen; }

    public void setVersionProceso(long versionProceso) { this.versionProceso = versionProceso; }

    /**
     * Retorna el token Bearer del request entrante (puede ser null).
     * Transient — no persiste en BD (ADR-29).
     */
    public String getBearerToken() { return bearerToken; }

    /**
     * Almacena el token Bearer del request entrante.
     * Debe llamarse desde el controller antes de pasar el proceso al saga (ADR-29).
     *
     * @param bearerToken valor del header Authorization sin el prefijo "Bearer "
     */
    public void setBearerToken(String bearerToken) { this.bearerToken = bearerToken; }

    /**
     * Establece la referencia al archivo ZIP de backup generado tras COMPLETADO.
     * Solo debe ser invocado por SagaEngineService.evaluarCompletacionProceso
     * tras confirmar estado COMPLETADO (ADR-44).
     *
     * @param backupRef ruta relativa al ZIP de backup
     */
    public void setBackupRef(String backupRef) { this.backupRef = backupRef; }

    public boolean isGenerateBackup() { return generateBackup; }

    /**
     * Activa la generación de backup ZIP al completar el proceso.
     * Relevante para tipo DUPLICATE cuando el cliente solicita backup (REQ-BACKUP-02).
     *
     * @param generateBackup true para generar backup al completar
     */
    public void setGenerateBackup(boolean generateBackup) { this.generateBackup = generateBackup; }
}
