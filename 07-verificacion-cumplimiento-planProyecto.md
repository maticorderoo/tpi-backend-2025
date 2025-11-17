# 07 - Verificación de cumplimiento planProyecto

## Summary
* Se alinearon todos los controladores de Logistics al prefijo único `/api/logistics/{recurso}` y se ampliaron los casos de uso exigidos (asignaciones, inicios/finalizaciones y consultas de tramos) para que coincidan con los contratos definidos en los planes funcionales.【F:logistics-service/src/main/java/com/tpibackend/logistics/controller/RutaController.java†L34-L142】【F:logistics-service/src/main/java/com/tpibackend/logistics/controller/TramoController.java†L33-L181】【F:logistics-service/src/main/java/com/tpibackend/logistics/controller/DepositoController.java†L31-L79】【F:logistics-service/src/main/java/com/tpibackend/logistics/service/TramoService.java†L200-L258】
* Se sincronizaron los clientes HTTP, colecciones Postman, guía de pruebas y script de saneamiento para reflejar los endpoints estandarizados, evitando rutas alias o desfasadas en Orders y en las herramientas de testing.【F:orders-service/src/main/resources/application.properties†L24-L33】【F:POSTMAN_GUIDE.md†L130-L248】【F:TPI-2025-COMPLETE.postman_collection.json†L617-L716】【F:TPI-2025-COMPLETE.postman_collection.json.bak†L700-L818】【F:fix-tramos-endpoints.js†L1-L34】

## Testing
* ⚠️ `mvn -f logistics-service/pom.xml test` (falla por la dependencia privada `com.tpibackend.distance:distance-client:1.0.0`, no disponible en Maven Central según el log de Maven).【82b28a†L131-L164】

---

## Planes leídos y verificaciones
1. `planProyecto/02-plan-refactor-microservicios.md`: rutas unificadas en Logistics y dependencia de Orders hacia `/api/logistics/routes/...`.【F:planProyecto/02-plan-refactor-microservicios.md†L47-L54】
2. `planProyecto/03-bounded-contexts-y-dominio.md`: ownership de contenedores en Orders y prohibición de exponerlos desde Logistics.【F:planProyecto/03-bounded-contexts-y-dominio.md†L90-L114】
3. `planProyecto/03-modelos-dominio-ajustes.md`: seguimiento basado en contenedor+ruta (sin eventos administrativos).【F:planProyecto/03-modelos-dominio-ajustes.md†L250-L280】
4. `planProyecto/04 - Endpoints y casos de uso.md`: catálogo completo de endpoints `/api/logistics/routes` y `/api/logistics/tramos`.【F:planProyecto/04 - Endpoints y casos de uso.md†L12-L33】
5. `planProyecto/04-eliminar-solicitud-evento.md`: tracking consolidado y TODOs en `SolicitudServiceImpl`.【F:planProyecto/04-eliminar-solicitud-evento.md†L8-L26】
6. `planProyecto/05 - Logistics, distance-client y cálculo de costos.md`: Logistics como única puerta para distance-client.【F:planProyecto/05 - Logistics, distance-client y cálculo de costos.md†L3-L32】
7. `planProyecto/06 - Pruebas E2E y limpieza final.md`: flujo E2E por rol y colección Postman oficial.【F:planProyecto/06 - Pruebas E2E y limpieza final.md†L15-L67】

## Detalle de verificaciones por plan
### 02-plan-refactor-microservicios.md
- Validado que cada controlador de Logistics mantiene un único `@RequestMapping("/logistics/...")` y que Orders consume `/routes/...` vía configuración centralizada.【F:logistics-service/src/main/java/com/tpibackend/logistics/controller/RutaController.java†L34-L141】【F:orders-service/src/main/resources/application.properties†L24-L28】

### 03-bounded-contexts-y-dominio.md
- Se corroboró que Logistics solo expone dominio operativo (rutas/tramos/depositos). Se mantiene documentada la deuda de contenedores para Orders (ver TODOs pendientes).【F:logistics-service/src/main/java/com/tpibackend/logistics/controller/ContenedorController.java†L27-L64】

### 03-modelos-dominio-ajustes.md
- Seguimiento se deriva de contenedor + ruta: los endpoints de Logistics devuelven el resumen de tramos requerido para que Orders pueda integrarlo vía `LogisticsClient`.【F:logistics-service/src/main/java/com/tpibackend/logistics/controller/RutaController.java†L81-L141】

### 04 - Endpoints y casos de uso.md
- Se verificó la existencia y nomenclatura de los endpoints exigidos: `/api/logistics/routes`, `/api/logistics/routes/{rutaId}/asignaciones`, `/api/logistics/routes/solicitudes/{id}`, `/api/logistics/tramos/{id}/asignaciones|inicios|finalizaciones` y `GET /api/logistics/tramos?camionId`.【F:logistics-service/src/main/java/com/tpibackend/logistics/controller/RutaController.java†L34-L141】【F:logistics-service/src/main/java/com/tpibackend/logistics/controller/TramoController.java†L33-L181】

### 04-eliminar-solicitud-evento.md
- Se corroboró que la API de tracking se mantiene libre de eventos administrativos y solo depende de contenedor+ruta. No se requirieron cambios adicionales (documentado en TODOs).【F:planProyecto/04-eliminar-solicitud-evento.md†L8-L26】

### 05 - Logistics, distance-client y cálculo de costos.md
- Verificado que Orders sigue llamando únicamente al endpoint `/api/logistics/routes/estimaciones/distancia` mediante el cliente HTTP, sin dependencia directa de la librería externa.【F:orders-service/src/main/resources/application.properties†L24-L28】

### 06 - Pruebas E2E y limpieza final.md
- Se actualizó la colección Postman y la guía para reflejar los flujos descritos (roles, secuencia y endpoints).【F:POSTMAN_GUIDE.md†L135-L248】【F:TPI-2025-COMPLETE.postman_collection.json†L617-L716】

## Inconsistencias encontradas y correcciones aplicadas
1. **Alias y caminos mezclados en Logistics**: los controladores admitían `/routes`, `/rutas`, `/legs`, `/tramos` rompiendo el contrato `/api/logistics/{recurso}` indicado en el plan.【F:planProyecto/02-plan-refactor-microservicios.md†L47-L50】 Se consolidó cada `@RequestMapping` y sus subrutas, además de exponer la variante `/solicitudes/{id}` y `/asignaciones` conforme al catálogo.【F:logistics-service/src/main/java/com/tpibackend/logistics/controller/RutaController.java†L34-L141】【F:logistics-service/src/main/java/com/tpibackend/logistics/controller/TramoController.java†L33-L145】【F:logistics-service/src/main/java/com/tpibackend/logistics/controller/DepositoController.java†L31-L79】
2. **Faltaba el `GET /api/logistics/tramos` y `GET /api/logistics/tramos/{id}` exigidos por los casos de uso**.【F:planProyecto/04 - Endpoints y casos de uso.md†L18-L21】 Se añadieron los métodos `listar` y `obtenerDetalle` en el controlador junto con el soporte en `TramoService`.【F:logistics-service/src/main/java/com/tpibackend/logistics/controller/TramoController.java†L47-L61】【F:logistics-service/src/main/java/com/tpibackend/logistics/service/TramoService.java†L200-L219】
3. **Colección Postman y guía usaban rutas antiguas (`/rutas`, `/tramos/{id}/asignar`, `/inicio`, `/fin`)** contrariando el plan de limpieza E2E.【F:planProyecto/06 - Pruebas E2E y limpieza final.md†L15-L27】【F:planProyecto/04 - Endpoints y casos de uso.md†L12-L21】 Se actualizaron ambos artefactos, incluido el respaldo `.bak`, para que solo utilicen `/routes`, `/tramos/{id}/asignaciones`, `/inicios` y `/finalizaciones`.【F:TPI-2025-COMPLETE.postman_collection.json†L617-L715】【F:TPI-2025-COMPLETE.postman_collection.json.bak†L700-L818】【F:POSTMAN_GUIDE.md†L135-L247】
4. **Script `fix-tramos-endpoints.js` seguía corrigiendo hacia `/inicio` y `/fin`, reproduciendo el alias que el plan pidió eliminar**.【F:planProyecto/06 - Pruebas E2E y limpieza final.md†L66-L67】 Se ajustó para que normalice a `/inicios` y `/finalizaciones`, evitando divergencias futuras.【F:fix-tramos-endpoints.js†L11-L34】
5. **Orders continuaba apuntando a `/routes/solicitud/{id}` (singular)** mientras el contrato definido es `/routes/solicitudes/{id}`.【F:planProyecto/04 - Endpoints y casos de uso.md†L14-L15】 Se corrigió la configuración del cliente para garantizar compatibilidad con Logistics.【F:orders-service/src/main/resources/application.properties†L24-L28】
6. **Tests de seguridad aún apuntaban a endpoints obsoletos**. Se actualizaron las pruebas de `RutaControllerSecurityTest` y `TramoControllerSecurityTest` para cubrir los nuevos paths y roles esperados, asegurando la validación automática de seguridad por rol.【F:logistics-service/src/test/java/com/tpibackend/logistics/controller/RutaControllerSecurityTest.java†L41-L120】【F:logistics-service/src/test/java/com/tpibackend/logistics/controller/TramoControllerSecurityTest.java†L37-L125】

## TODOs no automatizables en esta iteración
1. **Migrar ContenedorController a Orders**: el plan indica que Logistics no debe exponer contenedores, pero la funcionalidad sigue allí; se requiere mover lógica y datos a Orders.【F:planProyecto/04 - Endpoints y casos de uso.md†L17-L33】【F:planProyecto/03-bounded-contexts-y-dominio.md†L90-L114】【F:logistics-service/src/main/java/com/tpibackend/logistics/controller/ContenedorController.java†L27-L64】
2. **Eliminar el endpoint manual `POST /api/orders/{id}/estado` y reemplazarlo por eventos Logistics→Orders**.【F:planProyecto/04 - Endpoints y casos de uso.md†L30-L33】 Aún depende de trabajo de eventos y coordinación entre microservicios.
3. **Reemplazar los `OrdersClient` utilizados por Logistics por mensajería/eventos** para respetar los bounded contexts.【F:planProyecto/04 - Endpoints y casos de uso.md†L31-L33】【F:planProyecto/03-bounded-contexts-y-dominio.md†L117-L125】
4. **Agregar los campos faltantes en `Solicitud` (estado, origen/destino, coordenadas)** según el DER, pendiente del plan de dominio.【F:planProyecto/03-bounded-contexts-y-dominio.md†L105-L120】
5. **Colección Postman aún incluye flujo de contenedores en Logistics hasta que se complete la migración**; deberá actualizarse nuevamente cuando Orders publique el endpoint definitivo (ver punto 1).

## Estado final
- **Faltan tareas**: quedan pendientes las migraciones de contenedores, la eliminación de actualizaciones manuales en Orders y la sustitución de clientes REST por eventos. Es necesario abordar los TODOs descritos para completar el cumplimiento pleno del plan.
