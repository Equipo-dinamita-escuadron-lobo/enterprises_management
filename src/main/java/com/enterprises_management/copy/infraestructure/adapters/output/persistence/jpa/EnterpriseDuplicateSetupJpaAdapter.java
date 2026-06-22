package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa;

import com.enterprises_management.copy.application.output.IEnterpriseDuplicateSetupPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Crea la empresa destino en BD antes de iniciar una saga DUPLICATE.
 *
 * <p>El participante ENTERPRISES asume que la empresa destino ya existe
 * al arrancar la Fase 1 (ver CopyEnterprisesParticipantController).
 * Sin esta creación previa, todos los participantes operan sobre un UUID
 * inexistente y retornan 0 equivalencias.
 */
@Component
public class EnterpriseDuplicateSetupJpaAdapter implements IEnterpriseDuplicateSetupPort {

    private static final Logger log = LoggerFactory.getLogger(EnterpriseDuplicateSetupJpaAdapter.class);

    private final JdbcTemplate jdbc;

    public EnterpriseDuplicateSetupJpaAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String crearEmpresaDestino(String empresaOrigenId, String nombreDestino, String tenantId) {
        try {
            Map<String, Object> src = jdbc.queryForMap(
                    "SELECT * FROM enterprise WHERE id = CAST(? AS uuid)", empresaOrigenId);

            Long newPersonTypeId = copiarPersonType(src.get("person_type_id"));
            Long newLocationId   = copiarLocation(src.get("location_id"));

            String newId = UUID.randomUUID().toString();

            jdbc.update(
                    "INSERT INTO enterprise " +
                    "(id, id_user, name, nit, dv, phone, branch, email, logo, " +
                    " state, main_activity, secondary_activity, inventory_methods, " +
                    " tax_payer_type_id, enterprise_type_id, person_type_id, location_id, tenant_id) " +
                    "SELECT CAST(? AS uuid), id_user, ?, nit, dv, phone, branch, email, logo, " +
                    "       state, main_activity, secondary_activity, inventory_methods, " +
                    "       tax_payer_type_id, enterprise_type_id, ?, ?, ? " +
                    "FROM enterprise WHERE id = CAST(? AS uuid)",
                    newId, nombreDestino, newPersonTypeId, newLocationId, tenantId, empresaOrigenId
            );

            copiarTaxLiabilities(empresaOrigenId, newId);
            copiarSubjects(empresaOrigenId, newId);

            log.info("[DUPLICATE] Empresa destino creada: {} ('{}') desde origen {}",
                    newId, nombreDestino, empresaOrigenId);
            return newId;

        } catch (Exception ex) {
            log.error("[DUPLICATE] No se pudo crear empresa destino desde {}: {}", empresaOrigenId, ex.getMessage(), ex);
            return null;
        }
    }

    private Long copiarPersonType(Object srcPersonTypeId) {
        if (srcPersonTypeId == null) return null;
        try {
            return jdbc.queryForObject(
                    "INSERT INTO person_type (type, name, surname, bussiness_name) " +
                    "SELECT type, name, surname, bussiness_name FROM person_type WHERE id = ? RETURNING id",
                    Long.class, srcPersonTypeId
            );
        } catch (Exception ex) {
            log.warn("[DUPLICATE] No se pudo copiar person_type {}: {}", srcPersonTypeId, ex.getMessage());
            return null;
        }
    }

    private Long copiarLocation(Object srcLocationId) {
        if (srcLocationId == null) return null;
        try {
            return jdbc.queryForObject(
                    "INSERT INTO location (address, city_id, department_id, country_id) " +
                    "SELECT address, city_id, department_id, country_id FROM location WHERE id = ? RETURNING id",
                    Long.class, srcLocationId
            );
        } catch (Exception ex) {
            log.warn("[DUPLICATE] No se pudo copiar location {}: {}", srcLocationId, ex.getMessage());
            return null;
        }
    }

    private void copiarTaxLiabilities(String origenId, String destinoId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT tax_liability_id FROM enterprise_tax_liabilities WHERE enterprise_id = CAST(? AS uuid)",
                origenId
        );
        for (Map<String, Object> row : rows) {
            try {
                jdbc.update(
                        "INSERT INTO enterprise_tax_liabilities (enterprise_id, tax_liability_id) VALUES (CAST(? AS uuid), ?)",
                        destinoId, row.get("tax_liability_id")
                );
            } catch (Exception ex) {
                log.warn("[DUPLICATE] No se pudo copiar tax_liability para empresa {}: {}", destinoId, ex.getMessage());
            }
        }
    }

    private void copiarSubjects(String origenId, String destinoId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT subject_id FROM enterprise_subject WHERE enterprise_id = CAST(? AS uuid)",
                origenId
        );
        for (Map<String, Object> row : rows) {
            try {
                jdbc.update(
                        "INSERT INTO enterprise_subject (enterprise_id, subject_id) VALUES (CAST(? AS uuid), CAST(? AS uuid))",
                        destinoId, row.get("subject_id").toString()
                );
            } catch (Exception ex) {
                log.warn("[DUPLICATE] No se pudo copiar subject para empresa {}: {}", destinoId, ex.getMessage());
            }
        }
    }
}
