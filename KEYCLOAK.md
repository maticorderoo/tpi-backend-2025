# Guía de Keycloak - TPI 2025

## Requisitos previos

- Docker Desktop instalado y **en ejecución**

## Métodos de inicio

### Método 1: Script Batch (Más fácil) ⭐

1. Asegúrate de que Docker Desktop esté corriendo
2. Ejecuta el script:
   ```cmd
   start-keycloak.bat
   ```
3. El script:
   - Verifica que Docker esté corriendo
   - Descarga la imagen de Keycloak (primera vez)
   - Inicia el contenedor con el realm importado
   - Espera a que Keycloak esté listo

### Método 2: Docker Compose

```cmd
docker-compose -f docker-compose.keycloak.yml up -d
```

Para ver logs:
```cmd
docker-compose -f docker-compose.keycloak.yml logs -f
```

Para detener:
```cmd
docker-compose -f docker-compose.keycloak.yml down
```

### Método 3: Comando Docker directo

```cmd
docker run -d --name keycloak-tpi ^
  -p 8080:8080 ^
  -e KEYCLOAK_ADMIN=admin ^
  -e KEYCLOAK_ADMIN_PASSWORD=admin ^
  -v "%cd%\keycloak\realm-export:/opt/keycloak/data/import" ^
  quay.io/keycloak/keycloak:23.0 ^
  start-dev --import-realm
```

## Acceso a Keycloak

- **URL**: http://localhost:8080
- **Admin Console**: http://localhost:8080/admin
- **Usuario**: admin
- **Contraseña**: admin

## Realm configurado

- **Nombre del Realm**: `tpi-2025`
- **Roles disponibles**:
  - `CLIENTE` - Para operaciones de clientes
  - `OPERADOR` - Para gestión de operaciones
  - `TRANSPORTISTA` - Para asignación de camiones

## Crear usuarios de prueba

1. Accede a http://localhost:8080/admin
2. Inicia sesión con admin/admin
3. Selecciona el realm `tpi-2025` (dropdown superior izquierda)
4. Ve a **Users** → **Add user**
5. Crea usuarios y asígnales roles según necesites

### Ejemplo de usuarios recomendados:

| Usuario | Email | Rol | Password sugerido |
|---------|-------|-----|------------------|
| cliente1 | cliente1@test.com | CLIENTE | test123 |
| operador1 | operador1@test.com | OPERADOR | test123 |
| transportista1 | transportista1@test.com | TRANSPORTISTA | test123 |

## Obtener tokens para Postman

### Opción A: Desde Keycloak Admin

1. Crea un cliente para pruebas:
   - **Clients** → **Create client**
   - Client ID: `tpi-testing`
   - Client authentication: OFF
   - Authentication flow: ✓ Direct access grants

2. Usa Postman con:
   - Auth Type: OAuth 2.0
   - Grant Type: Password Credentials
   - Access Token URL: `http://localhost:8080/realms/tpi-2025/protocol/openid-connect/token`
   - Client ID: `tpi-testing`
   - Username: tu usuario
   - Password: tu password

### Opción B: Curl directo

```cmd
curl -X POST http://localhost:8080/realms/tpi-2025/protocol/openid-connect/token ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
  -d "client_id=tpi-testing" ^
  -d "username=cliente1" ^
  -d "password=test123" ^
  -d "grant_type=password"
```

## Comandos útiles

### Ver logs en tiempo real:
```cmd
docker logs -f keycloak-tpi
```

### Detener Keycloak:
```cmd
docker stop keycloak-tpi
```
O ejecuta: `stop-keycloak.bat`

### Iniciar Keycloak detenido:
```cmd
docker start keycloak-tpi
```

### Eliminar completamente:
```cmd
docker stop keycloak-tpi
docker rm keycloak-tpi
```

### Reiniciar (borrar todo y volver a crear):
```cmd
docker stop keycloak-tpi
docker rm keycloak-tpi
start-keycloak.bat
```

## Troubleshooting

### Error: "Docker no está corriendo"
- Abre Docker Desktop y espera a que inicie completamente
- Verifica que el ícono de Docker en la bandeja del sistema esté activo

### Puerto 8080 ocupado
Si el puerto 8080 está en uso, puedes:

1. Cambiar el puerto de Keycloak:
   ```cmd
   docker run -d --name keycloak-tpi -p 8090:8080 ...
   ```
   Y actualizar las URLs de tus servicios a `http://localhost:8090`

2. O detener el servicio que usa el puerto 8080

### El realm no se importa
Verifica que la ruta al archivo sea correcta:
```cmd
dir keycloak\realm-export\tpi-2025-realm.json
```

## Integración con los servicios

Los servicios Spring Boot buscan Keycloak en `http://localhost:8080` por defecto.

Si usaste otro puerto o host, actualiza:
- `fleet-service/src/main/resources/application.yml`
- `orders-service/src/main/resources/application.yml`
- `logistics-service/src/main/resources/application.yml`
- `api-gateway/src/main/resources/application.yml`

Busca las propiedades:
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/tpi-2025
```

## Next Steps

Después de levantar Keycloak:

1. ✅ Crear usuarios de prueba con los roles necesarios
2. ✅ Obtener tokens JWT
3. ✅ Configurar los tokens en Postman
4. ✅ Probar los endpoints protegidos de los servicios

---

**Nota**: Keycloak en modo desarrollo (`start-dev`) es solo para pruebas locales. No usar en producción.
