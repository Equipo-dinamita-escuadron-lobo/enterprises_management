package com.enterprises_management.copy.infraestructure.adapters.output.client;

import com.enterprises_management.copy.application.output.IParticipantClientPort;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.models.CopyModuleExecution;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.infraestructure.adapters.output.participant.stub.StubParticipantClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para StubParticipantClient.
 * Verifica comportamiento con failure-rate=0, failure-rate=1 y latencia configurable.
 * ADR-13: stub configurable por propiedades.
 */
class StubParticipantClientTest {

    private CopyProcess crearProceso() {
        return CopyProcess.crear(CopyProcessType.DUPLICATE, UUID.randomUUID(), "Destino", null, "user-test");
    }

    private CopyModuleExecution crearEjecucion(String idProceso) {
        return CopyModuleExecution.crear(idProceso, UUID.randomUUID().toString(), "ENTERPRISES");
    }

    @Test
    @DisplayName("con failure-rate=0 siempre retorna resultado exitoso")
    void invoke_failureRateCero_retornaExito() {
        StubParticipantClient stub = new StubParticipantClient("ENTERPRISES", 0, 0.0);
        CopyProcess proceso = crearProceso();
        CopyModuleExecution ejecucion = crearEjecucion(proceso.getId());

        IParticipantClientPort.ParticipantResult resultado = stub.invoke(proceso, ejecucion);

        assertThat(resultado.exitoso()).isTrue();
    }

    @Test
    @DisplayName("con failure-rate=1 siempre retorna error reintentable")
    void invoke_failureRateUno_retornaErrorReintentable() {
        StubParticipantClient stub = new StubParticipantClient("ENTERPRISES", 0, 1.0);
        CopyProcess proceso = crearProceso();
        CopyModuleExecution ejecucion = crearEjecucion(proceso.getId());

        IParticipantClientPort.ParticipantResult resultado = stub.invoke(proceso, ejecucion);

        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.reintentable()).isTrue();
    }

    @Test
    @DisplayName("resultado exitoso contiene exactamente 3 equivalencias ficticias (ADR-13)")
    void invoke_exitoso_contieneTresEquivalencias() {
        StubParticipantClient stub = new StubParticipantClient("CATALOGUE", 0, 0.0);
        CopyProcess proceso = crearProceso();
        CopyModuleExecution ejecucion = crearEjecucion(proceso.getId());

        IParticipantClientPort.ParticipantResult resultado = stub.invoke(proceso, ejecucion);

        assertThat(resultado.exitoso()).isTrue();
        assertThat(resultado.equivalencias()).hasSize(3);
    }

    @Test
    @DisplayName("las equivalencias ficticias tienen idNuevo = idViejo + 1000 para el módulo invocado (ADR-13)")
    void invoke_exitoso_equivalenciasTienenIdNuevoIncrementado() {
        StubParticipantClient stub = new StubParticipantClient("PRODUCTS", 0, 0.0);
        CopyProcess proceso = crearProceso();
        CopyModuleExecution ejecucion = crearEjecucion(proceso.getId());

        IParticipantClientPort.ParticipantResult resultado = stub.invoke(proceso, ejecucion);

        // Verificar que las equivalencias tienen el módulo correcto y relación idViejo→idNuevo
        assertThat(resultado.equivalencias()).allSatisfy(eq -> {
            assertThat(eq.modulo()).isEqualTo("PRODUCTS");
            int idViejo = Integer.parseInt(eq.idViejo());
            int idNuevo = Integer.parseInt(eq.idNuevo());
            assertThat(idNuevo).isEqualTo(idViejo + 1000);
        });
    }

    @Test
    @DisplayName("resultado con failure-rate=1 no contiene equivalencias")
    void invoke_errorReintentable_noContieneEquivalencias() {
        StubParticipantClient stub = new StubParticipantClient("ENTERPRISES", 0, 1.0);
        CopyProcess proceso = crearProceso();
        CopyModuleExecution ejecucion = crearEjecucion(proceso.getId());

        IParticipantClientPort.ParticipantResult resultado = stub.invoke(proceso, ejecucion);

        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.equivalencias()).isEmpty();
    }

    @Test
    @DisplayName("getNombreModulo retorna el nombre configurado")
    void getNombreModulo_retornaNombreConfigurado() {
        StubParticipantClient stub = new StubParticipantClient("PRODUCTS", 0, 0.0);

        assertThat(stub.getNombreModulo()).isEqualTo("PRODUCTS");
    }

    @Test
    @DisplayName("latency-ms=50 causa retardo medible en invoke")
    void invoke_conLatencia_causaRetardoMedible() {
        int latenciaMs = 50;
        StubParticipantClient stub = new StubParticipantClient("ENTERPRISES", latenciaMs, 0.0);
        CopyProcess proceso = crearProceso();
        CopyModuleExecution ejecucion = crearEjecucion(proceso.getId());

        long inicio = System.currentTimeMillis();
        stub.invoke(proceso, ejecucion);
        long transcurrido = System.currentTimeMillis() - inicio;

        // Verificar que tardó al menos latenciaMs - 10ms (tolerancia de scheduling)
        assertThat(transcurrido).isGreaterThanOrEqualTo(latenciaMs - 10);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 0.5, 1.0})
    @DisplayName("diferentes failure-rates producen el comportamiento correcto (parametrizado)")
    void invoke_conDiferentesFailureRates_produceBehaviorCorrecto(double failureRate) {
        StubParticipantClient stub = new StubParticipantClient("ENTERPRISES", 0, failureRate);
        CopyProcess proceso = crearProceso();
        CopyModuleExecution ejecucion = crearEjecucion(proceso.getId());

        IParticipantClientPort.ParticipantResult resultado = stub.invoke(proceso, ejecucion);

        if (failureRate == 0.0) {
            assertThat(resultado.exitoso()).isTrue();
        } else if (failureRate == 1.0) {
            assertThat(resultado.exitoso()).isFalse();
            assertThat(resultado.reintentable()).isTrue();
        }
        // Para 0.5 solo verificamos que retorna un resultado no nulo
        assertThat(resultado).isNotNull();
    }
}
