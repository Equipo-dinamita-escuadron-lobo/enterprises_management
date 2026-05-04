package com.enterprises_management.copy.integration;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Centraliza los stub builders de WireMock para los 9 participantes del copy.
 * Usado por E2EFases123H4Test y tests de carga futuros (ADR-41, REQ-E2E-H4-01).
 */
final class WireMockParticipantFactory {

    private static final String BODY_COMPLETADO = """
            {
              "estado": "COMPLETADO",
              "registrosProcesados": 2,
              "equivalenciasGeneradas": [],
              "mensaje": "Copia completada",
              "advertencias": []
            }
            """;

    private WireMockParticipantFactory() {}

    /** Registra respuesta COMPLETADO para un path dado. */
    static void stubCompletado(WireMockServer wm, String path) {
        wm.stubFor(post(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(BODY_COMPLETADO)));
    }

    /** Stub de todos los 9 participantes (F1+F2+F3) devuelven COMPLETADO. */
    static void stubTodosCompletados(WireMockServer wm) {
        stubCompletado(wm, "/api/accountCatalogue/copy/phase");
        stubCompletado(wm, "/api/products/copy/phase");
        stubCompletado(wm, "/api/thirds/copy/phase");
        stubCompletado(wm, "/api/treasury/copy/phase");
        stubCompletado(wm, "/api/stock/copy/phase");
        stubCompletado(wm, "/api/kardex/weighted-average/copy/phase");
        stubCompletado(wm, "/api/factures/copy/phase");
        stubCompletado(wm, "/api/kardex/peps/copy/phase");
        stubCompletado(wm, "/api/auxiliary-books/copy/phase");
    }

    /** Stub de solo los participantes de Fase 1. */
    static void stubFase1(WireMockServer wm) {
        stubCompletado(wm, "/api/accountCatalogue/copy/phase");
    }

    /** Stub de participantes de Fase 2. */
    static void stubFase2(WireMockServer wm) {
        stubCompletado(wm, "/api/products/copy/phase");
        stubCompletado(wm, "/api/thirds/copy/phase");
    }

    /** Stub de los 6 participantes de Fase 3. */
    static void stubFase3(WireMockServer wm) {
        stubCompletado(wm, "/api/treasury/copy/phase");
        stubCompletado(wm, "/api/stock/copy/phase");
        stubCompletado(wm, "/api/kardex/weighted-average/copy/phase");
        stubCompletado(wm, "/api/factures/copy/phase");
        stubCompletado(wm, "/api/kardex/peps/copy/phase");
        stubCompletado(wm, "/api/auxiliary-books/copy/phase");
    }
}
