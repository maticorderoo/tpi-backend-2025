# 03 - Bounded Contexts y Dominio

## 1. Responsabilidades definitivas por microservicio

### Orders / Solicitudes
**Rol**: puerta de entrada del flujo, recibe a los clientes, valida disponibilidad del contenedor y publica la orden logistica con su seguimiento cronologico.

**Entidades que DEBE manejar**
- `Cliente`: datos de contacto, identificacion y medio de comunicacion con el operador.
- `Contenedor`: identificador unico, peso, volumen y estado operativo (enum `ContenedorEstado`).
- `Solicitud`: agregado principal que relaciona cliente-contenedor, guarda la estimacion de costo/tiempo, el origen/destino del traslado y la fecha de creacion. Su estado visible se deriva del contenedor asociado y de los tramos que la logistica defina.

**Entidades que NO debe manejar**
- `Camion`, `Tarifa`, `Ruta`, `Tramo`, `Deposito`, ni informacion fisica de flota o depositos.
- Cualquier integracion directa con `distance-client` o Google Maps (se delega a Logistics).

**Reglas de negocio que le corresponden**
- Validar que un contenedor no tenga mas de una solicitud activa simultanea, consultando el estado vigente del propio contenedor.
- Generar codigos/identidades de contenedor cuando el cliente no provee uno y hacer cumplir las restricciones de peso/volumen minimas.
- Ejecutar la estimacion inicial (costos aproximados, tiempo, estadias) combinando distancia estimada y metricas promedio de flota.
- Gobernar las transiciones del ciclo de vida de la solicitud (BORRADOR -> PROGRAMADA -> COMPLETADA/CANCELADA) y reflejarlas en el tracking del cliente.

**Datos que expone y consume**
- Expone: API para crear solicitudes, consultar detalle/seguimiento, recalcular estimaciones y cerrar costo/tiempo real; provee el tracking cronologico y el estado actual del contenedor.
- Consume: resumenes y estimaciones de rutas provenientes de Logistics (distancia, tramos, depositos) y los promedios de consumo/costo publicados por Fleet.

### Logistics
**Rol**: planificador operativo. Modela las rutas, tramos y estadias en depositos; asigna camiones y controla la ejecucion logistica.

**Entidades que DEBE manejar**
- `Ruta`: definicion completa de tramos, depositos intermedios, costos y metricas acumuladas.
- `Tramo`: movimiento o estadia puntual, con origen/destino, estado (`TramoEstado`), asignacion de camion y costos reales/estimados.
- `Deposito`: catalogo de ubicaciones habilitadas y su costo de estadia diario.
- Capability `DistanceData`: valor calculado (km/minutos) usado para planificar rutas.

- **Entidades que NO debe manejar**
- `Cliente`, `Contenedor` o cualquier dato de negocio del front de Orders.
- `Camion` y `Tarifa` como agregados persistentes (solo referencia IDs y consulta/actualiza disponibilidad via Fleet).

**Reglas de negocio que le corresponden**
- Validar secuencias de origen -> depositos -> destino y calcular distancias entre cada tramo.
- Calcular costos aproximados y reales por tramo (incluyendo estadias) y agregarlos al total de la ruta.
- Enforcear que un camion asignado soporte peso/volumen del tramo y bloquear disponibilidad mientras el tramo esta en transito.
- Generar eventos logisticos que disparen cambios de estado/costo en Orders y disponibilidad en Fleet.

**Datos que expone y consume**
- Expone: CRUD de rutas y depósitos, asignación/inicio/fin de tramos, consultas operativas sobre rutas y un endpoint interno de estimación de distancia (`/api/logistics/routes/estimaciones/distancia`) usado sólo como utilitario interno.
- Consume: metricas de disponibilidad/costo de Fleet (camiones y tarifas) y las ordenes publicadas por Orders (IDs de solicitud y datos del contenedor) para asociar rutas.

### Fleet
**Rol**: inventario de camiones y tarifas corporativas; fuente de verdad para disponibilidad, costos y consumos.

**Entidades que DEBE manejar**
- `Camion`: dominio, transportista, capacidades, costo por km, consumo por km y estado de disponibilidad.
- `Tarifa`: configuraciones globales (cargo de gestion, precio de combustible, etc.) usadas en calculos de Logistics.
- `FleetMetrics`: promedios derivados de camiones disponibles.

**Entidades que NO debe manejar**
- `Solicitud`, `Ruta`, `Tramo`, `Contenedor`, ni informacion de clientes o depositos.
- `DistanceData` o reglas de asignacion de rutas (eso pertenece a Logistics).

**Reglas de negocio que le corresponden**
- Validar unicidad de patente, capacidades minimas y coherencia de costos/consumos en altas y actualizaciones.
- Exponer y proteger la actualizacion de disponibilidad (solo operadores o servicios internos autorizados).
- Mantener tarifas maestras y proveer promedios de consumo/costo solo sobre camiones disponibles para estimaciones confiables.

**Datos que expone y consume**
- Expone: APIs para gestionar camiones (`/fleet/trucks`), actualizar disponibilidad (`/fleet/trucks/{id}/disponibilidad`) y consultar tarifas/metricas (`/fleet/metrics/promedios`).
- Consume: solicitudes de Logistics para reservar/liberar camiones y lecturas de Orders para conocer el contexto (solo IDs o motivos, nunca detalles de cliente).

### distance-client (capacidad dentro de Logistics)
**Rol**: integracion especializada con Google Directions API para traducir coordenadas en distancias y tiempos; no debe exponerse directamente al resto del dominio.

**Entidades/objetos que DEBE manejar**
- `DistanceData` y DTOs asociados al request/response hacia Google, encapsulados como libreria dentro de Logistics.

**Entidades que NO debe manejar**
- Cualquier agregado de dominio (Pedidos, Rutas, Camiones) o persistencia propia.

**Reglas de negocio que le corresponden**
- Configurar politica de reintentos, timeouts y parsing de la respuesta externa.
- Convertir legs de Google en un unico dato agregado usable por Logistics (km/minutos), manteniendo trazabilidad para auditoria.

**Datos que expone y consume**
- No expone endpoints públicos; Logistics se encarga de encapsular distance-client en `/api/logistics/routes/estimaciones/distancia`, aunque Orders ya no lo consume directamente.
- Consume: coordenadas (lat/lng) provistas por Logistics y la API key configurada para Google Maps.

## 2. Matriz Entidad ↔ Microservicio ideal

| Entidad / Artefacto | Rol funcional | Microservicio ideal | Notas sobre el estado actual |
| --- | --- | --- | --- |
| Cliente | Datos de identificacion y contacto del solicitante | Orders | Correctamente ubicado en Orders; se usa para validar ownership del contenedor antes de crear solicitudes. |
| Contenedor | Capacidad y estado operativo del contenedor | Orders | Logistics expone consultas temporales de contenedores (`logistics-service/src/main/java/com/tpibackend/logistics/controller/ContenedorController.java:39`) que deben migrar para evitar duplicacion. |
| Solicitud | Agregado que vincula cliente, contenedor y seguimiento | Orders | El modelo necesita persistir estado y coordenadas segun DER; ver comparacion en seccion 3. |
| Ruta | Plan logistico completo asociado a una solicitud | Logistics | Actualmente se calcula y asigna en Logistics, que persiste `solicitudId` para vincularla. |
| Tramo | Unidad minima de ejecucion (movimiento o estadia) | Logistics | Logistics controla los estados y asignaciones; expone datos a Orders via DTOs de `LogisticsClient`. |
| Deposito | Ubicaciones intermedias disponibles | Logistics | Reside en Logistics y se utiliza en la planificacion de rutas; no debe replicarse en otros servicios. |
| Camion | Flota fisica, capacidades y disponibilidad | Fleet | Logistics solo consulta/actualiza disponibilidad mediante `FleetClient`; las reglas viven en Fleet. |
| Tarifa | Configuraciones globales (combustible, gestion) | Fleet | Fleet provee CRUD y Logistics las consulta para calcular costos. |
| DistanceData / Estimacion de distancia | Valor agregado (km/min) usado por Orders y Logistics | Logistics (encapsulando distance-client) | Orders ahora consume el endpoint de Logistics (`orders-service/src/main/java/com/tpibackend/orders/client/LogisticsClient.java:61`); no debe volver a depender directo de la libreria. |
| Seguimiento del envio | Estado expuesto al cliente final | Orders (derivado de Contenedor/Tramos) | Debe armarse combinando el estado actual del contenedor y la informacion de tramos estimados/reales proveniente de Logistics; no existe entidad propia. |

## 3. Comparacion modelo ideal vs estado actual

### Entidades faltantes (o incompletas)
- `Solicitud` no persiste el estado actual ni los datos geograficos (origen/destino y coordenadas) que el DER exige (`initdb/02-orders-schema.sql:30`, `initdb/02-orders-schema.sql:34`) porque la entidad solo define cliente, contenedor y montos (`orders-service/src/main/java/com/tpibackend/orders/model/Solicitud.java:34`). Esto impide reconstruir el contexto del pedido sin consultar otros servicios.
- `SolicitudCreateRequest` recibe `origen` y `destino` (`orders-service/src/main/java/com/tpibackend/orders/dto/request/SolicitudCreateRequest.java:19`), pero al no existir esos campos en la entidad, la informacion se pierde despues del POST y Logistics no puede reutilizarla para crear rutas automaticamente.

### Entidades sobrantes (en el micro equivocado)
- Logistics sigue exponiendo un `ContenedorController`/`ContenedorService` para listar contenedores pendientes (`logistics-service/src/main/java/com/tpibackend/logistics/controller/ContenedorController.java:39`), aunque el agregado vive en Orders. Esta API debe eliminarse tras migrar la logica.
- Orders mantiene un endpoint manual para mutar el estado del contenedor (`orders-service/src/main/java/com/tpibackend/orders/controller/SolicitudController.java:192`), pero las transiciones reales deberian llegar desde Logistics cuando avanzan los tramos.

### Entidades duplicadas entre microservicios
- El estado operativo del envio se calcula en Logistics (tramos) y tambien se reflejaba de forma manual en Orders mediante historial propio. Es necesario eliminar la duplicacion y derivar siempre desde contenedor/tramos para que exista una sola fuente de verdad.
- La estimacion de distancia existe como capability interna (distance-client) y como endpoint HTTP. Mientras Orders ya consume el endpoint, la libreria sigue disponible en Logistics y podria volver a usarse directamente desde otros modulos si no se definen contratos claros.

### Relaciones que no coinciden con el enunciado
- Logistics actualiza estados y costos finales llamando directamente a Orders (`logistics-service/src/main/java/com/tpibackend/logistics/service/RutaService.java:156` y `logistics-service/src/main/java/com/tpibackend/logistics/service/TramoService.java:108`), en lugar de publicar eventos que Orders consuma. Esto rompe la separacion de bounded contexts prevista en el diagnostico inicial.
- Orders permite actualizar el estado de contenedor por fuera del flujo logistico (`orders-service/src/main/java/com/tpibackend/orders/controller/SolicitudController.java:192`), lo que contradice el enunciado (solo Logistics conoce la realidad de la ruta).
- Logistics expone datos de contenedor y rutas sin pasar por Orders, lo que genera consultas cruzadas sin control transaccional y dificulta seguir el DER.

## 4. Plan de acciones para reorganizar el dominio
1. **Alinear el modelo de Orders con el DER**: agregar campos `estado`, `origen`, `destino`, `origenLat/Lng`, `destinoLat/Lng` y persistirlos en `orders-service` junto con validaciones; actualizar DTOs, mappers y scripts SQL (`initdb/02-orders-schema.sql`) para que el agregado `Solicitud` complete el contexto de planeamiento.
2. **Centralizar contenedores en Orders**: mover las consultas de contenedores pendientes y su exposicion REST desde Logistics hacia Orders, eliminando `ContenedorController`/`ContenedorService` en Logistics y publicando una API de busqueda en Orders.
3. **Orquestar estados y costos mediante eventos**: reemplazar las llamadas directas de Logistics (`OrdersClient`) por eventos asincronicos “ruta programada / tramo iniciado / tramo finalizado” y hacer que Orders consuma esos eventos para actualizar el estado visible del contenedor/solicitud y el costo final; en paralelo, retirar el endpoint manual de estado en Orders.
4. **Blindar la capability de distancia**: documentar y versionar el endpoint `/api/logistics/routes/estimaciones/distancia` sólo para uso interno y remover cualquier dependencia directa a `distance-client` fuera de Logistics.
5. **Formalizar los contratos Fleet -> Logistics -> Orders**: publicar DTOs compartidos para disponibilidad de camiones y metricas, asegurando que Logistics solo manipule IDs y que Fleet siga siendo el unico en cambiar disponibilidad (`fleet-service/src/main/java/com/tpibackend/fleet/controller/CamionController.java:141`), mientras Orders solo lea los promedios expuestos (`fleet-service/src/main/java/com/tpibackend/fleet/controller/MetricsController.java:32`).

Este plan sienta las bases para que el siguiente ciclo implemente los cambios tecnicos (migracion de controladores, agregados y eventos) respetando los bounded contexts definidos.
