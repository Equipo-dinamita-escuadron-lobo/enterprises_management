package com.enterprises_management.copy.application.output;

import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import java.util.List;

public interface ITaxLiabilityRemapPort {

    /**
     * Actualiza enterprise_tax_liabilities para la empresa restaurada,
     * reemplazando los IDs de impuesto originales (del backup) por los nuevos
     * IDs asignados por el microservicio contable durante el restore.
     */
    void remapear(String empresaDestino, List<CopyEquivalenceId> taxEquivalencias);
}
