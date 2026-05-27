package com.enterprises_management.copy.infraestructure.adapters.output.backup;

import com.enterprises_management.copy.application.output.IBackupSerializerPort;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.models.BackupManifest;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.infraestructure.config.CopyOrchestratorProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Adaptador de salida que serializa el estado de un proceso completado en un ZIP (ADR-44, ADR-45).
 * Escribe tres entradas: manifest.json, equivalencias.json, phases.json.
 */
public class ZipBackupSerializerAdapter implements IBackupSerializerPort {

    private static final Logger log = LoggerFactory.getLogger(ZipBackupSerializerAdapter.class);
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final CopyOrchestratorProperties props;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbc;

    public ZipBackupSerializerAdapter(CopyOrchestratorProperties props, ObjectMapper objectMapper, JdbcTemplate jdbc) {
        this.props = props;
        this.jdbc = jdbc;
        // Clonar el ObjectMapper para no mutar el bean compartido de Spring
        this.objectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Crea el directorio de backups al arrancar si no existe.
     */
    @PostConstruct
    public void inicializarDirectorio() {
        try {
            Files.createDirectories(Path.of(props.getBackup().getDir()));
        } catch (IOException ex) {
            throw new RuntimeException(
                    "No se pudo crear el directorio de backups: " + props.getBackup().getDir(), ex);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * El nombre del archivo sigue el patrón:
     * {@code backup_{entIdOrigen}_{idProceso}_{yyyyMMdd-HHmmss}.zip}
     */
    @Override
    public String serializarBackup(CopyProcess proceso, List<CopyEquivalenceId> equivalencias,
                                   List<CopyPhase> fases, Map<String, Object> datosModulos) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
        String filename = "backup_" + proceso.getEmpresaOrigen()
                + "_" + proceso.getId()
                + "_" + timestamp + ".zip";

        Path rutaCompleta = Path.of(props.getBackup().getDir()).resolve(filename);

        try (ZipOutputStream zos = new ZipOutputStream(
                Files.newOutputStream(rutaCompleta, StandardOpenOption.CREATE_NEW))) {

            // Entrada 1 — manifest.json
            BackupManifest manifest = new BackupManifest(
                    "1.0",
                    proceso.getId(),
                    proceso.getTipo(),
                    proceso.getEmpresaOrigen(),
                    proceso.getEmpresaDestino(),
                    proceso.getIniciadoPor(),
                    proceso.getSnapshotCorte(),
                    proceso.getFinalizadoEn(),
                    filename
            );
            escribirEntrada(zos, "manifest.json", objectMapper.writeValueAsBytes(manifest));

            // Entrada 2 — equivalencias.json
            escribirEntrada(zos, "equivalencias.json", objectMapper.writeValueAsBytes(equivalencias));

            // Entrada 3 — phases.json
            escribirEntrada(zos, "phases.json", objectMapper.writeValueAsBytes(fases));

            // Entrada 4 — enterprise.json (snapshot completo de la empresa para importación real)
            // Opcional: si la query falla el ZIP se completa igualmente con las 3 entradas base
            try {
                Map<String, Object> enterpriseSnapshot = fetchEnterpriseSnapshot(proceso.getEmpresaOrigen().toString());
                if (enterpriseSnapshot != null) {
                    escribirEntrada(zos, "enterprise.json", objectMapper.writeValueAsBytes(enterpriseSnapshot));
                }
            } catch (Exception ex) {
                log.warn("[idProceso={}] No se pudo agregar enterprise.json al backup: {}",
                        proceso.getId(), ex.getMessage());
            }

            // Entradas de datos de módulos (BACKUP con datos reales)
            for (Map.Entry<String, Object> entry : datosModulos.entrySet()) {
                try {
                    String entryName = entry.getKey().toLowerCase() + ".json";
                    escribirEntrada(zos, entryName, objectMapper.writeValueAsBytes(entry.getValue()));
                    log.info("[idProceso={}] Módulo '{}' → '{}' agregado al backup",
                            proceso.getId(), entry.getKey(), entryName);
                } catch (Exception ex) {
                    log.warn("[idProceso={}] No se pudo agregar '{}' al backup: {}",
                            proceso.getId(), entry.getKey(), ex.getMessage());
                }
            }

        } catch (IOException ex) {
            throw new RuntimeException("Error al serializar backup para proceso " + proceso.getId(), ex);
        }

        return filename;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Retorna la ruta relativa (nombre del archivo) donde se escribiría el backup.
     * No crea el archivo.
     */
    @Override
    public String resolverRutaBackup(String entIdOrigen, String idProceso) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
        return "backup_" + entIdOrigen + "_" + idProceso + "_" + timestamp + ".zip";
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Map<String, Object> fetchEnterpriseSnapshot(String empresaId) {
        String sql = """
                SELECT e.id::text, e.id_user, e.name, e.nit, e.dv, e.phone, e.branch, e.email, e.logo,
                       e.state, e.main_activity, e.secondary_activity, e.inventory_methods,
                       e.tax_payer_type_id, e.enterprise_type_id, e.tenant_id,
                       pt.type AS pt_type, pt.name AS pt_name, pt.surname AS pt_surname,
                       pt.bussiness_name AS pt_bussiness_name,
                       l.address AS l_address, l.city_id AS l_city_id,
                       l.department_id AS l_department_id, l.country_id AS l_country_id
                FROM enterprise e
                LEFT JOIN person_type pt ON pt.id = e.person_type_id
                LEFT JOIN location l ON l.id = e.location_id
                WHERE e.id = CAST(? AS uuid)
                """;
        return jdbc.query(sql, rs -> {
            if (!rs.next()) return null;
            Map<String, Object> snap = new LinkedHashMap<>();
            snap.put("id", rs.getString("id"));
            snap.put("id_user", rs.getString("id_user"));
            snap.put("name", rs.getString("name"));
            snap.put("nit", rs.getString("nit"));
            snap.put("dv", rs.getString("dv"));
            snap.put("phone", rs.getString("phone"));
            snap.put("branch", rs.getString("branch"));
            snap.put("email", rs.getString("email"));
            snap.put("logo", rs.getString("logo"));
            snap.put("state", rs.getObject("state"));
            snap.put("main_activity", rs.getObject("main_activity"));
            snap.put("secondary_activity", rs.getObject("secondary_activity"));
            snap.put("inventory_methods", rs.getString("inventory_methods"));
            snap.put("tax_payer_type_id", rs.getObject("tax_payer_type_id"));
            snap.put("enterprise_type_id", rs.getObject("enterprise_type_id"));
            snap.put("tenant_id", rs.getString("tenant_id"));

            Map<String, Object> personType = new LinkedHashMap<>();
            personType.put("type", rs.getString("pt_type"));
            personType.put("name", rs.getString("pt_name"));
            personType.put("surname", rs.getString("pt_surname"));
            personType.put("bussiness_name", rs.getString("pt_bussiness_name"));
            snap.put("person_type", personType);

            Map<String, Object> location = new LinkedHashMap<>();
            location.put("address", rs.getString("l_address"));
            location.put("city_id", rs.getObject("l_city_id"));
            location.put("department_id", rs.getObject("l_department_id"));
            location.put("country_id", rs.getObject("l_country_id"));
            snap.put("location", location);

            // tax liabilities — ElementCollection en enterprise_tax_liabilities
            List<Long> taxLiabilities = jdbc.queryForList(
                    "SELECT tax_liability_id FROM enterprise_tax_liabilities WHERE enterprise_id = CAST(? AS uuid)",
                    Long.class,
                    empresaId
            );
            snap.put("taxLiabilities", taxLiabilities);

            // subject associations — ManyToMany en enterprise_subject
            List<String> subjectIds = jdbc.queryForList(
                    "SELECT subject_id::text FROM enterprise_subject WHERE enterprise_id = CAST(? AS uuid)",
                    String.class,
                    empresaId
            );
            snap.put("subjectIds", subjectIds);

            return snap;
        }, empresaId);
    }

    private void escribirEntrada(ZipOutputStream zos, String nombre, byte[] contenido) throws IOException {
        zos.putNextEntry(new ZipEntry(nombre));
        zos.write(contenido);
        zos.closeEntry();
    }
}
