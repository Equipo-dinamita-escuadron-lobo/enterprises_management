package com.enterprises_management.copy.infraestructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Bootstrap inicial de la configuración de fases (REQ-CFG-02, REQ-BOOTSTRAP-01).
 *
 * <p>Inserta las filas de configuración de módulos en {@code copy_phase_config}
 * si no existen todavía. Idempotente: re-ejecutar no duplica filas.
 *
 * <p>Fase 1 (siempre activa): ENTERPRISES(orden=1), CATALOGUE(orden=2), PRODUCTS(orden=3).
 * <p>Fase 2 (Hito 3 — ADR-27): PRODUCTS(orden=1) y THIRDS(orden=2) condicionados
 * por feature flags {@code app.copy.orchestrator.participants.enabled.products/thirds}.
 *
 * <p>Solo corre cuando {@code app.copy.orchestrator.bootstrap-config=true}.
 */
@Component
@ConditionalOnProperty(name = "app.copy.orchestrator.bootstrap-config", havingValue = "true")
public class PhaseConfigBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PhaseConfigBootstrap.class);

    /**
     * Configuración fija de Fase 1 (módulos stub base).
     * Formato: {modulo, numeroFase, orden}
     */
    private static final Object[][] CONFIGURACION_FASE_1 = {
        { "ENTERPRISES", 1, 1 },
        { "CATALOGUE",   1, 2 },
        { "PRODUCTS",    1, 3 },
    };

    @Value("${app.copy.orchestrator.participants.enabled.products:false}")
    private boolean productsHabilitado;

    @Value("${app.copy.orchestrator.participants.enabled.thirds:false}")
    private boolean thirdsHabilitado;

    @Value("${app.copy.orchestrator.participants.enabled.treasury:false}")
    private boolean treasuryHabilitado;

    @Value("${app.copy.orchestrator.participants.enabled.stock:false}")
    private boolean stockHabilitado;

    @Value("${app.copy.orchestrator.participants.enabled.kardex:false}")
    private boolean kardexHabilitado;

    @Value("${app.copy.orchestrator.participants.enabled.factures:false}")
    private boolean facturesHabilitado;

    @Value("${app.copy.orchestrator.participants.enabled.inventorypeps:false}")
    private boolean inventoryPepsHabilitado;

    @Value("${app.copy.orchestrator.participants.enabled.auxiliarybook:false}")
    private boolean auxiliaryBookHabilitado;

    private final JdbcTemplate jdbcTemplate;

    public PhaseConfigBootstrap(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    void setProductsHabilitado(boolean productsHabilitado) {
        this.productsHabilitado = productsHabilitado;
    }

    void setThirdsHabilitado(boolean thirdsHabilitado) {
        this.thirdsHabilitado = thirdsHabilitado;
    }

    void setTreasuryHabilitado(boolean treasuryHabilitado) {
        this.treasuryHabilitado = treasuryHabilitado;
    }

    void setStockHabilitado(boolean stockHabilitado) {
        this.stockHabilitado = stockHabilitado;
    }

    void setKardexHabilitado(boolean kardexHabilitado) {
        this.kardexHabilitado = kardexHabilitado;
    }

    void setFacturesHabilitado(boolean facturesHabilitado) {
        this.facturesHabilitado = facturesHabilitado;
    }

    void setInventoryPepsHabilitado(boolean inventoryPepsHabilitado) {
        this.inventoryPepsHabilitado = inventoryPepsHabilitado;
    }

    void setAuxiliaryBookHabilitado(boolean auxiliaryBookHabilitado) {
        this.auxiliaryBookHabilitado = auxiliaryBookHabilitado;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Iniciando bootstrap de configuración de fases (PhaseConfigBootstrap)");
        int insertadas = 0;

        // Fase 1: módulos base siempre presentes
        for (Object[] config : CONFIGURACION_FASE_1) {
            String modulo = (String) config[0];
            int numeroFase = (int) config[1];
            int orden = (int) config[2];
            insertadas += insertarSiNoExiste(modulo, numeroFase, orden);
        }

        // Fase 2: participantes reales (PRODUCTS y THIRDS) — condicionados por feature flags
        if (productsHabilitado) {
            insertadas += insertarSiNoExiste("PRODUCTS", 2, 1);
        } else {
            log.debug("Feature flag products=false — PRODUCTS(fase=2) no registrado");
        }

        if (thirdsHabilitado) {
            insertadas += insertarSiNoExiste("THIRDS", 2, 2);
        } else {
            log.debug("Feature flag thirds=false — THIRDS(fase=2) no registrado");
        }

        // Fase 3: participantes transaccionales (Hito 4 ADR-41)
        if (treasuryHabilitado) {
            insertadas += insertarSiNoExiste("TREASURY", 3, 1);
        }
        if (stockHabilitado) {
            insertadas += insertarSiNoExiste("STOCK", 3, 2);
        }
        if (kardexHabilitado) {
            insertadas += insertarSiNoExiste("KARDEX", 3, 3);
        }
        if (facturesHabilitado) {
            insertadas += insertarSiNoExiste("FACTURES", 3, 4);
        }
        if (inventoryPepsHabilitado) {
            insertadas += insertarSiNoExiste("INVENTORYPEPS", 3, 5);
        }
        if (auxiliaryBookHabilitado) {
            insertadas += insertarSiNoExiste("AUXILIARY-BOOK", 3, 6);
        }

        log.info("Bootstrap de configuración completado: {} filas insertadas", insertadas);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Inserta la configuración si no existe todavía (idempotente).
     *
     * @return 1 si se insertó, 0 si ya existía
     */
    private int insertarSiNoExiste(String modulo, int numeroFase, int orden) {
        if (!existeConfiguracion(modulo, numeroFase)) {
            insertarConfiguracion(modulo, numeroFase, orden);
            log.debug("Configuración insertada: módulo={} fase={} orden={}", modulo, numeroFase, orden);
            return 1;
        } else {
            log.debug("Configuración ya existe: módulo={} fase={} — se omite", modulo, numeroFase);
            return 0;
        }
    }

    private boolean existeConfiguracion(String modulo, int numeroFase) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM copy_phase_config WHERE modulo = ? AND numero_fase = ?",
            Integer.class,
            modulo,
            numeroFase
        );
        return count != null && count > 0;
    }

    private void insertarConfiguracion(String modulo, int numeroFase, int orden) {
        jdbcTemplate.update(
            "INSERT INTO copy_phase_config (id, numero_fase, modulo, orden, activo, parametros_json) " +
            "VALUES (?, ?, ?, ?, ?, ?)",
            java.util.UUID.randomUUID().toString(),
            numeroFase,
            modulo,
            orden,
            true,
            null
        );
    }
}
