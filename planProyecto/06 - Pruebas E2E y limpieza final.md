06 - Pruebas E2E y limpieza final
1. Escenarios de prueba end-to-end por rol
Cliente
Login: obtener token en Keycloak (POST /realms/tpi-2025/protocol/openid-connect/token).
Registrar solicitud: POST /api/orders (Gateway u Orders directo) con cliente, contenedor, origen/destino.
Ver detalles de solicitud: GET /api/orders/{solicitudId}.
Consultar tracking: GET /api/orders/{contenedorId}/tracking (debería devolver estado del contenedor + ruta).
Listar solicitudes propias (cuando se implemente): GET /api/orders?clienteId={loggedUser}.
Resultado esperado: solicitud creada en estado BORRADOR/PROGRAMADA, contenedor asociado, tracking mostrando estado actual.
Operador
Login (rol OPERADOR).
Listar solicitudes pendientes: GET /api/orders?estadoContenedor=BORRADOR.
Calcular estimación: POST /api/orders/{id}/estimacion.
Configurar depósitos: POST /api/logistics/depositos.
Planificar ruta: POST /api/logistics/routes con origen/destino/depósitos.
Asignar ruta a solicitud: POST /api/logistics/routes/{rutaId}/asignaciones.
Asignar camión: POST /api/logistics/tramos/{tramoId}/asignaciones.
Actualizar disponibilidad camión (si se hace manualmente): PUT /api/fleet/trucks/{id}/disponibilidad.
Confirmar costo final (cuando Logistics notifique o manualmente por ahora): PUT /api/orders/{id}/costo.
Resultado esperado: ruta creada con tramos, camión asignado, costos estimados calculados, Orders reflejando ruta y costo final.
Transportista
Login (rol TRANSPORTISTA).
Listar tramos asignados: GET /api/logistics/tramos?camionId={camionAsignado} (si existe); o GET /api/logistics/tramos/{id}.
Iniciar tramo: POST /api/logistics/tramos/{id}/inicios (se marca INICIADO, camión queda no disponible).
Finalizar tramo: POST /api/logistics/tramos/{id}/finalizaciones (registro de km reales, fechas, costos).
Ver estado actualizado: GET /api/orders/{contenedorId}/tracking o GET /api/logistics/routes/solicitudes/{solicitudId}.
Resultado esperado: tramos avanzan de ASIGNADO a FINALIZADO, costos y tiempos reales se registran, Orders se actualiza vía evento/rest.
2. Plan de colección Postman/Bruno
TPI-2025.postman_collection
├── 00-Auth-Keycloak
│   ├── Obtener token CLIENTE
│   ├── Obtener token OPERADOR
│   └── Obtener token TRANSPORTISTA
├── 01-Cliente
│   ├── Crear solicitud
│   ├── Obtener solicitud
│   ├── Listar solicitudes (cuando exista)
│   └── Consultar tracking
├── 02-Operador
│   ├── Calcular estimacion
│   ├── CRUD Depositos
│   ├── Crear ruta
│   ├── Asignar ruta a solicitud
│   ├── Asignar camion a tramo
│   ├── Finalizar ruta / actualizaciones
│   └── Actualizar costo final en Orders
├── 03-Transportista
│   ├── Listar tramos asignados
│   ├── Iniciar tramo
│   └── Finalizar tramo
├── 04-Fleet
│   ├── CRUD camiones
│   ├── CRUD tarifas
│   └── Métricas promedios
Variables de entorno:

gateway_url, orders_url, logistics_url, fleet_url.
Credenciales Keycloak (client_id, client_secret si se usa client-credentials, username, password por rol).
token_cliente, token_operador, token_transportista.
Identificadores intermedios (solicitudId, contenedorId, rutaId, tramoId, camionId) actualizados por scripts.
3. Checklist de endpoints/funcionalidades sobrantes
Endpoint / Funcionalidad	Motivo	Acción
POST /api/orders/{id}/estado	No forma parte de flujos E2E; contradice proceso basado en logística.	Eliminar o documentar como fuera de alcance.
logistics-service – GET /api/logistics/containers/pendientes	Responsabilidad de Orders (tracking).	Migrar a Orders y eliminar en Logistics.
logistics-service – uso directo de OrdersClient.actualizarEstado/costo	No se usará después de implementar eventos; no pertenece a un flujo E2E del enunciado.	Reemplazar por mensajería/event bridge; quitar cliente REST directo.
Endpoints duplicados (/routes, /logistics/rutas, alias legacy)	Confusión en API y Postman; no obligatorios para TPI.	Deprecar alias antiguos tras actualizar Gateway/Postman.
Cualquier script/manual para setear estado sin pasar por tramos (ej. fix-tramos-endpoints.js)	Scripts de workaround, no parte del deliverable.	Documentar como herramientas de soporte o eliminar.
4. Resumen final de cumplimiento del enunciado
Requisito / Regla	Cumplimiento
Arquitectura de microservicios (Orders, Logistics, Fleet, Gateway, Keycloak)	Cada micro maneja su dominio: Orders (solicitudes, clientes, contenedores), Logistics (rutas, tramos, depósitos, distance-client), Fleet (camiones, tarifas, métricas). Integración vía HTTP con Gateway o eventos.
Uso de Keycloak (roles Cliente, Operador, Transportista)	Endpoints anotados con @PreAuthorize. Colección Postman proveerá tokens por rol. Necesario validar claims adicionales (cliente solo ve sus solicitudes).
Uso de API externa/distance-client (Google Maps)	Logistics encapsula distance-client, expone un endpoint interno para Orders y usa la librería para planificar y recalcular tramos. Requiere completar persistencia de tiempos/tiempos reales.
Registrar solicitudes y seguimiento	Cliente puede crear solicitud, ver estimación (cuando operador la calcule) y consultar tracking. Tracking debe combinar estadoContenedor + datos de ruta/tramos.
Planificación logística y ejecución	Operador: crea depósitos, rutas, asigna camiones. Transportista: inicia/finaliza tramos. Logistics recalcula costos y notifica Orders.
Cálculo de costos	Estimaciones (Fleet Metrics + distance-client), costos reales (camión, tarifas combustible, estadía) en TramoService. Falta completar integración con Fleet para tarifas y eventos a Orders.
Validación de capacidades	TramoService.asignarCamion ya consulta Fleet; se reforzará con campos de carga por tramo.
Seguimiento end-to-end	Escenarios definidos para cada rol; colección Postman organizara flujos completos.
Limpieza de funcionalidades	Se identificaron endpoints manuales y consultas cruzadas a eliminar o mover. Documentar qué queda fuera de alcance.
Con estas definiciones se completa la planificación de pruebas E2E, la estructura de la colección de APIs y la lista final de deudas técnicas a resolver para dejar el sistema alineado al enunciado del TPI 2025.