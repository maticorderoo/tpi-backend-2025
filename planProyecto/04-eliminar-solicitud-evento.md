# 04 - Eliminacion de SolicitudEvento

## 1. Motivacion
- El enunciado del TPI define al contenedor como entidad central; la solicitud solo guarda datos administrativos. Mantener una tabla `solicitud_eventos` duplicaba estados y obligaba a sincronizar manualmente con Logistics.
- El seguimiento real del envio surge de los estados del contenedor y de los tramos planificados/ejecutados; no existe requerimiento de historial administrativo separado.
- Simplificar el dominio nos permite alinear Orders con Logistics/Fleet, reducir acoplamientos y preparar la derivacion futura del seguimiento directamente desde los microservicios operativos.

## 2. Cambios aplicados
- **Dominio y DTOs de Orders**
  - Elimine la entidad `SolicitudEvento`, su DTO `SolicitudEventoResponseDto`, el enum `SolicitudEstado` y el request `SolicitudEstadoUpdateRequest`.
  - Limpie `Solicitud`, `SolicitudResponseDto`, `SeguimientoResponseDto` y el `SolicitudMapper` para que solo expongan cliente, contenedor y ruta.
  - Refactorice `SolicitudServiceImpl` para validar solicitudes activas consultando el estado del contenedor (`ContenedorEstado`), actualizar el seguimiento con la ruta de Logistics y quitar cualquier logica de historial.
  - Actualice `SolicitudRepository`, `SolicitudService`, `EstadoService` y `SolicitudController` (incluyendo ejemplos Swagger) para reflejar que ya no se devuelven eventos administrativos.
- **SQL / initdb**
  - Elimine la definicion y los indices de la tabla `solicitud_eventos` en `initdb/02-orders-schema.sql`.
- **Documentacion**
  - Ajuste `03-bounded-contexts-y-dominio.md`, `TARIFAS_Y_REGLAS_NEGOCIO.md` y `POSTMAN_GUIDE.md` para describir el seguimiento basado en contenedores y tramos.
- **Tests**
  - Actualice `SolicitudControllerTest` y `SolicitudServiceImplTest` para que validen el nuevo modelo (estado del contenedor + ruta). No se requieren asserts sobre eventos.
- **Postman**
  - Elimine la referencia a "historial cronologico de eventos" en la guia; el request de tracking ahora se interpreta como vista del estado del contenedor + ruta.

## 3. Impacto en el sistema
- `mvn -f orders-service/pom.xml test` se ejecuto. Los tests especificos del dominio pasan, pero persiste el error conocido en `com.orders.orders_service.OrdersServiceApplicationTests` porque ese test nunca tuvo una configuracion valida en el proyecto de base (Spring no encuentra una `@SpringBootConfiguration`). Este fallo es preexistente y no esta relacionado con la eliminacion de `SolicitudEvento`.
- Los flujos criticos (crear solicitud, recalcular estimacion, consultar tracking y actualizar estado del contenedor desde Logistics) continuan funcionando: ahora el tracking expone `estadoContenedor` y `ruta`.
- Se dejaron `TODO` documentados en `SolicitudServiceImpl.obtenerSeguimientoPorContenedor` para incorporar mas adelante la informacion detallada de tramos/logistica una vez que se definan los eventos compartidos.
