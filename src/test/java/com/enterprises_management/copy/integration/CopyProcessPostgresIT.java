package com.enterprises_management.copy.integration;

import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.application.input.ICopyProcessStartPort;
import com.enterprises_management.copy.application.input.command.IniciarProcesoCommand;
import com.enterprises_management.copy.application.services.SagaEngineService;
import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.domain.models.CopyProcess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("pg-integration")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "app.copy.orchestrator.enabled=true",
    "app.copy.orchestrator.bootstrap-config=true",
    "app.copy.orchestrator.participant.transport=stub",
    "app.copy.orchestrator.participants.enabled.products=true",
    "app.copy.orchestrator.participants.enabled.thirds=true",
    "app.copy.orchestrator.participants.enabled.treasury=true",
    "app.copy.orchestrator.participants.enabled.stock=true",
    "app.copy.orchestrator.participants.enabled.kardex=true",
    "app.copy.orchestrator.participants.enabled.factures=true",
    "app.copy.orchestrator.participants.enabled.inventorypeps=true",
    "app.copy.orchestrator.participants.enabled.auxiliarybook=true",
    "app.copy.orchestrator.stub.failure-rate=0",
    "app.copy.orchestrator.stub.latency-ms=0",
    "app.copy.orchestrator.default-retries=3",
    "app.copy.orchestrator.events.amqp.enabled=false",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "spring.sql.init.data-locations=",
    "jwt.auth.converter.principle-attribute=preferred_username",
    "jwt.auth.converter.resource-id=microservices_client",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false",
    "spring.main.allow-bean-definition-overriding=true"
})
@DisplayName("PG Integration: ciclo completo en PostgreSQL real (REQ-TC-01, REQ-TC-02, REQ-TC-03)")
class CopyProcessPostgresIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withInitScript("schema-copy.sql");

    @DynamicPropertySource
    static void pgProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @MockBean private JwtDecoder jwtDecoder;
    @Autowired private ICopyProcessStartPort startPort;
    @Autowired private SagaEngineService sagaEngineService;
    @Autowired private ICopyProcessQueryPort queryPort;

    @Test
    @DisplayName("schema-copy.sql ejecuta en PG y proceso DUPLICATE llega a COMPLETADO (REQ-TC-02, REQ-TC-03)")
    void schema_pg_y_ciclo_basico() {
        IniciarProcesoCommand cmd = new IniciarProcesoCommand(
                CopyProcessType.DUPLICATE, UUID.randomUUID(),
                "empresa-destino-pg", null, "pg-user", false);
        CopyProcess proceso = startPort.iniciar(cmd);
        sagaEngineService.avanzarFase(proceso.getId(), 1);
        CopyProcess result = queryPort.consultarProceso(proceso.getId());
        assertThat(result.getEstado()).isEqualTo(ProcessState.COMPLETADO);
    }
}
