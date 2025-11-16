04 - Endpoints y casos de uso
1. Requerimientos ↔ Endpoints
Requerimiento	Microservicio / Endpoint actual	Rol esperado	Estado / Comentarios
Registrar nueva solicitud de transporte	orders-service – POST /api/orders	Cliente	Implementado. Validar campos origen/destino completos según modelo.
Consultar detalle de una solicitud	orders-service – GET /api/orders/{id}	Cliente / Operador	Implementado, devolver datos de contenedor y ruta.
Consultar estado/seguimiento de un contenedor	orders-service – GET /api/orders/{contenedorId}/tracking	Cliente / Operador	Implementado; falta enriquecer con tramos.
Calcular estimación (costo/tiempo) antes de planificar	orders-service – POST /api/orders/{id}/estimacion	Operador	Implementado, debe consumir Logistics y Fleet. Validar payload.
Confirmar costo/tiempo final cuando Logistics finaliza	orders-service – PUT /api/orders/{id}/costo	Operador / Servicio interno	Implementado; debería recibir eventos desde Logistics en vez de consumo manual.
Actualizar estado del contenedor manualmente	orders-service – POST /api/orders/{id}/estado	Operador	Sobrante: Contradice diseño basado en eventos logísticos. Marcar para eliminar cuando Logistics publíque eventos.
Consultar lista de solicitudes del cliente	(no endpoint dedicado; se usa GET /api/orders/{id} individual)	Cliente	PENDIENTE: agregar GET /api/orders?clienteId= para front web.
CRUD de depósitos (operador)	logistics-service – POST/GET/PUT/DELETE /api/logistics/depositos	Operador	Implementado.
Planificar ruta (crear)	logistics-service – POST /api/logistics/routes	Operador	Implementado. Debe validar datos de contenedores.
Consultar rutas	logistics-service – GET /api/logistics/routes, GET /api/logistics/routes/{id}	Operador	Implementado.
Obtener ruta por solicitud	logistics-service – GET /api/logistics/routes/solicitudes/{solicitudId}	Operador / Orders	Implementado (usado vía LogisticsClient).
Asignar ruta a solicitud	logistics-service – POST /api/logistics/routes/{rutaId}/asignaciones	Operador	Implementado; hoy actualiza Orders vía REST, debe pasar a eventos.
Obtener rutas tentativas/estimación de distancia	logistics-service – POST /api/logistics/routes/estimaciones/distancia	Operador / Orders	Implementado (Orders la consume).
Listar contenedores pendientes en depósitos	logistics-service – GET /api/logistics/containers/pendientes	Operador	Fuera de lugar: debería migrarse a Orders.
Listar tramos por camión	logistics-service – GET /api/logistics/tramos?camionId=	Operador / Transportista	Revisar implementación (métodos en TramoService).
Asignar camión a tramo	logistics-service – POST /api/logistics/tramos/{id}/asignaciones	Operador	Implementado; valida capacidad con Fleet.
Registrar inicio de tramo	logistics-service – POST /api/logistics/tramos/{id}/inicios	Transportista	Implementado (requiere rol TRANSPORTISTA).
Registrar fin de tramo	logistics-service – POST /api/logistics/tramos/{id}/finalizaciones	Transportista	Implementado. Debe actualizar costos y notificar Orders.
Consultar camiones disponibles	fleet-service – GET /api/fleet/trucks	Operador / Transportista	Implementado con filtro disponible.
Registrar/editar camión	fleet-service – POST /api/fleet/trucks, PUT /api/fleet/trucks/{id}	Operador	Implementado.
Actualizar disponibilidad de camión	fleet-service – PUT /api/fleet/trucks/{id}/disponibilidad	Operador	Implementado; Logistics lo usa vía cliente HTTP.
Consultar tarifas / registrar tarifas	fleet-service – GET/POST /api/fleet/tarifas	Operador	Implementado.
Obtener métricas promedio (costo km/consumo)	fleet-service – GET /api/fleet/metrics/promedios	Operador / Orders	Implementado (usado por Orders).
Autenticación / gestión de usuarios	Keycloak externo	Cliente / Operador / Transportista	Fuera del alcance del backend Java.
2. Endpoints sobrantes o inconsistentes
Endpoint	Problema	Acción propuesta
orders-service – POST /api/orders/{id}/estado	Permite mutar estados manualmente, rompe la regla de que el contenedor refleja los tramos logísticos.	Deprecar/eliminar tras implementar eventos Logistics→Orders.
logistics-service – GET /api/logistics/containers/pendientes y servicio asociado	Logistics no debería exponer consultas de contenedores; es responsabilidad de Orders.	Mover funcionalidad a orders-service (consultando tramos y estado contenedor) y eliminar del micro de logística.
logistics-service – Uso directo de OrdersClient.actualizarEstado() y actualizarCosto() en RutaService/TramoService	Rompe bounded contexts (Logistics actualiza datos de Orders vía REST).	Reemplazar por eventos/event streaming; remover endpoints en Orders usados solo para esto.
orders-service – Falta endpoint para listar solicitudes por cliente / filtrar por estado.	Requerido por enunciado para front cliente.	Crear GET /api/orders?clienteId=&estado=.
fleet-service – Ausencia de endpoints para historial de disponibilidad / auditoría.	No crítico pero enunciado menciona seguimiento de flota.	Documentar como fuera de alcance o agregar en backlog.
3. Checklist de refactor por microservicio
Orders-service
Crear
GET /api/orders con filtros (clienteId, estadoContenedor, paginación).
Webhook/endpoint interno POST /api/orders/{id}/eventos-logisticos (consumido por Logistics en etapa interim o mejor un listener Kafka; documentar).
Modificar
POST /api/orders – validar y persistir campos de origen/destino, tipo de carga.
GET /api/orders/{contenedorId}/tracking – enriquecer respuesta con información de tramos (consumir Logistics).
PUT /api/orders/{id}/costo – restringir a llamadas autenticadas desde Logistics (scope interno).
Eliminar / Deprecar
POST /api/orders/{id}/estado – reemplazar por eventos externos.
Cualquier endpoint público que exponga cambios administrativos no alineados al modelo.
Logistics-service
Crear
POST /api/logistics/routes/{rutaId}/confirmaciones – endpoint interno para cerrar ruta y publicar evento (si se continua con REST).
GET /api/logistics/tramos/{id} – detalle de tramo para seguimiento front (si no existe).
Modificar
POST /api/logistics/routes – validar campos adicionales (peso/volumen por tramo, orden).
POST /api/logistics/tramos/{id}/finalizaciones – asegurar que solo camiones asignados puedan finalizar, y publicar evento a Orders/Fleet.
GET /api/logistics/routes/solicitudes/{solicitudId} – devolver estado de ruta y lista de tramos con orden y datos de camión.
Eliminar / Deprecar
GET /api/logistics/containers/pendientes – migrar a Orders.
Cualquier endpoint que actualice Orders directamente (reemplazar por mensajería/eventos).
Duplicados de rutas alias (/routes, /logistics/rutas, etc.) una vez unificada nomenclatura.
Fleet-service
Crear
GET /api/fleet/trucks/{id}/historial-disponibilidad (opcional) para auditoría.
GET /api/fleet/tarifas/{tipo} con vigencia (si se requiere).
Modificar
PUT /api/fleet/trucks/{id} – validar tipo de camión y capacidades conforme al modelo.
GET /api/fleet/metrics/promedios – incluir metadata (cantidad de camiones considerados).
POST /api/fleet/tarifas – requerir moneda, vigencia.
Eliminar / Deprecar
Ninguno obligatorio, pero documentar endpoints internos si se exponían sin rol.
4. Validación de roles y seguridad
Microservicio	Observaciones actuales	Acciones sugeridas
Orders	@PreAuthorize se usa: POST /api/orders (CLIENTE), POST /estimacion y PUT /costo (OPERADOR), GET /tracking (CLIENTE/OPERADOR). Falta restringir endpoints internos que serán consumidos por Logistics (podrían usar rol técnico).	- Mantener distinción CLIENTE/OPERADOR.<br>- Para endpoints internos (ej. actualizaciones desde Logistics), usar scope ROLE_OPERADOR o ROLE_SYSTEM + network restrictions.<br>- Agregar validación para que un cliente solo acceda a sus solicitudes (clienteId en token).
Logistics	La mayoría de controllers usan @PreAuthorize("hasRole('OPERADOR')"); TramoController permite TRANSPORTISTA para inicio/fin. Endpoint de contenedores usa OPERADOR.	- Reforzar que Transportista solo pueda iniciar/finalizar tramos asignados a su camión (validar con Keycloak claims).<br>- Evitar que OPERADOR pueda modificar tramos finalizados (chequeo en servicio).<br>- Proteger estimación de distancia si se expone externamente (solo orders-service via client credentials).
Fleet	CamionController expone GET para OPERADOR/TRANSPORTISTA, PUT disponibilidad exclusivo OPERADOR. MetricsController restringido a OPERADOR tras refactor.	- Exigir ROLE_OPERADOR para POST/PUT de camiones y tarifas.<br>- Revisar si TRANSPORTISTA necesita ver todos los camiones; de lo contrario limitar a los propios.<br>- Evitar que Transportista modifique disponibilidad directamente.
Reglas adicionales

Validar en servicios que no se puedan modificar entidades en estados finales (ej.: ruta finalizada, tramo finalizado, solicitud cancelada). Añadir checks en los servicios y devolver 409 Conflict.
Auditar SecurityConfig en cada micro: asegurar que /actuator/** o /swagger-ui/** sean los únicos públicos; el resto pasa por Gateway/Keycloak.
Documentar en Postman qué headers/roles se necesitan para cada endpoint para facilitar pruebas.
Con este mapa se puede abordar la refactorización de endpoints, la actualización de colecciones Postman y la definición de nuevos contratos entre microservicios.