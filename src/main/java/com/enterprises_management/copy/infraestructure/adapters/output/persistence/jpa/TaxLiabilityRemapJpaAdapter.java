package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa;

import com.enterprises_management.copy.application.output.ITaxLiabilityRemapPort;
import com.enterprises_management.copy.domain.models.CopyEquivalenceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaxLiabilityRemapJpaAdapter implements ITaxLiabilityRemapPort {

    private static final Logger log = LoggerFactory.getLogger(TaxLiabilityRemapJpaAdapter.class);

    private final JdbcTemplate jdbc;

    public TaxLiabilityRemapJpaAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void remapear(String empresaDestino, List<CopyEquivalenceId> taxEquivalencias) {
        for (CopyEquivalenceId eq : taxEquivalencias) {
            try {
                Long idViejo = Long.parseLong(eq.getIdViejo());
                Long idNuevo = Long.parseLong(eq.getIdNuevo());
                int updated = jdbc.update(
                        "UPDATE enterprise_tax_liabilities SET tax_liability_id = ? " +
                        "WHERE enterprise_id = CAST(? AS uuid) AND tax_liability_id = ?",
                        idNuevo, empresaDestino, idViejo
                );
                log.info("Remapeado tax_liability {} → {} para empresa {} ({} fila(s))",
                        idViejo, idNuevo, empresaDestino, updated);
            } catch (Exception ex) {
                log.warn("No se pudo remapear tax_liability {} → {} para empresa {}: {}",
                        eq.getIdViejo(), eq.getIdNuevo(), empresaDestino, ex.getMessage());
            }
        }
    }
}
