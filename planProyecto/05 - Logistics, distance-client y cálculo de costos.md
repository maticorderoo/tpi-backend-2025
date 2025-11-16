05 - Logistics, distance-client y cálculo de costos
1. Flujos de uso de distance-client
1.1 Calcular rutas tentativas y tramos (planificación)
Orders solicita estimación: orders-service llama a POST /api/logistics/routes/estimaciones/distancia para obtener distancia/tiempo entre origen y destino (ya implementado en LogisticsClient.estimarDistancia y RutaService.estimarDistancia, pero sin validar inputs rich).
Operador arma ruta: en Logistics, RutaService.crearRuta recibe origen, destino y depósitos intermedios. Flujo ideal:
Para cada punto consecutivo (origen → depósito 1, depósito 1 → depósito 2, ..., último depósito → destino), invocar DistanceClient.getDistance.
Persistir distanciaKmEstimada, tiempo estimado en cada Tramo.
Calcular cantTramos, cantDepositos, costos aproximados (ver sección 2) y guardar en Ruta.
Estado actual:
RutaService ya llama a distanceClient.getDistance por tramo, pero no persiste tiempo estimado ni valida fallbacks coherentes; cuando falla, usa 0 km.
No hay DTO de respuesta que devuelva tiempos estimados por tramo al front/postman.
1.2 Calcular recorrido total (sumatoria de tramos)
Ideal:
Tras crear la ruta, sumar distanciaKmEstimada de cada tramo para obtener distanciaTotalEstimada. Guardarlo en Ruta y devolverlo en RutaResponse.
Para costos: usar calcularCostoTotalAprox() + cargoGestion.
Estado actual:
Ruta.calcularCostoTotalAprox() existe y se usa. No se calcula distanciaTotalEstimada ni se expone en DTOs.
No se guarda tiempoEstimado a nivel ruta.
1.3 Recalcular costos reales al finalizar tramos
Ideal:
TramoService.finalizarTramo debe:
Recalcular distanciaKmReal con distanceClient.getDistance(origenLat, origenLng, destinoLat, destinoLng) si no se provee kmReal.
Calcular costo real por tramo: costoKMBase * distanciaReal + consumoLKm * distanciaReal * precioCombustible + costoEstadia.
Actualizar fechaHoraFin, diasEstadia, costoEstadia.
Marcar camión como disponible (ya ocurre) y publicar evento a Orders para actualizar costo final.
Al finalizar la ruta (todos los tramos FINALIZADO), recalcular costoTotalReal en Ruta y notificar Orders.
Estado actual:
finalizarTramo ya recalcula costos usando datos del request y DistanceClient, pero:
No siempre se usan promedios reales de Fleet (usas request fields costoKmBase, consumoLitrosKm, precioCombustible, sin validar vs Fleet).
No se registra tiempoRealMinutos ni fechaHoraFinEstimada.
Notificación a Orders es directa vía REST (debe migrar a eventos).
2. Diseño del cálculo de costos
2.1 Costo estimado (planificación)
Pseudo-pasos (dentro de RutaService.crearRuta):

para cada tramo estimado:
    distance = distanceClient.getDistance(origen, destino)
    km = distance.distanceKm
    tiempoMinutos = distance.durationMinutes
    agregar a Tramo: distanciaKmEstimada = km, fechaHoraInicioEstimada/Fin con proyección

    # obtener valores promedio
    metrics = fleetMetricsClient.getFleetAverages()  # via Orders o directo
    costoKM = metrics.costoKmPromedio
    consumo = metrics.consumoPromedio
    precioCombustible = tarifa("PRECIO_COMBUSTIBLE")
    cargoGestion = tarifa("CARGO_GESTION_POR_TRAMO")

    costoAproxTramo = km * costoKM + km * consumo * precioCombustible + (diasEstadiaEstimados * deposito.costoEstadiaDia)
    acumular costoAproxTramo
costoRuta = suma(costoAproxTramo) + cargoGestion * cantTramos
guardar en Ruta.costoTotalAprox
Ubicación: RutaService (para setear valores estimados en Ruta y Tramo).
Fuente de datos: DistanceClient, FleetMetrics (para promedios), TarifaService (para cargos y combustible).
Checks: validar que distanceClient devuelva datos; si falla, registrar error y marcar ruta como incompleta.
2.2 Costo real (ejecución)
Pseudo-pasos (en TramoService.finalizarTramo):

distanceReal = (request.kmReal != null) ? request.kmReal : distanceClient.getDistance(origenCoord, destinoCoord).km
camion = fleetClient.obtenerCamion(tramo.camionId) -> obtener costoKmBase, consumoLKm (reales)
tarifaCombustible = tarifa("PRECIO_COMBUSTIBLE") vigente
costoKM = camion.costoKmBase
consumo = camion.consumoLKm
diasEstadia = request.diasEstadia (validar con depósitos)

costoEstadia = diasEstadia * deposito.costoEstadiaDia
costoRealTramo = (distanceReal * costoKM) + (distanceReal * consumo * tarifaCombustible) + costoEstadia

actualizar tramo: distanciaKmReal, costoReal, fechaHoraFin, diasEstadia, estado FINALIZADO
Ruta.costoTotalReal = suma(costoReal)
si todos los tramos finalizados:
    publicar evento a Orders con costo total y tiempos reales
Ubicación: TramoService para cálculo por tramo; RutaService o TramoService para actualizar ruta al cerrar todos los tramos.
Dependencias: FleetClient (datos reales del camión), TarifaService, DistanceClient.
Auditoría: guardar fechaHoraInicio/Fin, updatedBy, motivo.
3. Checklist de cambios en Logistics y distance-client
Distance-client & Logistics integration

 Validar entradas en RutaService.estimarDistancia: rechazar strings vacías, normalizar direcciones.
 Crear DTO (EstimacionDistanciaResponse) que incluya duracionMinutos y usarlo para setear tiempos estimados en Tramo.
 Manejar fallback de distanceClient (si falla, registrar error y usar distancia previa, no 0).
 Documentar en distance-client cómo se registran modos de transporte (solo driving).
 Confirmar que distance-client se usa solo desde Logistics; evitar accesos directos desde Orders (ya migrado).
DTOs y endpoints

 Actualizar logistics-service DTOs (RutaResponse, TramoResponse) para incluir distanciaKmEstimada, tiempoEstimado, distanciaKmReal, costoAprox, costoReal.
 Incluir estado de ruta y orden de tramos en respuestas.
Servicios en Logistics

 En RutaService.crearRuta, luego de cada distanceClient.getDistance, guardar fechaHoraInicioEstimada/FinEstimada (usando duración).
 Implementar método calcularDistanciaTotal() en Ruta y exponerlo.
 Al finalizar todos los tramos, recalcular costoTotalReal y tiempoReal de la ruta y notificar Orders (preferentemente vía evento).
 Garantizar que TramoService.asignarCamion consulte Fleet para obtener capPeso y capVolumen (ya se hace) y, adicionalmente, guardar esos datos en el tramo para auditoría.
Tarifas y métricas

 Crear cliente interno en Logistics para consultar FleetMetrics y Tarifa (si aún no existe) o definir endpoints en Fleet para obtener datos necesarios.
 Al calcular costos aproximados, usar FleetMetrics promedio; al calcular costos reales, usar datos del camión y la tarifa vigente de combustible.
 Almacenar cargoGestionPorTramo aplicado en Ruta para trazabilidad.
Registros de dates/times

 Asegurar que Tramo siempre persista fechaHoraInicio, fechaHoraFin, fechaHoraInicioEstimada y fechaHoraFinEstimada.
 Validar que un tramo finalizado no pueda ser modificado (check en servicio + estado).
Distance-client settings

 Revisar configuración de timeouts, reintentos y logging (ya definidos en librería). Documentar cómo setear la API key en logistics-service.
 Agregar métricas/logging para detectar fallos recurrentes del provider.
4. Verificación de reglas de negocio afectadas
Regla	Garantía propuesta
Costo final incluye kilómetros, combustible, estadías y cargos por tramo	TramoService.finalizarTramo sumará: km * costoKmBase, km * consumo * precioCombustible, diasEstadia * costoEstadiaDia. Ruta.calcularCostoTotalReal() añade cargoGestionPorTramo * cantTramos. Requiere obtener tarifas de Fleet.
Tiempo estimado se deriva de distancias	Cada llamada a distanceClient.getDistance devuelve durationMinutes; se almacenará en Tramo.fechaHoraInicioEstimada/FinEstimada y se sumará para Ruta. Orders puede usar estos datos para mostrar ETA.
Registro de fechas estimadas/reales en tramos	Tramo ya tiene campos; se debe completar en RutaService y TramoService. Estimadas se calculan en planificación; reales se actualizan al iniciar/finalizar. Validar que se setean siempre y que updatedBy se registra.
Integridad del seguimiento	Logistics será la única fuente de distancias/tiempos (via distance-client). Orders se actualizará mediante eventos cuando cambien los tramos o la ruta, evitando endpoints manuales.
Capacidad del camión	TramoService.asignarCamion ya consulta Fleet; agregar validación para peso/volumen por tramo (no solo de ruta). Guardar pesoCarga y volumenCarga en la entidad.
Tiempo/costo derivan de datos reales	Al finalizar tramos, distanceClient recalcula distancia real si no se provee manualmente, evitando números arbitrarios. Todos los cálculos se centralizan en Logistics, garantizando consistencia.
Con estas acciones, Logistics utilizará coherentemente distance-client para obtener distancias/tiempos, calculará costos aproximados y reales conforme al enunciado y proveerá datos confiables al resto de los microservicios.