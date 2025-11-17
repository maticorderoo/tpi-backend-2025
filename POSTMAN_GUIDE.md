# 📬 Guía de Uso de Postman - TPI Backend 2025

## 📁 Archivos Incluidos

| Archivo | Descripción |
|---------|-------------|
| **TPI-2025-secured.postman_collection.json** | Colección alineada al API Gateway (`/api/**`). Incluye carpetas por rol y requests típicos. |
| **TPI-2025.gateway-dev.postman_environment.json** | Environment para Docker Compose (Gateway en `http://localhost:8081`, Keycloak `http://localhost:8085`). |
| **TPI-2025.local-dev.postman_environment.json** | Environment para ejecución local (Gateway en `http://localhost:8080`). |

Importá los 3 archivos desde **Postman → Import**.

## 🔐 Usuarios & Roles

Los usuarios provienen del realm `tpi-2025` de Keycloak y se mapean 1 a 1 con los roles funcionales.

| Usuario | Contraseña | Rol | Permisos principales |
|---------|------------|-----|----------------------|
| `cliente01` | `cliente123` | CLIENTE | Crear solicitudes propias y consultar su tracking. |
| `operador01` | `operador123` | OPERADOR | Operar logística/flota: rutas, tramos, asignaciones, costos. |
| `transportista01` | `trans123` | TRANSPORTISTA | Ver tramos asignados e iniciar/finalizar recorridos. |
| `admin01` | `admin123` | ADMIN | Todo lo anterior + administración de depósitos, camiones y tarifas. |

> ℹ️ El rol **ADMIN** tiene acceso a todos los endpoints autorizados para los demás roles.

## 🚀 Configuración de Environments

Ambos environments comparten las mismas variables, sólo cambia `gateway_base_url`.

| Variable | Ejemplo (gateway-dev) | Descripción |
|----------|----------------------|-------------|
| `gateway_base_url` | `http://localhost:8081` | URL pública del API Gateway. |
| `keycloak_base_url` | `http://localhost:8085` | URL base de Keycloak. |
| `realm` | `tpi-2025` | Realm configurado. |
| `client_id` | `tpi-client` | Client público usado por Postman. |
| `*_username` / `*_password` | `cliente01` / `cliente123` | Credenciales por rol (cliente/operador/transportista/admin). |
| `access_token_*` | *(vacío inicialmente)* | Aquí se guardan los tokens generados por cada login. |
| `solicitud_id`, `contenedor_id`, `ruta_id`, `tramo_id`, `camion_id`, `deposito_id`, `tarifa_id` | *(vacío)* | Variables que se rellenan con IDs devueltos por la API para reutilizarlos en requests posteriores. |

### Pasos para autenticarse

1. Seleccioná el environment deseado (`gateway-dev` o `local-dev`).
2. En la carpeta **🔐 Authentication** ejecutá los logins necesarios para tu flujo.
3. Cada request guarda automáticamente el token en su variable (`access_token_cliente`, etc.).
4. Los requests de la colección leen el token correspondiente mediante el header `Authorization: Bearer ...`.

Si necesitás un token manualmente, podés copiar el valor de la variable desde **Environment → Edit → Current values**.

## 📂 Estructura de la Colección

- **🔐 Authentication**: Logins para CLIENTE / OPERADOR / TRANSPORTISTA / ADMIN (grant type `password`).
- **👤 Cliente**:
  - Crear solicitud (setea `solicitud_id` y `contenedor_id`).
  - Ver detalle de su solicitud.
  - Tracking básico desde Orders.
  - Tracking extendido desde Logistics.
- **⚙️ Operador**:
  - Calcular estimación y actualizar costo.
  - Consultar contenedores pendientes.
  - Listar / asignar tramos.
  - Seguimiento global (todos los contenedores).
- **🚚 Transportista**:
  - Listar tramos asignados al camión actual.
  - Marcar inicio y fin del tramo.
- **🛡️ Admin**:
  - Crear depósito.
  - Crear camión.
  - Crear tarifa.

Cada request usa exclusivamente el Gateway (`{{gateway_base_url}}/api/...`) y valida el rol indicado por el enunciado.

## ✅ Casos de Prueba Recomendados

1. **Flujo cliente → operador → transportista**
   1. Login Operador → Crear Depósito + Camión.
   2. Login Cliente → Crear Solicitud (guardar IDs).
   3. Login Operador → Calcular estimación, asignar camión al tramo, verificar seguimiento global.
   4. Login Transportista → Listar tramos asignados, iniciar y finalizar tramo.
   5. Login Operador/Admin → Actualizar costo final.
   6. Login Cliente → Reconsultar tracking (Orders + Logistics) y validar estados.

2. **Validación de seguridad**
   - Intentar crear camión con token de CLIENTE → debe responder 403.
   - Intentar acceder a `/api/logistics/seguimiento/pendientes` con token de TRANSPORTISTA → debe responder 403.
   - Tracking (Orders o Logistics) sin token → 401 (el Gateway ahora protege todos los endpoints). 

3. **Asignación de camión**
   - Enviar un camión con capacidad insuficiente al endpoint `POST /api/logistics/tramos/{id}/asignaciones` → error 409 con mensaje explicativo.

## 🧭 Notas importantes

- **Todos los endpoints se consumen vía Gateway**. No golpees directamente a los microservicios para no saltar la validación JWT.
- El Gateway agrega automáticamente `X-User-Id`, `X-User-Username` y `X-User-Roles` para ayudar al backend a trazar auditorías.
- Recordá refrescar los tokens cada ~60 minutos (lifetime del Access Token configurado en el realm).
- Las variables de IDs se actualizan sólo si la respuesta tiene JSON con los campos esperados; verificá los test scripts si necesitás personalizarlos.
