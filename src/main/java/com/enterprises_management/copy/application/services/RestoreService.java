package com.enterprises_management.copy.application.services;

import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.ICopyRestoreInputPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.input.command.RestoreCommand;
import com.enterprises_management.copy.application.output.IBackupReaderPort;
import com.enterprises_management.copy.application.output.IEquivalenceRepositoryPort;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.exceptions.BackupCorruptedException;
import com.enterprises_management.copy.domain.models.BackupManifest;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import com.enterprises_management.copy.domain.models.CopyProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Servicio de aplicación que orquesta la creación de un proceso RESTORE desde un backup.
 * Implementa ICopyRestoreInputPort (REQ-RESTORE-01, REQ-RESTORE-02, REQ-RESTORE-03, ADR-46).
 */
public class RestoreService implements ICopyRestoreInputPort {

    private static final Logger log = LoggerFactory.getLogger(RestoreService.class);

    private final IBackupReaderPort backupReader;
    private final ICopyProcessStartPort startPort;
    private final IEquivalenceRepositoryPort equivalenciaRepo;

    public RestoreService(
            IBackupReaderPort backupReader,
            ICopyProcessStartPort startPort,
            IEquivalenceRepositoryPort equivalenciaRepo
    ) {
        this.backupReader = backupReader;
        this.startPort = startPort;
        this.equivalenciaRepo = equivalenciaRepo;
    }

    @Override
    public CopyProcess iniciarRestore(RestoreCommand command) {
        // 1. Leer y deserializar el manifest del ZIP
        BackupManifest manifest = backupReader.leerManifest(command.backupRef());

        // 2. Validar manifest y comando (REQ-RESTORE-03)
        validar(manifest, command);

        // 3. Leer equivalencias del ZIP (con idProceso original — se re-keyean en el paso 5)
        List<CopyEquivalenceId> equivalenciasOriginales = backupReader.leerEquivalencias(command.backupRef());

        // 4. Crear el proceso RESTORE via el servicio de inicio (reutiliza invariantes: unicidad, 4 fases, evento)
        IniciarProcesoCommand iniciarCmd = new IniciarProcesoCommand(
                CopyProcessType.RESTORE,
                manifest.empresaOrigen(),
                command.empresaDestino(),
                command.backupRef(),
                command.iniciadoPor(),
                false
        );
        CopyProcess nuevoProceso = startPort.iniciar(iniciarCmd);

        // 5. Resembrar equivalencias re-keyeadas bajo el NUEVO idProceso (ADR-46)
        int total = 0;
        for (CopyEquivalenceId eq : equivalenciasOriginales) {
            try {
                CopyEquivalenceId rekeyed = CopyEquivalenceId.crear(
                        nuevoProceso.getId(),
                        eq.getModulo(),
                        eq.getTabla(),
                        eq.getIdViejo(),
                        eq.getIdNuevo()
                );
                equivalenciaRepo.guardar(rekeyed);
                total++;
            } catch (Exception ex) {
                log.warn("Restore {}: error al resembrar equivalencia ({}/{}/{}): {}",
                        nuevoProceso.getId(), eq.getModulo(), eq.getTabla(), eq.getIdViejo(), ex.getMessage());
            }
        }
        log.info("Restore {}: {} equivalencias resembradas desde backup {}",
                nuevoProceso.getId(), total, command.backupRef());

        return nuevoProceso;
    }

    /**
     * Valida el manifest y el comando antes de crear el proceso (REQ-RESTORE-03).
     *
     * @param manifest manifest leído del ZIP
     * @param command  comando de restore
     * @throws BackupCorruptedException si la versión del backup no es soportada
     * @throws IllegalArgumentException si empresaDestino viola REQ-RESTORE-03
     */
    private void validar(BackupManifest manifest, RestoreCommand command) {
        if (!"1.0".equals(manifest.version())) {
            throw new BackupCorruptedException("versión de backup no soportada: " + manifest.version());
        }

        if (command.empresaDestino() == null || command.empresaDestino().isBlank()) {
            throw new IllegalArgumentException("empresaDestino no puede ser nula o vacía");
        }

        if (command.iniciadoPor() == null || command.iniciadoPor().isBlank()) {
            throw new IllegalArgumentException("iniciadoPor no puede ser nulo o vacío");
        }

        // REQ-RESTORE-03: estas restricciones no aplican en modo inplace (H9)
        // porque la empresa origen ya fue eliminada y se creó una nueva con UUID distinto
        if (!command.inplace()) {
            if (manifest.empresaOrigen() != null &&
                    manifest.empresaOrigen().toString().equals(command.empresaDestino())) {
                throw new IllegalArgumentException("RESTORE a la empresa origen no está soportado en v1");
            }
            if (manifest.empresaDestinoOriginal() != null &&
                    manifest.empresaDestinoOriginal().equals(command.empresaDestino())) {
                throw new IllegalArgumentException(
                        "La empresa destino no puede ser la misma que la empresa destino original del backup");
            }
        }
    }
}
