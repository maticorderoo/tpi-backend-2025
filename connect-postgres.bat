@echo off
echo ====================================
echo  Acceso Rapido a PostgreSQL
echo ====================================
echo.
echo Selecciona la base de datos:
echo.
echo 1. Orders Service (orders_service)
echo 2. Logistics Service (logistics_service)
echo 3. Fleet Service (fleet_service)
echo 4. PostgreSQL Admin (postgres)
echo.
set /p choice="Ingresa tu opcion (1-4): "

if "%choice%"=="1" (
    echo.
    echo Conectando a orders_service...
    echo Usuario: orders_user
    echo Password: Orders_!2025
    echo.
    docker exec -it postgres-tpi psql -U orders_user -d orders_service
    goto :end
)

if "%choice%"=="2" (
    echo.
    echo Conectando a logistics_service...
    echo Usuario: logistics_user
    echo Password: Logistics_!2025
    echo.
    docker exec -it postgres-tpi psql -U logistics_user -d logistics_service
    goto :end
)

if "%choice%"=="3" (
    echo.
    echo Conectando a fleet_service...
    echo Usuario: fleet_user
    echo Password: Fleet_!2025
    echo.
    docker exec -it postgres-tpi psql -U fleet_user -d fleet_service
    goto :end
)

if "%choice%"=="4" (
    echo.
    echo Conectando como admin...
    echo Usuario: tpi_admin
    echo Password: SuperSegura_!2025
    echo.
    docker exec -it postgres-tpi psql -U tpi_admin -d postgres
    goto :end
)

echo.
echo Opcion invalida!
echo.

:end
