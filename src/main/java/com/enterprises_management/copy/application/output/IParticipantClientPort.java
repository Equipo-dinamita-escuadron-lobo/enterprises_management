package com.enterprises_management.copy.application.output;

import com.enterprises_management.copy.domain.models.CopyModuleExecution;
import com.enterprises_management.copy.domain.models.CopyProcess;

import java.util.List;

/**
 * Puerto de salida para invocar a los módulos participantes de la saga.
 * Según ADR-12, las llamadas a este puerto DEBEN ocurrir FUERA de @Transactional.
 * El stub configurable implementa este puerto para Hito 1 (ADR-13).
 */
public interface IParticipantClientPort {

    /**
     * Nombre del módulo que este cliente gestiona.
     * Usado por el SagaEngineService para seleccionar el cliente correcto.
     *
     * @return nombre del módulo (ej: "ENTERPRISES", "CATALOGUE", "PRODUCTS")
     */
    String getNombreModulo();

    /**
     * Invoca al módulo participante para que ejecute su parte de la saga.
     * Esta llamada es potencialmente remota o de larga duración — NUNCA llamar
     * dentro de una transacción activa (ADR-12).
     *
     * @param proceso   proceso de copia raíz (para contexto)
     * @param ejecucion ejecución de módulo que se está lanzando
     * @return resultado de la invocación
     */
    ParticipantResult invoke(CopyProcess proceso, CopyModuleExecution ejecucion);

    /**
     * Una equivalencia individual generada por un módulo participante (ADR-13).
     * Representa la relación idViejo → idNuevo para una tabla específica.
     *
     * @param modulo  nombre del módulo que genera la equivalencia
     * @param tabla   tabla de la entidad copiada
     * @param idViejo ID original en la empresa origen
     * @param idNuevo ID nuevo en la empresa destino
     */
    record EquivalenciaResultado(
            String modulo,
            String tabla,
            String idViejo,
            String idNuevo
    ) {}

    /**
     * Resultado devuelto por un módulo participante.
     *
     * @param exitoso         true si el módulo completó correctamente
     * @param conAdvertencias true si completó pero con advertencias
     * @param reintentable    true si el error es reintentable
     * @param errorDetalle    descripción del error (null si exitoso)
     * @param equivalencias   equivalencias de IDs generadas por el módulo (vacío si fallo o ninguna)
     * @param datosExportados datos exportados en modo BACKUP (null si no aplica)
     */
    record ParticipantResult(
            boolean exitoso,
            boolean conAdvertencias,
            boolean reintentable,
            String errorDetalle,
            List<EquivalenciaResultado> equivalencias,
            Object datosExportados
    ) {
        /** Factory: éxito limpio sin equivalencias. */
        public static ParticipantResult exito() {
            return new ParticipantResult(true, false, false, null, List.of(), null);
        }

        /** Factory: éxito con equivalencias generadas por el módulo (ADR-13). */
        public static ParticipantResult exitoConEquivalencias(List<EquivalenciaResultado> equivalencias) {
            return new ParticipantResult(true, false, false, null, equivalencias, null);
        }

        /** Factory: éxito con equivalencias y datos exportados (modo BACKUP). */
        public static ParticipantResult exitoConDatos(List<EquivalenciaResultado> equivalencias, Object datosExportados) {
            return new ParticipantResult(true, false, false, null, equivalencias, datosExportados);
        }

        /** Factory: éxito con advertencias. */
        public static ParticipantResult exitoConAdvertencias(String detalle) {
            return new ParticipantResult(true, true, false, detalle, List.of(), null);
        }

        /** Factory: error reintentable. */
        public static ParticipantResult errorReintentable(String detalle) {
            return new ParticipantResult(false, false, true, detalle, List.of(), null);
        }

        /** Factory: error no reintentable. */
        public static ParticipantResult errorNoReintentable(String detalle) {
            return new ParticipantResult(false, false, false, detalle, List.of(), null);
        }
    }
}
