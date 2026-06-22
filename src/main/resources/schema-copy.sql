-- =============================================================================
-- Schema del bounded context "copy" — Orquestador de saga (Hito 1)
-- Motor objetivo: PostgreSQL 15+
-- Compatibilidad H2: sí (excepto las líneas marcadas [POSTGRES-ONLY])
-- Convención: todas las tablas llevan prefijo copy_
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. copy_process — proceso raíz de la saga de copia
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS copy_process (
    id                  VARCHAR(36)     NOT NULL,
    tipo                VARCHAR(20)     NOT NULL,   -- BACKUP | RESTORE | DUPLICATE
    estado              VARCHAR(20)     NOT NULL,   -- ProcessState enum
    empresa_origen      VARCHAR(36)     NOT NULL,
    empresa_destino     VARCHAR(255),               -- nullable hasta completar Fase 1
    backup_ref          VARCHAR(255),               -- solo para RESTORE
    snapshot_corte      TIMESTAMP       NOT NULL,
    iniciado_por        VARCHAR(255)    NOT NULL,   -- claim sub del JWT
    fase_actual         INTEGER         DEFAULT 0,
    finalizado_en       TIMESTAMP,
    error_resumen       VARCHAR(2000),
    version_proceso     BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT pk_copy_process PRIMARY KEY (id),
    CONSTRAINT chk_copy_process_estado CHECK (
        estado IN ('PENDIENTE','EN_PROCESO','COMPLETADO','ERROR','CANCELADO')
    ),
    CONSTRAINT chk_copy_process_tipo CHECK (
        tipo IN ('BACKUP','RESTORE','DUPLICATE')
    )
);

-- [POSTGRES-ONLY] Índice único parcial: solo un proceso activo por empresa origen
-- CREATE UNIQUE INDEX IF NOT EXISTS uq_copy_process_active_origin
--     ON copy_process(empresa_origen)
--     WHERE estado IN ('PENDIENTE','EN_PROCESO');

-- ---------------------------------------------------------------------------
-- 2. copy_phase — las 4 fases de la saga para cada proceso
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS copy_phase (
    id          VARCHAR(36)     NOT NULL,
    id_proceso  VARCHAR(36)     NOT NULL,
    numero      INTEGER         NOT NULL,   -- 1..4
    nombre      VARCHAR(50)     NOT NULL,   -- BASE | INTERNAS | EXTERNAS | CIERRE
    estado      VARCHAR(20)     NOT NULL,   -- PhaseState enum
    iniciada_en TIMESTAMP,
    finalizada_en TIMESTAMP,
    error_detalle VARCHAR(2000),
    CONSTRAINT pk_copy_phase PRIMARY KEY (id),
    CONSTRAINT uq_copy_phase_proceso_numero UNIQUE (id_proceso, numero),
    CONSTRAINT fk_copy_phase_proceso FOREIGN KEY (id_proceso)
        REFERENCES copy_process(id) ON DELETE CASCADE,
    CONSTRAINT chk_copy_phase_estado CHECK (
        estado IN ('PENDIENTE','EN_PROCESO','COMPLETADA','ERROR','OMITIDA')
    )
);

-- ---------------------------------------------------------------------------
-- 3. copy_module_execution — ejecuciones de módulo dentro de cada fase
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS copy_module_execution (
    id          VARCHAR(36)     NOT NULL,
    id_proceso  VARCHAR(36)     NOT NULL,
    id_fase     VARCHAR(36)     NOT NULL,
    modulo      VARCHAR(100)    NOT NULL,
    estado      VARCHAR(30)     NOT NULL,   -- ModuleExecutionState enum
    intentos    INTEGER         NOT NULL DEFAULT 0,
    iniciado_en TIMESTAMP,
    finalizado_en TIMESTAMP,
    error_detalle VARCHAR(2000),
    CONSTRAINT pk_copy_module_execution PRIMARY KEY (id),
    CONSTRAINT uq_copy_module_execution UNIQUE (id_proceso, id_fase, modulo),
    CONSTRAINT fk_copy_module_execution_fase FOREIGN KEY (id_fase)
        REFERENCES copy_phase(id) ON DELETE CASCADE,
    CONSTRAINT chk_copy_module_estado CHECK (
        estado IN ('PENDIENTE','EN_EJECUCION','COMPLETADO','COMPLETADO_CON_ADVERTENCIAS',
                   'ERROR_REINTENTABLE','ERROR_NO_REINTENTABLE')
    )
);

-- ---------------------------------------------------------------------------
-- 4. copy_equivalence_id — tabla de equivalencias idViejo → idNuevo
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS copy_equivalence_id (
    id          VARCHAR(36)     NOT NULL,
    id_proceso  VARCHAR(36)     NOT NULL,
    modulo      VARCHAR(100)    NOT NULL,
    tabla       VARCHAR(100)    NOT NULL,
    id_viejo    VARCHAR(80)     NOT NULL,   -- ADR-5: varchar para UUID o bigint
    id_nuevo    VARCHAR(80)     NOT NULL,
    registrado_en TIMESTAMP     NOT NULL,
    CONSTRAINT pk_copy_equivalence_id PRIMARY KEY (id),
    CONSTRAINT uq_copy_equivalence_clave UNIQUE (id_proceso, modulo, tabla, id_viejo),
    CONSTRAINT fk_copy_equivalence_proceso FOREIGN KEY (id_proceso)
        REFERENCES copy_process(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------------
-- 5. copy_phase_config — configuración de módulos por fase (sin hardcodeo)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS copy_phase_config (
    id              VARCHAR(36)     NOT NULL,
    numero_fase     INTEGER         NOT NULL,
    modulo          VARCHAR(100)    NOT NULL,
    orden           INTEGER         NOT NULL DEFAULT 1,
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    parametros_json VARCHAR(2000),
    CONSTRAINT pk_copy_phase_config PRIMARY KEY (id),
    CONSTRAINT uq_copy_phase_config_fase_modulo UNIQUE (numero_fase, modulo)
);

-- ---------------------------------------------------------------------------
-- 6. copy_process_event — log de eventos para trazabilidad y SSE
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS copy_process_event (
    id              BIGSERIAL       NOT NULL,
    id_proceso      VARCHAR(36)     NOT NULL,
    tipo_evento     VARCHAR(60)     NOT NULL,
    ocurrido_en     TIMESTAMP       NOT NULL,
    payload_json    VARCHAR(4000),
    CONSTRAINT pk_copy_process_event PRIMARY KEY (id),
    CONSTRAINT fk_copy_process_event_proceso FOREIGN KEY (id_proceso)
        REFERENCES copy_process(id) ON DELETE CASCADE
);

-- [POSTGRES-ONLY] En PostgreSQL usar BIGSERIAL en lugar de AUTO_INCREMENT:
-- id BIGSERIAL NOT NULL
