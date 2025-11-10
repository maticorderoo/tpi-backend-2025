@echo off
echo ====================================
echo  TPI Backend - PostgreSQL Setup
echo ====================================
echo.

echo Deteniendo contenedores existentes...
docker compose down
echo.

echo Eliminando volumenes antiguos (opcional - comentar si quieres mantener datos)...
docker volume rm tpi-backend-2025_postgres_data 2>nul
echo.

echo Construyendo imagenes...
docker compose build
echo.

echo Iniciando servicios...
docker compose up -d
echo.

echo Esperando que PostgreSQL este listo...
timeout /t 15 /nobreak >nul
echo.

echo Estado de los contenedores:
docker ps
echo.

echo ====================================
echo  Accesos:
echo ====================================
echo API Gateway:        http://localhost:8081
echo Orders Service:     http://localhost:8082
echo Logistics Service:  http://localhost:8083
echo Fleet Service:      http://localhost:8084
echo.
echo Swagger UI:
echo   Orders:    http://localhost:8082/swagger-ui.html
echo   Logistics: http://localhost:8083/swagger-ui.html
echo   Fleet:     http://localhost:8084/swagger-ui.html
echo.
echo PostgreSQL:
echo   Host:     localhost:5432
echo   Admin:    tpi_admin / SuperSegura_!2025
echo   Databases:
echo     - orders_service   (user: orders_user / Orders_!2025)
echo     - logistics_service (user: logistics_user / Logistics_!2025)
echo     - fleet_service    (user: fleet_user / Fleet_!2025)
echo.
echo ====================================

echo.
echo Para ver logs: docker compose logs -f [service-name]
echo Para detener: docker compose down
echo.
