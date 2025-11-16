# 03 - Modelos de dominio y ajustes

## 1. Modelo objetivo por entidad clave

### Cliente (Orders)
- **Microservicio**: `orders-service`
- **Campos recomendados**

| Campo | Tipo logico | Descripcion |
| --- | --- | --- |
| `id` | Long | Identificador interno. |
| `nombre` | String | Nombre o razon social del cliente solicitante. |
| `email` | String | Correo de contacto, unico. |
| `telefono` | String | Telefono de contacto. |
| `documento` | String | CUIT/DNI para facturacion (falta). |
| `direccionFacturacion` | String | Direccion asociada al cliente. |

- **Relaciones**
  - 1:N con `Contenedor` (propietario).
  - 1:N con `Solicitud` (quien la origina).

### Contenedor (Orders)
- **Microservicio**: `orders-service`
- **Campos recomendados**

| Campo | Tipo logico | Descripcion |
| --- | --- | --- |
| `id` | Long | Identificador interno. |
| `codigo` | String | Identificador unico visible para el cliente. |
| `peso` | BigDecimal | Peso total declarado en kg. |
| `volumen` | BigDecimal | Volumen en m3. |
| `estado` | Enum (`BORRADOR`, `PROGRAMADA`, etc.) | Estado operativo derivado del flujo logistico. |
| `tipoCarga` | Enum/String | Granel, palletizado, refrigerado, etc. (sugerido por enunciado). |
| `requisitosEspeciales` | String | Observaciones (temperatura, documentacion). |
| `cliente` | Cliente | Propietario. |
| `solicitudActiva` | Solicitud | Referencia a la solicitud vigente. |
| `origenLat/Lng`, `destinoLat/Lng` | Double | Coordenadas del punto de retiro y entrega (para derivar rutas). |

- **Relaciones**
  - N:1 con `Cliente`.
  - 1:1 con `Solicitud` activa.

### Solicitud (Orders)
- **Microservicio**: `orders-service`
- **Campos recomendados**

| Campo | Tipo logico | Descripcion |
| --- | --- | --- |
| `id` | Long | Identificador. |
| `cliente` | Cliente | Solicitante. |
| `contenedor` | Contenedor | Contenedor asociado (unico). |
| `origenNombre/Direccion/Lat/Lng` | String/Double | Detalle del punto de retiro. |
| `destinoNombre/Direccion/Lat/Lng` | String/Double | Detalle del punto de entrega. |
| `costoEstimado` | BigDecimal | Estimacion inicial. |
| `tiempoEstimadoMinutos` | Long | Tiempo estimado del servicio. |
| `costoFinal` | BigDecimal | Resultado final entregado por Logistics. |
| `tiempoRealMinutos` | Long | Duracion real. |
| `estadiaEstimada` | BigDecimal | Dias de deposito estimados. |
| `observaciones` | String | Comentarios internos. |
| `fechaCreacion`, `updatedAt`, `updatedBy` | Timestamp/String | Auditoria. |

- **Relaciones**
  - 1:1 con `Contenedor`.
  - 1:1 con `Ruta` (logistics) via `solicitudId`.

### Deposito (Logistics)
- **Microservicio**: `logistics-service`
- **Campos recomendados**

| Campo | Tipo logico | Descripcion |
| --- | --- | --- |
| `id` | Long | Identificador del deposito. |
| `nombre` | String | Nombre comercial. |
| `direccion` | String | Direccion postal. |
| `lat/lng` | Double | Coordenadas para distance-client. |
| `costoEstadiaDia` | BigDecimal | Costo diario imputable al tramo. |
| `capacidadMaxima` | BigDecimal | Capacidad opcional (kg/m3). |
| `contacto` | String | Informacion de responsable (sugerido). |

- **Relaciones**
  - Utilizado por `Tramo` (referencias cuando el origen/destino es un deposito).

### Ruta (Logistics)
- **Microservicio**: `logistics-service`
- **Campos recomendados**

| Campo | Tipo logico | Descripcion |
| --- | --- | --- |
| `id` | Long | Identificador. |
| `solicitudId` | Long | Referencia a la solicitud en Orders. |
| `cantTramos` | Integer | Cantidad de tramos planificados. |
| `cantDepositos` | Integer | Cantidad de depositos intermedios. |
| `costoTotalAprox` | BigDecimal | Estimacion de costo total (incluye cargo gestion). |
| `costoTotalReal` | BigDecimal | Resultado real. |
| `pesoTotal` | BigDecimal | Peso consolidado. |
| `volumenTotal` | BigDecimal | Volumen consolidado. |
| `estado` | Enum | PROYECTADA / PLANIFICADA / EN_EJECUCION / FINALIZADA (falta en modelo). |
| `createdAt/updatedAt` | Timestamp | Auditoria. |

- **Relaciones**
  - 1:N con `Tramo`.
  - 1:1 con `Solicitud` (via `solicitudId`).

### Tramo (Logistics)
- **Microservicio**: `logistics-service`
- **Campos recomendados**

| Campo | Tipo logico | Descripcion |
| --- | --- | --- |
| `id` | Long | Identificador. |
| `ruta` | Ruta | Ruta de pertenencia. |
| `orden` | Integer | Secuencia del tramo dentro de la ruta. |
| `origenTipo/destinoTipo` | Enum (`SOLICITUD`, `DEPOSITO`, `PUNTO_INTERMEDIO`) | Indica si es retiro, deposito, entrega. |
| `origenId/destinoId` | Long | FK al deposito u otra entidad. |
| `origenLat/Lng`, `destinoLat/Lng` | Double | Coordenadas pre-calculadas. |
| `tipo` | Enum (`TRASLADO`, `ESPERA`) | Clasificacion del tramo. |
| `estado` | Enum (`ESTIMADO`, `ASIGNADO`, `INICIADO`, `FINALIZADO`) | Estado operativo. |
| `camionId` | Long | Camion asignado (referencia a Fleet). |
| `pesoCarga`, `volumenCarga` | BigDecimal | Valores especificos por tramo (falta). |
| `distanciaKmEstimada/distanciaKmReal` | Double | Distancias. |
| `costoAprox/costoReal` | BigDecimal | Costos monetarios. |
| `costoEstadiaDia/costoEstadia` | BigDecimal | Costos de deposito. |
| `fechaHoraInicioEstimada/finEstimada` | Timestamp | Planificacion. |
| `fechaHoraInicio/fin` | Timestamp | Ejecucion real. |
| `diasEstadia` | Integer | Permanencia en deposito. |
| `updatedAt/updatedBy` | Timestamp/String | Auditoria. |

- **Relaciones**
  - N:1 con `Ruta`.
  - N:1 con `Camion` (Fleet) via `camionId`.
  - N:1 con `Deposito` (cuando tipo = deposito).

### Camion (Fleet)
- **Microservicio**: `fleet-service`
- **Campos recomendados**

| Campo | Tipo logico | Descripcion |
| --- | --- | --- |
| `id` | Long | Identificador. |
| `dominio` | String | Patente unica. |
| `transportistaNombre` | String | Nombre del transportista / empresa. |
| `telefono` | String | Contacto. |
| `capPeso` | BigDecimal | Capacidad maxima en kg. |
| `capVolumen` | BigDecimal | Capacidad maxima m3. |
| `disponible` | Boolean | Disponible para asignacion. |
| `costoKmBase` | BigDecimal | Costo por km. |
| `consumoLKm` | BigDecimal | Consumo (l/km). |
| `tipo` | Enum/String | Categoria del camion (liviano, mediano, pesado). |
| `propietarioId` | Long | Referencia a transportista (si aplica). |

- **Relaciones**
  - Referenciado por `Tramo` (via `camionId`).
  - Consultado por `FleetMetrics` para promedios.

### Tarifa (Fleet)
- **Microservicio**: `fleet-service`
- **Campos recomendados**

| Campo | Tipo logico | Descripcion |
| --- | --- | --- |
| `id` | Long | Identificador. |
| `tipo` | String/Enum | `CARGO_GESTION_POR_TRAMO`, `PRECIO_COMBUSTIBLE`, etc. |
| `valor` | BigDecimal | Valor asociado. |
| `moneda` | String | Moneda (ARS, USD) - falta. |
| `vigenciaDesde/Hasta` | Date | Periodo de validez (sugerido). |

- **Relaciones**
  - Consumido por Logistics/Orders para calculos.

## 2. Comparacion con entidades actuales

### Cliente (`orders-service/src/main/java/com/tpibackend/orders/model/Cliente.java`)

| Aspecto | Situacion actual | Accion sugerida |
| --- | --- | --- |
| Identificacion fiscal | No existe `documento` ni datos de facturacion. | Agregar campos `documento` y `direccionFacturacion`. |
| Relaciones | Relacion 1:N con contenedores/solicitudes correcta. | Mantener. |

### Contenedor (`orders-service/src/main/java/com/tpibackend/orders/model/Contenedor.java`)

| Aspecto | Situacion actual | Accion sugerida |
| --- | --- | --- |
| Coordenadas origen/destino | No se almacenan, solo peso/volumen. | Agregar campos `origenLat/Lng`, `destinoLat/Lng` y descripciones para derivar rutas. |
| Atributos de carga | No hay tipo de carga ni requisitos especiales. | Agregar `tipoCarga`, `requisitosEspeciales`. |
| Estado | Enum presente (`ContenedorEstado`). | Mantener como fuente de verdad del seguimiento. |

### Solicitud (`orders-service/src/main/java/com/tpibackend/orders/model/Solicitud.java`)

| Aspecto | Situacion actual | Accion sugerida |
| --- | --- | --- |
| Datos de origen/destino | No existen columnas a pesar de figurar en DTOs y DER (`initdb/02-orders-schema.sql`). | Reincorporar campos de direccion y coordenadas para persistir informacion completa. |
| Campos economicos | `costoEstimado`, `tiempoEstimadoMinutos`, `costoFinal`, `tiempoRealMinutos`, `estadiaEstimada` ya existen. | Mantener y asegurar que se actualicen desde Logistics. |
| Auditoria | `updatedAt/updatedBy` presentes. | Mantener. |
| Historial | `SolicitudEvento` eliminado. Seguimiento derivado de Contenedor/Ruta. | Documentar que el tracking usa `estadoContenedor` + `ruta`. |

### Deposito (`logistics-service/src/main/java/com/tpibackend/logistics/model/Deposito.java`)

| Aspecto | Situacion actual | Accion sugerida |
| --- | --- | --- |
| Campos obligatorios | Nombre, direccion, lat/lng, costo de estadia presentes. | OK. |
| Capacidad | No se modela capacidad maxima. | Evaluar agregar `capacidadPeso/Volumen` si enunciado lo requiere. |

### Ruta (`logistics-service/src/main/java/com/tpibackend/logistics/model/Ruta.java`)

| Aspecto | Situacion actual | Accion sugerida |
| --- | --- | --- |
| Estado de ruta | No existe campo `estado`. | Agregar `estado` para distinguir planificada/en ejecucion/completada. |
| Asociacion con solicitud | `solicitudId` simple (sin FK). | Mantener pero reforzar validando existencia en Orders via eventos. |
| Cargo de gestion | Se calcula en servicio, no se persiste. | Documentar calculo y considerar almacenar `cargoGestionAplicado`. |

### Tramo (`logistics-service/src/main/java/com/tpibackend/logistics/model/Tramo.java`)

| Aspecto | Situacion actual | Accion sugerida |
| --- | --- | --- |
| Orden del tramo | No hay campo `orden`. | Agregar entero para garantizar secuencia. |
| Peso/volumen por tramo | No se persisten; se calcula a partir de ruta. | Agregar para validar asignacion de camion. |
| Auditoria | `updatedAt/updatedBy` presentes. | OK. |
| Relacion con camiones | Solo se guarda `camionId`. | OK, pero reforzar validaciones en servicio. |

### Camion (`fleet-service/src/main/java/com/tpibackend/fleet/model/entity/Camion.java`)

| Aspecto | Situacion actual | Accion sugerida |
| --- | --- | --- |
| Campos principales | Dominio, transportista, capacidades, costos presentes. | Correcto. |
| Tipo de camion | No se especifica categoria. | Agregar `tipo` o `clase` segun enunciado. |
| Propietario/identificador externo | No se modela. | Agregar si se requiere distinguir flota propia vs tercerizada. |

### Tarifa (`fleet-service/src/main/java/com/tpibackend/fleet/model/entity/Tarifa.java`)

| Aspecto | Situacion actual | Accion sugerida |
| --- | --- | --- |
| Moneda y vigencia | No se controla moneda ni periodo. | Agregar `moneda`, `vigenciaDesde`, `vigenciaHasta` para soportar ajustes. |
| Tipo | String libre, unico. | Mantener pero migrar a Enum centralizado para evitar errores tipograficos. |

## 3. Checklist de cambios concretos en el codigo

1. **Orders - Cliente**
   - Agregar campos `documento` y `direccionFacturacion` a `Cliente` (`orders-service/src/main/java/com/tpibackend/orders/model/Cliente.java`) y al `ClienteResponseDto`.

2. **Orders - Contenedor**
   - En `Contenedor` (`orders-service/src/main/java/com/tpibackend/orders/model/Contenedor.java`), agregar campos `origenNombre`, `origenLat`, `origenLng`, `destinoNombre`, `destinoLat`, `destinoLng`, `tipoCarga`, `requisitosEspeciales`.
   - Ajustar DTOs y repositorios para persistir estos valores.

3. **Orders - Solicitud**
   - Reincorporar los campos de origen/destino presentes en `initdb/02-orders-schema.sql` (origen/destino + coordenadas).
   - Actualizar `SolicitudMapper`, `SolicitudResponseDto`, `SolicitudCreateRequest` para exponerlos.

4. **Orders - Seguimiento**
   - Extender `SeguimientoResponseDto` para incluir `estadoContenedor` y `ruta` (ya se expone) y prever `ultimoTramo`.
   - Documentar en `SolicitudServiceImpl` como se derivara el estado usando datos del contenedor y Logistics; dejar `TODO` para integrar con tramos detallados.

5. **Logistics - Ruta**
   - Agregar campo `estado` en `Ruta` con enum dedicado; exponerlo en `RutaResponse` y persistir en DB/migraciones.

6. **Logistics - Tramo**
   - Agregar campo `orden` (Integer) y `pesoCarga`/`volumenCarga` a `Tramo`.
   - Guardar `camionCapacidadVerificada` o al menos `pesoValidado/volumenValidado` para auditoria.

7. **Logistics - Deposito**
   - Evaluar agregar `capacidadMaxima` si se requiere validar cupos; de lo contrario documentar que la disponibilidad se gestiona fuera del modelo.

8. **Fleet - Camion**
   - Agregar campo `tipo` (`Enum CamionTipo`) para reflejar categorias del enunciado (liviano, mediano, pesado).
   - Incorporar `propietarioId` o `empresa` si se necesita distinguir transportistas.

9. **Fleet - Tarifa**
   - Agregar campos `moneda`, `vigenciaDesde`, `vigenciaHasta`.
   - Convertir `tipo` a Enum o tabla de referencia para evitar duplicados.

10. **SQL/initdb**
    - Actualizar `initdb/02-orders-schema.sql` con los campos agregados a `solicitudes/ contenedores / clientes`.
    - Agregar migraciones equivalentes (Flyway/Liquibase) si se usan en cada microservicio.

## 4. Verificacion de reglas de negocio en el modelo

| Regla | Donde validarla | Detalle |
| --- | --- | --- |
| Capacidad de camion (peso/volumen) | Servicio de dominio `TramoService` (`logistics-service`) + constraints logicas en `FleetClient` | Al asignar un camion, comparar `pesoCarga` y `volumenCarga` del tramo vs `capPeso` y `capVolumen` del camion. Considerar constraint o check en DB si se persiste `pesoCarga`. |
| Calculo de costos (tarifas, combustible, estadia) | `RutaService`/`TramoService` en Logistics + datos de `Tarifa` | Actualizar `Ruta`/`Tramo` con cargo de gestion, costo combustible (`consumoLKm * precioCombustible`) y costo estadia (`diasEstadia * costoEstadiaDia`). Las tarifas se obtienen via Fleet; se pueden complementar con vistas/materialized views para auditoria. |
| Estados de contenedores y solicitudes | `EstadoService` (Orders) y `TramoService` (Logistics) | Orders deriva el estado visible de la solicitud desde `ContenedorEstado`. Logistics maneja `TramoEstado` y debe emitir eventos/asincronos al finalizar cada tramo. Usar listeners (eventos de dominio o mensajeria) para propagar cambios y evitar endpoints manuales. |
| Estados de ruta/tramo | `TramoService` y `RutaService` | Agregar `estado` en `Ruta` y reutilizar `TramoEstado` existente. Se recomienda publicar eventos (`tramo-iniciado`, `tramo-finalizado`) y dejar reglas en servicios, no en DB. |
| Validacion de tarifas | `TarifaController` / `TarifaService` | Anadir reglas en servicios (p.ej. no permitir tarifas duplicadas por tipo/moneda/vigencia). Se puede reforzar en DB con constraint UNIQUE (`tipo`, `moneda`, `vigencia_desde`). |

**Recomendacion general**: mantener las reglas criticas (capacidad, estados, costos) en los servicios de dominio donde existe contexto completo. Usar listeners/eventos para sincronizar Orders ↔ Logistics ↔ Fleet, y solo recurrir a constraints de base para asegurar integridad estructural (uniqueness, not null, foreign keys).
