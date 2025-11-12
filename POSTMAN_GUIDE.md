# 📬 Guía de Uso de Postman - TPI Backend 2025

## 📋 Archivos Incluidos

- **TPI-2025-COMPLETE.postman_collection.json** - Colección completa con todos los endpoints
- **TPI-2025.gateway-dev.postman_environment.json** - Environment para usar con API Gateway (puerto 8081)
- **TPI-2025.local-dev.postman_environment.json** - Environment para usar servicios directamente (puertos 8082/8083/8084)

## 🚀 Configuración Inicial

### 1. Importar en Postman

1. Abrir Postman
2. Click en **Import**
3. Seleccionar los 3 archivos JSON
4. Verificar que se importaron correctamente

### 2. Seleccionar Environment

En la esquina superior derecha de Postman, seleccionar:
- **TPI Backend 2025 - gateway-dev** (recomendado) - Todo va por el gateway en puerto 8081
- **TPI Backend 2025 - local-dev** - Acceso directo a cada microservicio

## 🔐 Autenticación

### Usuarios Disponibles

La colección incluye 3 tipos de usuarios:

| Usuario | Contraseña | Rol | Permisos |
|---------|------------|-----|----------|
| cliente01 | cliente123 | CLIENTE | Crear solicitudes, ver tracking |
| operador01 | operador123 | OPERADOR | Gestión completa del sistema |
| transportista01 | trans123 | TRANSPORTISTA | Iniciar/finalizar tramos asignados |

### Cómo Autenticarse

1. Ir a la carpeta **🔐 Authentication**
2. Ejecutar el request según el rol que necesites:
   - **Login - Cliente**
   - **Login - Operador**
   - **Login - Transportista**
3. El token JWT se guarda automáticamente en la variable `jwt_token`
4. Todos los demás requests usan este token automáticamente

## 📂 Estructura de la Colección

### 🔐 Authentication
- Login para cada rol
- Los tokens se guardan automáticamente

### 📦 Orders Service
- **Crear Solicitud** - Crea cliente, contenedor y solicitud (CLIENTE)
- **Obtener Solicitud** - Ver detalles completos
- **Tracking** - Seguimiento público del contenedor
- **Calcular Estimación** - Usa distance-client y métricas de flota (OPERADOR)
- **Actualizar Costo Final** - Cuando se completa la entrega (OPERADOR)
- **Cambiar Estado** - Override manual de estados (OPERADOR)

### 🚛 Fleet Service

#### Camiones
- **Crear Camión** - Alta de nuevo vehículo (OPERADOR)
- **Listar Todos** - Todos los camiones
- **Listar Disponibles** - Solo libres para asignar
- **Listar Ocupados** - Solo en uso
- **Obtener por ID** - Detalle de un camión
- **Actualizar** - Modificar datos (OPERADOR)

#### Tarifas
- **Crear Tarifa** - Ej: CARGO_GESTION_POR_TRAMO (OPERADOR)
- **Listar Tarifas** - Ver todas las configuradas
- **Obtener por Tipo** - Buscar tarifa específica
- **Actualizar Tarifa** - Modificar valor (OPERADOR)

#### Métricas
- **Obtener Promedios** - Consumo y costo promedio de la flota

### 🗺️ Logistics Service

#### Depósitos
- **Crear Depósito** - Alta con coordenadas (OPERADOR)
- **Listar Depósitos** - Todos los puntos intermedios
- **Obtener por ID** - Detalle de un depósito
- **Actualizar** - Modificar datos (OPERADOR)

#### Rutas
- **Crear Ruta** - Genera tramos usando distance-client (OPERADOR)
- **Obtener Ruta** - Ver ruta completa con tramos
- **Asignar a Solicitud** - Vincular ruta con pedido (OPERADOR)

#### Tramos
- **Asignar Camión** - Valida capacidad y disponibilidad (OPERADOR)
- **Iniciar Tramo** - Marca inicio del viaje (TRANSPORTISTA)
- **Finalizar Tramo** - Recalcula distancia real (TRANSPORTISTA)
- **Listar por Camión** - Ver tramos asignados a un transportista
- **Contenedores en Depósito** - Ver qué está esperando asignación

### 🔄 Flujo End-to-End Completo

Secuencia de 15 pasos que simula el flujo completo:

1. **Login Operador** - Autenticarse como operador
2. **Crear Depósito** - Rosario como punto intermedio
3. **Crear Camión** - Dar de alta vehículo
4. **Crear Tarifa Gestión** - Configurar cargo por tramo
5. **Login Cliente** - Cambiar a rol cliente
6. **Crear Solicitud** - Buenos Aires → Córdoba
7. **Login Operador** - Volver a operador
8. **Calcular Estimación** - Costo y tiempo usando APIs
9. **Crear Ruta** - Con depósito intermedio
10. **Asignar Ruta a Solicitud** - Vincular
11. **Asignar Camión a Tramo** - Con validación
12. **Login Transportista** - Cambiar a transportista
13. **Iniciar Tramo** - Comenzar viaje
14. **Finalizar Tramo** - Terminar con distancia real
15. **Ver Tracking** - Verificar historial

## 🎯 Variables de Environment

Las siguientes variables se auto-gestionan durante el flujo:

| Variable | Descripción | Origen |
|----------|-------------|--------|
| `jwt_token` | Token de autenticación JWT | Login requests |
| `refresh_token` | Para renovar el token | Login requests |
| `solicitud_id` | ID de la solicitud creada | Crear Solicitud |
| `contenedor_id` | ID del contenedor | Crear Solicitud |
| `ruta_id` | ID de la ruta | Crear Ruta |
| `tramo_id` | ID del primer tramo | Crear Ruta |
| `camion_id` | ID del camión | Crear Camión |
| `deposito_id` | ID del depósito | Crear Depósito |
| `tarifa_id` | ID de la tarifa | Crear Tarifa |

## ✅ Casos de Prueba Importantes

### 1. Validación de Capacidad
```
POST /api/logistics/tramos/{{tramo_id}}/asignar
```
- Enviar un camión con capacidad menor al contenedor
- Debe retornar error 400 con mensaje de capacidad insuficiente

### 2. Integración con Distance Client
```
POST /api/logistics/rutas
```
- La ruta debe calcular automáticamente las distancias entre puntos usando Google Maps

### 3. Cálculo de Costo con Fórmula Completa
```
POST /api/orders/{{solicitud_id}}/estimacion
```
- Verifica que el costo incluya: km × costoBase + combustible + estadía

### 4. Estados del Contenedor
```
GET /api/orders/{{solicitud_id}}/tracking
```
- Debe mostrar historial cronológico de eventos
- Estados: BORRADOR → PROGRAMADA → EN_RETIRO → EN_VIAJE → EN_DEPOSITO → ENTREGADO

### 5. Seguridad por Roles
- Intentar crear camión con token de CLIENTE → debe dar 403 Forbidden
- Tracking debe funcionar sin token (público)
- Solo TRANSPORTISTA puede iniciar/finalizar tramos

## 🐛 Troubleshooting

### Error 401 Unauthorized
- Verificar que ejecutaste el Login correspondiente
- El token expira en 5 minutos, volver a hacer login

### Error 403 Forbidden
- Estás usando un rol incorrecto para ese endpoint
- Ejemplo: CLIENTE no puede crear camiones

### Error 404 Not Found
- Verificar que las variables tienen valores (no están vacías)
- Ejecutar los requests en orden para poblar las variables

### Error 500 Internal Server Error
- Verificar que los servicios estén corriendo:
  ```bash
  docker-compose ps
  ```
- Revisar logs:
  ```bash
  docker-compose logs -f [service-name]
  ```

## 📊 Testing Recomendado

### Smoke Test Rápido (5 min)
1. Login Operador
2. Crear Camión
3. Crear Depósito
4. Login Cliente
5. Crear Solicitud
6. Ver Tracking

### Test Completo (15 min)
- Ejecutar toda la carpeta **🔄 Flujo End-to-End Completo**
- Click derecho en la carpeta → Run folder
- Verificar que todos los requests son exitosos

### Test de Reglas de Negocio
1. **Capacidad**: Asignar camión pequeño a contenedor grande (debe fallar)
2. **Disponibilidad**: Asignar mismo camión a 2 tramos (segundo debe fallar)
3. **Estados**: Intentar finalizar tramo sin iniciarlo (debe fallar)
4. **Roles**: Intentar operaciones con roles incorrectos (debe dar 403)

## 🌐 Endpoints Disponibles

### Orders Service
- `POST /api/orders` - Crear solicitud
- `GET /api/orders/{id}` - Obtener solicitud
- `GET /api/orders/{id}/tracking` - Tracking público
- `POST /api/orders/{id}/estimacion` - Calcular costo/tiempo
- `PUT /api/orders/{id}/costo` - Actualizar costo final
- `POST /api/orders/{id}/estado` - Cambiar estado manual

### Fleet Service
- `POST /api/trucks` - Crear camión
- `GET /api/trucks` - Listar camiones
- `GET /api/trucks?disponible=true` - Solo disponibles
- `GET /api/trucks/{id}` - Obtener camión
- `PUT /api/trucks/{id}` - Actualizar camión
- `POST /api/tarifas` - Crear tarifa
- `GET /api/tarifas` - Listar tarifas
- `GET /api/tarifas/tipo/{tipo}` - Buscar por tipo
- `PUT /api/tarifas/{id}` - Actualizar tarifa
- `GET /api/fleet/metrics/promedios` - Métricas de flota

### Logistics Service
- `POST /api/logistics/depositos` - Crear depósito
- `GET /api/logistics/depositos` - Listar depósitos
- `GET /api/logistics/depositos/{id}` - Obtener depósito
- `PUT /api/logistics/depositos/{id}` - Actualizar depósito
- `POST /api/logistics/rutas` - Crear ruta
- `GET /api/logistics/rutas/{id}` - Obtener ruta
- `POST /api/logistics/rutas/{id}/asignar` - Asignar a solicitud
- `POST /api/logistics/tramos/{id}/asignar` - Asignar camión
- `POST /api/logistics/tramos/{id}/iniciar` - Iniciar tramo
- `POST /api/logistics/tramos/{id}/finalizar` - Finalizar tramo
- `GET /api/logistics/tramos/camion/{id}` - Tramos de un camión
- `GET /api/logistics/tramos/deposito/{id}/contenedores` - Contenedores en depósito

## 📝 Notas Importantes

1. **Orden de Ejecución**: Algunos requests dependen de otros. Usar el flujo end-to-end para la secuencia correcta.

2. **Variables Automáticas**: Los requests con script `Test` guardan automáticamente IDs en variables.

3. **Coordenadas**: Los ejemplos usan coordenadas reales de Argentina:
   - Buenos Aires: -34.6037, -58.3816
   - Rosario: -32.9468, -60.6393
   - Córdoba: -31.4201, -64.1888

4. **Tokens**: Expiran en 5 minutos. Hacer login nuevamente si recibes 401.

5. **Gateway vs Directo**: 
   - Gateway (recomendado): Todo en puerto 8081
   - Directo: Orders (8082), Logistics (8083), Fleet (8084)

---

**¿Necesitas ayuda?** Revisa los logs de Docker o ejecuta el smoke test para verificar que todo funciona.
