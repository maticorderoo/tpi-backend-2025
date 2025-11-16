# Plan de refactorización de microservicios

## 1. Resumen de problemas detectados
- **distance-client acoplado a Orders**: Orders importaba el paquete `com.tpibackend.distance` y exponía la API key de Google Maps, aunque la responsabilidad de cálculo de distancia pertenece al dominio logístico. Logistics ya incluía la librería y es el que genera rutas/tramos.
- **Bounded contexts cruzados entre Orders y Logistics**: Logistics actualiza estados y costos en Orders mediante `OrdersClient`, mientras que Orders publica endpoints para mutar estados operativos (`POST /orders/{id}/estado`) que deberían responder a eventos logísticos. Esta doble vía crea dos fuentes de verdad.
- **Gestión de contenedores duplicada**: Existe un `ContenedorController` y `ContenedorService` dentro de Logistics que exponen listados de contenedores pendientes, aunque Orders es el dueño del agregado Contenedor.
- **Inconsistencias de seguridad**: Fleet permitía actualizar disponibilidad de camiones sin autenticación y el `MetricsController` referenciaba un rol inexistente (`INTERNO`).
- **Nomenclatura de rutas en Logistics**: Los controladores exponían alias mezclados (`/routes`, `/logistics/rutas`, `/legs`) sin una convención clara sobre el prefijo `/api/logistics` que espera el Gateway y las colecciones de Postman.

## 2. Plan de refactorización priorizado
- [x] **Centralizar distance-client en Logistics**: ofrecer un endpoint interno para estimar distancia/tiempo y hacer que Orders deje de depender directamente de la librería.
- [x] **Documentar y marcar los puntos de acoplamiento Orders ↔ Logistics**: agregar TODOs donde Logistics muta Orders y donde Orders expone operaciones operativas temporales.
- [x] **Visibilizar la deuda de Contenedores en Logistics**: anotar en controladores/servicios que el módulo será migrado a Orders y reflejarlo en este plan.
- [x] **Blindar el endpoint sensible de Fleet**: exigir rol OPERADOR para `PUT /fleet/trucks/{id}/disponibilidad` y ajustar la config de seguridad para que solo Swagger/health sigan siendo públicos.
- [x] **Corregir la referencia al rol inexistente**: alinear `MetricsController` con los roles reales del realm (solo OPERADOR) y documentar el cambio.
- [ ] **Unificar definitivamente la nomenclatura REST de Logistics**: dejar únicamente `/logistics/{recurso}` como base (lo que implica actualizar Gateway/Postman) y eliminar aliases heredados.
- [ ] **Desacoplar eventos Orders ↔ Logistics**: reemplazar los `OrdersClient` de Logistics por eventos asincrónicos y eliminar el endpoint manual de Orders cuando Logistics pase a ser la fuente de estados.
- [ ] **Migrar ContenedorController a Orders**: crear endpoints/consultas equivalentes dentro de Orders y borrar la lógica duplicada en Logistics.
- [ ] **Reforzar tests y contratos**: actualizar los tests unitarios quebrados para reflejar las firmas reales de las entidades/DTOs y reactivar el pipeline de Maven.

## 3. Cambios aplicados en este paso
### 3.1 Distance-client ahora es capability de Logistics
- **Archivos**: `logistics-service/.../RutaController.java`, `RutaService.java`, nuevos DTOs `EstimacionDistanciaRequest/Response`, `orders-service/pom.xml`, `OrdersServiceApplication.java`, `SolicitudServiceImpl.java`, `LogisticsClient.java`, `DistanceEstimationResponse.java`, `application.properties`, tests asociados.
- **Modificación**: Logistics expone `POST /api/logistics/routes/estimaciones/distancia` (context path + `@RequestMapping("/logistics/routes")`) que encapsula `distance-client`. Orders dejó de importar la librería, añadió `spring-boot-starter-webflux` para conservar `WebClient`, y ahora obtiene la estimación a través de `LogisticsClient` y DTO propio.
- **Justificación**: se reduce el acoplamiento directo al proveedor externo y se prepara a Logistics como único dueño del cálculo de distancias. Orders sólo depende de su microservicio hermano mediante HTTP.

### 3.2 Marcado de acoplamientos Orders ↔ Logistics
- **Archivos**: `logistics-service/.../RutaService.java`, `TramoService.java`, `orders-service/.../SolicitudController.java`.
- **Modificación**: se añadieron comentarios `TODO` donde Logistics invoca `OrdersClient` para cambiar estados/costos y donde Orders permite cambios manuales en contenedores, dejando explícito que son transitorios.
- **Justificación**: documentar los puntos a refactorizar facilita la migración a eventos y evita que se olviden los lugares que comprometen los bounded contexts.

### 3.3 Contenedores en Logistics marcados como deuda
- **Archivos**: `logistics-service/.../ContenedorController.java`, `ContenedorService.java`.
- **Modificación**: se añadió un TODO en el controlador y el servicio aclarando que la responsabilidad debe volver a Orders.
- **Justificación**: visibiliza la desviación del diseño original para priorizar su migración en próximas iteraciones.

### 3.4 Seguridad en Fleet
- **Archivos**: `fleet-service/.../CamionController.java`, `SecurityConfig.java`.
- **Modificación**: `PUT /fleet/trucks/{id}/disponibilidad` exige `@PreAuthorize("hasRole('OPERADOR')")` y la configuración HTTP solo deja públicos Swagger y health, cerrando el resto de `/fleet/**` tras Keycloak.
- **Justificación**: evita que un consumidor fuera del Gateway manipule disponibilidad de camiones.

### 3.5 Rol INTERNO eliminado
- **Archivos**: `fleet-service/.../MetricsController.java`.
- **Modificación**: el endpoint de métricas ahora solo permite rol OPERADOR y su descripción fue actualizada.
- **Justificación**: el realm `tpi2025` solo define OPERADOR/TRANSPORTISTA/CLIENTE, por lo que mantener INTERNO inducía a error.

### 3.6 Convención de rutas en Logistics
- **Archivos**: `logistics-service/.../RutaController.java`, `TramoController.java`, `DepositoController.java`, `ContenedorController.java`, `orders-service/src/main/resources/application.properties`.
- **Modificación**: todos los controladores comparten el prefijo `/logistics/...` (que combinado con `server.servlet.context-path=/api` produce los endpoints `/api/logistics/...`), manteniendo alias heredados mientras se actualiza la documentación. Las propiedades de Orders ahora apuntan a `/routes/...` para alinear el cliente con esta convención.
- **Justificación**: se define una ruta canónica sin romper las colecciones actuales y se documenta el paso siguiente (remover alias cuando Gateway/Postman se actualicen).

### 3.7 Evidencia de testing
- **Comando**: `cd orders-service && mvn -q test`
- **Resultado**: falló por errores de compilación preexistentes en los tests (`SolicitudeControllerTest`/`SolicitudServiceImplTest` referencian métodos inexistentes). Ver chunk `8840b0†L1-L36`. Se deja asentado en el plan (pendiente final de la checklist) para abordarlo en una iteración futura.
