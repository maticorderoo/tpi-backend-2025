# 🔐 Configuración de Keycloak - TPI Backend 2025

## 📋 Usuarios Preconfigurados

Al iniciar el sistema, Keycloak viene preconfigurado con 3 usuarios de prueba:

### 1. Cliente
- **Username:** `cliente01`
- **Password:** `1234`
- **Rol:** `CLIENTE`
- **Permisos:**
  - ✅ Crear solicitudes (`POST /api/orders`)
  - ✅ Ver solicitud (`GET /api/orders/{id}`)
  - ✅ Ver seguimiento (`GET /api/orders/{id}/tracking`)

### 2. Transportista
- **Username:** `transportista01`
- **Password:** `1234`
- **Rol:** `TRANSPORTISTA`
- **Permisos:**
  - ✅ Iniciar tramos (`POST /api/logistics/tramos/{id}/inicio`)
  - ✅ Finalizar tramos (`POST /api/logistics/tramos/{id}/fin`)
  - ✅ Ver métricas de flota (`GET /metrics`)

### 3. Operador
- **Username:** `operador01`
- **Password:** `1234`
  - **Rol:** `OPERADOR`
  - **Permisos:**
    - ✅ Crear/asignar rutas (`POST /api/logistics/rutas`)
    - ✅ Asignar camiones (`POST /api/logistics/tramos/{id}/asignar-camion`)
    - ✅ Ver todas las solicitudes (`GET /api/orders`)
    - ✅ Crear estimaciones (`POST /api/orders/{id}/estimacion`)
    - ✅ Gestionar flota (`POST /api/trucks`)

  ## 🚀 Cómo Obtener un Token

  ### Usando curl (CMD/PowerShell)

  ```cmd
  curl -X POST http://localhost:8085/realms/tpi-2025/protocol/openid-connect/token ^
    -d "grant_type=password" ^
    -d "client_id=tpi-client" ^
    -d "username=cliente01" ^
    -d "password=1234"
  ```

  ### Usando PowerShell

  ```powershell
  $response = Invoke-RestMethod -Uri "http://localhost:8085/realms/tpi-2025/protocol/openid-connect/token" `
    -Method Post `
    -Body @{ 
      grant_type = "password"
      client_id = "tpi-client"
      username = "cliente01"
      password = "1234"
    }

  $token = $response.access_token
  Write-Host "Token: $token"
  ```

  ## 🔑 Usar el Token en las Peticiones

  Una vez obtenido el token, úsalo en el header `Authorization`:

  ```cmd
  curl -H "Authorization: Bearer <TU_TOKEN_AQUI>" ^
    http://localhost:8081/api/orders
  ```

  ## 📊 Endpoints por Rol (resumen)

  ### CLIENTE
  | Método | Endpoint | Descripción |
  |--------|----------|-------------|
  | POST | `/api/orders` | Crear solicitud |
  | GET | `/api/orders/{id}` | Ver solicitud |
  | GET | `/api/orders/{id}/tracking` | Seguimiento |

  ### TRANSPORTISTA
  | Método | Endpoint | Descripción |
  |--------|----------|-------------|
  | POST | `/api/logistics/tramos/{id}/inicio` | Iniciar tramo |
  | POST | `/api/logistics/tramos/{id}/fin` | Finalizar tramo |
  | GET | `/metrics` | Ver métricas |

### OPERADOR
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/logistics/rutas` | Crear ruta |
| POST | `/api/logistics/rutas/{id}/asignar` | Asignar ruta |
| GET | `/api/logistics/rutas/solicitud/{id}` | Ver ruta |
| POST | `/api/logistics/tramos/{id}/asignar-camion` | Asignar camión |
| GET | `/api/orders` | Ver todas las solicitudes |
| POST | `/api/orders/{id}/estimacion` | Crear estimación |
| GET | `/api/trucks?disponible=true` | Ver camiones |
| POST | `/api/trucks` | Crear camión |  ## 🔒 Respuestas de Error

  ### 401 Unauthorized
  Sin token o token inválido:
  ```json
  {
    "error": "unauthorized"
  }
  ```

  ### 403 Forbidden
  Token válido pero sin el rol necesario:
  ```json
  {
    "error": "forbidden"
  }
  ```

  ## 🛠️ Troubleshooting

  ### El token expira
  Los tokens tienen una duración de **1 hora (3600 segundos)**. Usa el `refresh_token` para obtener uno nuevo sin volver a autenticarte:

  ```cmd
  curl -X POST http://localhost:8085/realms/tpi-2025/protocol/openid-connect/token ^
    -d "grant_type=refresh_token" ^
    -d "client_id=tpi-client" ^
    -d "refresh_token=<TU_REFRESH_TOKEN>"
  ```

  ## 📝 Notas Importantes

  - Los usuarios están preconfigurados en el archivo `keycloak/realm-export/tpi-2025-realm.json`
  - Las contraseñas **no son temporales**, no se pedirá cambiarlas al primer login
  - Los tokens incluyen el claim `realm_access.roles` que se mapea a `ROLE_*` en Spring Security
  - Todos los endpoints de Swagger y Actuator están públicos (no requieren autenticación)

```cmd
curl -X POST http://localhost:8085/realms/tpi-2025/protocol/openid-connect/token ^
  -d "grant_type=refresh_token" ^
  -d "client_id=tpi-client" ^
  -d "refresh_token=<TU_REFRESH_TOKEN>"
```

### Error "client_not_found"
Verifica que estés usando `client_id=tpi-client` en tus peticiones.

### Error "user_not_found"
Verifica que el usuario exista y esté habilitado en la consola de administración.

## 📝 Notas Importantes

- Los usuarios están preconfigurados en el archivo `keycloak/realm-export/tpi-2025-realm.json`
- Las contraseñas **no son temporales**, no se pedirá cambiarlas al primer login
- Los tokens incluyen el claim `realm_access.roles` que se mapea a `ROLE_*` en Spring Security
- Todos los endpoints de Swagger y Actuator están públicos (no requieren autenticación)
