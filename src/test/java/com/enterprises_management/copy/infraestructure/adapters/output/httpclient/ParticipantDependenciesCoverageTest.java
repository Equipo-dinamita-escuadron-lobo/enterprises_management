package com.enterprises_management.copy.infraestructure.adapters.output.httpclient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que PARTICIPANT_DEPENDENCIES cubra los módulos de Hito 4a.
 * Se amplía en Fase 10 para cubrir FACTURES, INVENTORYPEPS, AUXILIARY-BOOK.
 * REQ-PHASE3-02, ADR-43.
 */
class ParticipantDependenciesCoverageTest {

    @Test
    @DisplayName("Fase 4a: TREASURY, STOCK y KARDEX están registrados en PARTICIPANT_DEPENDENCIES")
    void dependencias_hito4a_presentes() {
        assertThat(HttpParticipantClientAdapter.PARTICIPANT_DEPENDENCIES)
                .containsKey("TREASURY")
                .containsKey("STOCK")
                .containsKey("KARDEX");
    }

    @Test
    @DisplayName("TREASURY depende de CATALOGUE (ADR-43)")
    void treasury_dependeDeCatalogue() {
        assertThat(HttpParticipantClientAdapter.PARTICIPANT_DEPENDENCIES.get("TREASURY"))
                .containsExactly("CATALOGUE");
    }

    @Test
    @DisplayName("STOCK depende de PRODUCTS (ADR-43)")
    void stock_dependeDeProducts() {
        assertThat(HttpParticipantClientAdapter.PARTICIPANT_DEPENDENCIES.get("STOCK"))
                .containsExactly("PRODUCTS");
    }

    @Test
    @DisplayName("KARDEX depende de PRODUCTS (ADR-43)")
    void kardex_dependeDeProducts() {
        assertThat(HttpParticipantClientAdapter.PARTICIPANT_DEPENDENCIES.get("KARDEX"))
                .containsExactly("PRODUCTS");
    }

    @Test
    @DisplayName("Módulos base Hito 1-3 siguen presentes")
    void modulosBase_siguePresentes() {
        assertThat(HttpParticipantClientAdapter.PARTICIPANT_DEPENDENCIES)
                .containsKey("PRODUCTS")
                .containsKey("CATALOGUE")
                .containsKey("ENTERPRISES")
                .containsKey("THIRDS");
    }

    @Test
    @DisplayName("Fase 4b: FACTURES, INVENTORYPEPS y AUXILIARY-BOOK están registrados (ADR-43)")
    void dependencias_hito4b_presentes() {
        assertThat(HttpParticipantClientAdapter.PARTICIPANT_DEPENDENCIES)
                .containsKey("FACTURES")
                .containsKey("INVENTORYPEPS")
                .containsKey("AUXILIARY-BOOK");
    }

    @Test
    @DisplayName("FACTURES depende de PRODUCTS y THIRDS (ADR-43)")
    void factures_dependeDeProductsYThirds() {
        assertThat(HttpParticipantClientAdapter.PARTICIPANT_DEPENDENCIES.get("FACTURES"))
                .containsExactlyInAnyOrder("PRODUCTS", "THIRDS");
    }

    @Test
    @DisplayName("INVENTORYPEPS depende de PRODUCTS (ADR-43)")
    void inventoryPeps_dependeDeProducts() {
        assertThat(HttpParticipantClientAdapter.PARTICIPANT_DEPENDENCIES.get("INVENTORYPEPS"))
                .containsExactly("PRODUCTS");
    }

    @Test
    @DisplayName("AUXILIARY-BOOK depende de CATALOGUE y THIRDS (ADR-43)")
    void auxiliaryBook_dependeDeCatalogueYThirds() {
        assertThat(HttpParticipantClientAdapter.PARTICIPANT_DEPENDENCIES.get("AUXILIARY-BOOK"))
                .containsExactlyInAnyOrder("CATALOGUE", "THIRDS");
    }
}
