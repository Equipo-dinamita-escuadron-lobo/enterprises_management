# Ping Pong
Para utilizar Swagger, primero se debe hacer ping u otra petición antes de acceder a la interfaz de Swagger.

```
http://localhost:8080/api/enterprises/test/ping
```
**URL Swagger**
```
http://localhost:8080/swagger-ui/index.html#/
```

## Módulo de copia (esqueleto — Hito 1)

### Propósito

Orquestador de saga para duplicar, respaldar o restaurar empresas entre instancias del sistema.
Ejecuta hasta 4 fases (BASE, INTERNAS, EXTERNAS, CIERRE), cada una con módulos participantes configurables.

### Cómo habilitar

El módulo está desactivado por defecto (`enabled=false` en `application.yml`).
Para activarlo, agregar en el perfil correspondiente:

```yaml
app:
  copy:
    orchestrator:
      enabled: true
      bootstrap-config: true   # inserta configuración inicial de fases
      stub-mode: true           # usa StubParticipantClient (sin servicios reales)
      stub:
        latency-ms: 100
        failure-rate: 0         # 0.0 = siempre exitoso, 1.0 = siempre falla
      default-retries: 3
      security:
        permissive-mode: true   # solo para dev
```

### Endpoints disponibles

| Método | URL | Permiso | Descripción |
|--------|-----|---------|-------------|
| POST | `/api/enterprises/copy/processes` | Backup_Create | Inicia un proceso de copia |
| GET | `/api/enterprises/copy/processes/{id}` | Backup_View | Estado consolidado |
| GET | `/api/enterprises/copy/processes/{id}/events` | Backup_View | Log de eventos |
| POST | `/api/enterprises/copy/processes/{id}/cancel` | Backup_Cancel | Cancela proceso activo |
| GET | `/api/enterprises/copy/processes/{id}/stream` | Backup_View | Stream SSE de eventos |
| GET | `/api/enterprises/copy/configuration/phases` | Backup_View | Matriz de configuración |
| POST | `/api/enterprises/copy/processes/{id}/equivalences` | isAuthenticated | Registra equivalencias de IDs |
| GET | `/api/enterprises/copy/processes/{id}/equivalences` | isAuthenticated | Consulta equivalencias |

### Cómo correr las pruebas E2E localmente

```bash
./mvnw test -Dtest="CopyProcessE2EHappyPathTest,CopyProcessE2EErrorPathTest,CopyEquivalenceIdempotencyTest,CopyProcessCancellationTest,CopyFeatureFlagOffIntegrationTest"
```

Los tests usan H2 en memoria + StubParticipantClient, sin servicios externos.

### Riesgos conocidos (Hito 1)

| ID | Descripción |
|----|-------------|
| D-R1 | El orquestador es en memoria (no sobrevive reinicio) |
| D-R2 | Sin compensación de fases completadas al cancelar |
| D-R3 | Sin control de concurrencia entre instancias (un pod a la vez) |
| D-R4 | Módulos participantes son stubs — no acceden a datos reales |
| D-R5 | Sin notificación de cancelación a módulos en curso |
| D-R6 | Sin límite de conexiones SSE concurrentes (max 50 evaluado en Hito 7) |
| D-R7 | Sin retry distribuido entre instancias del servicio |
| D-R8 | ENTERPRISES siempre orden=1 en Fase 1 (hardcoded en bootstrap) |

---

## Módulo de copia (Hito 2 — Contrato HTTP uniforme)

### Propósito

Cierra el cable real entre el orquestador y el primer participante (`account-catalogue`).
Reemplaza el `StubParticipantClient` del Hito 1 por un adaptador HTTP real (`HttpParticipantClientAdapter`)
que invoca al participante vía WebClient + Eureka.

### Feature flag — transport

```yaml
app:
  copy:
    orchestrator:
      participant:
        transport: http          # http (prod/dev) | stub (test/Hito 1)
        timeout-ms: 30000
      events:
        amqp:
          enabled: true          # false en perfil test
          exchange: copy.events
```

Con `transport=stub` se activan los `StubParticipantClient` del Hito 1 y los 188 tests del Hito 1 siguen verdes.
Con `transport=http` se activa `HttpParticipantClientAdapter` y se desactivan los stubs.

### Contrato HTTP uniforme — 4 endpoints del participante

Todo microservicio participante debe exponer exactamente estos 4 endpoints bajo `/api/<modulo>/copy`:

| # | Método | Ruta | Éxito | Errores |
|---|--------|------|-------|---------|
| 1 | POST | `/phase` | 200 | 400, 422, 500, 504 |
| 2 | GET | `/{idProceso}/status` | 200 | 404 |
| 3 | POST | `/{idProceso}/cancel` | 200 | 404, 409 |
| 4 | DELETE | `/{idProceso}/cleanup` | 204 | 404 |

**Request `POST /phase`:**

```json
{
  "idProceso":        "uuid",
  "fase":             1,
  "entOrigen":        "string",
  "entDestino":       "string",
  "snapshotCorte":    "ISO-8601",
  "equivalenciasPrev": [
    { "modulo": "string", "tabla": "string", "idViejo": "string", "idNuevo": "string" }
  ]
}
```

**Response `POST /phase`:**

```json
{
  "estado":              "COMPLETADO | COMPLETADO_CON_ADVERTENCIAS | ERROR_REINTENTABLE | ERROR_NO_REINTENTABLE",
  "registrosProcesados": 0,
  "equivalenciasGeneradas": [],
  "mensaje":             "string",
  "advertencias":        []
}
```

**Traducción HTTP → SagaEngine (HttpToParticipantResultMapper):**

| Código HTTP | exitoso | reintentable |
|-------------|---------|--------------|
| 200 COMPLETADO | true | — |
| 200 COMPLETADO_CON_ADVERTENCIAS | true | — |
| 400, 422, 404, 409 | false | false |
| 500, 503, 504 | false | true |
| Timeout / ConnRefused | false | true |

### Eventos AMQP — ciclo de vida del proceso

Exchange: `copy.events` (Topic, durable, mandatory=false).

| Evento | Routing Key |
|--------|-------------|
| Proceso iniciado | `copy.process.started` |
| Transición de fase | `copy.phase.transitioned` |
| Proceso completado | `copy.process.completed` |
| Proceso en error | `copy.process.failed` |
| Proceso cancelado | `copy.process.cancelled` |

La publicación es **best-effort**: un fallo AMQP no bloquea ni revierte la saga (REQ-EVENT-02).
En perfil test, `events.amqp.enabled=false` activa `LoggingEventPublisherAdapter` (no-op).

### Diagrama de flujo — Orchestrator → Participante (HTTP) y → Audit (AMQP)

```
[SagaEngineService]
        |
        |-- avanzarFase(idProceso, fase)
        |
        v
[HttpParticipantClientAdapter]
        |
        |-- POST lb://CATALOGUE/api/accountCatalogue/copy/phase
        |   (via WebClient @LoadBalanced + Eureka)
        |
        v
[account-catalogue REST Controller]
        |
        |-- CopyPhaseService (topological sort, tenant override, idempotencia)
        |
        |<-- 200 { estado, equivalenciasGeneradas, advertencias }
        |
[SagaEngineService: registra equivalencias, emite evento]
        |
        |-- [NotificadorEstadoSseAdapter] ──> SSE clients
        |
        |-- [RabbitEventPublisherAdapter] ──> copy.events (Topic Exchange)
                                                |
                                                └──> audit.copy.events.queue ──> [audit service]
```

### Reemplazo del stub

Con `transport=http`, el `HttpParticipantClientAdapter` reemplaza al `StubParticipantClient` para el módulo CATALOGUE.
Los módulos ENTERPRISES y PRODUCTS siguen usando stubs hasta que sus participantes reales se integren.

### Cómo correr los tests E2E del Hito 2

```bash
# Tests WireMock E2E (happy path + error path + timeout)
./mvnw test -Dtest="CopyE2EWireMockHappyPathTest,CopyE2EWireMockErrorPathTest,CopyE2EWireMockTimeoutTest"

# Regresión feature flag stub
./mvnw test -Dtest="CopyFeatureFlagStubRegressionTest"

# Suite completa (incluye Hito 1 + Hito 2)
./mvnw test
```

### Riesgos conocidos (Hito 2)

| ID | Descripción |
|----|-------------|
| H2-R1 | JWT puede expirar durante sagas largas → 401 → reintentable (mitigación: mTLS en Hito 3) |
| H2-R2 | Backoff fijo (no exponencial) — evaluar en Hito 3 si hay thrashing |
| H2-R3 | Sin outbox AMQP — BD es fuente de verdad; outbox diferido a Hito 7 |
| H2-R4 | Tests Testcontainers E2E diferidos a Hito 7 (complejidad docker en CI) |

---

## Módulo de copia (Hito 3 — Participantes reales: PRODUCTS + THIRDS)

### Propósito

Integra los participantes `products-management` y `thirds-management` al orquestador.
Habilita el encadenamiento automático Fase 1 → Fase 2 y la propagación de `equivalenciasPrev` y Bearer token.

### Encadenamiento automático de fases (REQ-CHAIN-01, ADR-27)

`SagaEngineService.evaluarCompletacionFase` consulta `configRepo.buscarActivosPorFase(n+1)` tras completar la fase N.
Si hay módulos activos en la fase siguiente, invoca `avanzarFase(id, n+1)` automáticamente.
Si la lista está vacía, la fase se marca `OMITIDA` y el proceso avanza a COMPLETADO.

### Bearer propagation (REQ-FIX-02, ADR-29)

El `ProcesoCopiaController` captura el header `Authorization` y lo almacena en `CopyProcess.bearerToken` (campo transient — NO persiste en BD).
El `HttpParticipantClientAdapter.construirRequest()` lo inyecta en cada llamada HTTP al participante.
Si no hay request scope activo (e.g., reanudación desde AMQP), se emite un log WARN y la llamada continúa sin Bearer.

### EquivalenciasPrev cross-fase (REQ-EQUIVPREV-01, ADR-28)

El mapa estático `PARTICIPANT_DEPENDENCIES` en `HttpParticipantClientAdapter` define qué equivalencias recibe cada participante:

| Módulo | Equivalencias recibidas |
|--------|------------------------|
| CATALOGUE | ninguna |
| PRODUCTS | CATALOGUE |
| THIRDS | ninguna |
| ENTERPRISES | ninguna |

Las equivalencias se cargan de `IEquivalenceRepositoryPort.buscarConFiltros(idProceso, ...)` y se filtran por el mapa.

### Bootstrap — Fase 2 con PRODUCTS y THIRDS (REQ-BOOTSTRAP-01, ADR-27)

`PhaseConfigBootstrap` inserta en `copy_phase_config` al arrancar (con `bootstrap-config=true`):

| Fase | Módulo | Orden | Feature flag |
|------|--------|-------|-------------|
| 1 | ENTERPRISES | 1 | siempre |
| 1 | CATALOGUE | 2 | siempre |
| 1 | PRODUCTS | 3 | siempre |
| 2 | PRODUCTS | 1 | `participants.enabled.products=true` |
| 2 | THIRDS | 2 | `participants.enabled.thirds=true` |

### Beans HTTP — PRODUCTS y THIRDS (REQ-PRODUCTS-01, REQ-THIRDS-04, ADR-32)

`HttpParticipantClientConfig` registra `httpProductsClient` y `httpThirdsClient` cuando `transport=http`:
- `lb://PRODUCTS` → `products-management` (registrado como `PRODUCTS` en Eureka)
- `lb://THIRDS` → `thirds-management` (registrado como `THIRDS` en Eureka)

Con `transport=stub`, los beans `stubProductsClient` y `stubThirdsClient` (4 stubs totales) se activan en su lugar.

### Feature flags (REQ-BOOTSTRAP-01)

```yaml
app:
  copy:
    orchestrator:
      participants:
        enabled:
          products: true    # habilita PRODUCTS en Fase 2 bootstrap + HTTP bean
          thirds: true      # habilita THIRDS en Fase 2 bootstrap + HTTP bean
```

### Cómo correr los tests E2E del Hito 3

```bash
# Test multi-fase (CATALOGUE Fase 1 + PRODUCTS+THIRDS Fase 2)
./mvnw test -Dtest="CopyOrchestratorMultiPhaseE2ETest"

# Bootstrap tests
./mvnw test -Dtest="PhaseConfigBootstrapTest"

# Suite completa
./mvnw test
```

### Riesgos conocidos (Hito 3)

| ID | Descripción |
|----|-------------|
| H3-R1 | Bearer token del CopyProcess se pierde si el proceso se recarga de BD (field transient) |
| H3-R2 | Spring Cloud BOM divergente: PM usa 2023.0.0 (Boot 3.2.3), TM usa 2024.0.1 (Boot 3.4.7) |
| H3-R3 | PRODUCTS y THIRDS se invocan secuencialmente en Fase 2 — sin paralelismo |
| H3-R4 | Si PRODUCTS falla, THIRDS no se invoca — no hay reanudación parcial de fase |

---

## Diagrama de Contexto

![](https://res.cloudinary.com/dtmtu3rkh/image/upload/v1718347245/Contexto.drawio_fays6y.svg)

## Diagrama de Contenedores
![](https://res.cloudinary.com/dtmtu3rkh/image/upload/v1718347271/Contonedores.drawio_vv9z8a.svg)

## Diagrama de Componentes

![](https://res.cloudinary.com/dtmtu3rkh/image/upload/v1718347136/Componentes.drawio_vbnfok.svg)

## Diagrama de Paquetes
![](https://res.cloudinary.com/dtmtu3rkh/image/upload/v1718347280/ClasesAccountCAatalogue.drawio_tfu9pb.svg)
