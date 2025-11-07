@echo off
echo ==========================================
echo Docker Smoke Test - TPI Backend 2025
echo ==========================================
echo.

REM Verificar que Docker este corriendo
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker no esta corriendo.
    echo Por favor, inicia Docker Desktop y vuelve a ejecutar este script.
    pause
    exit /b 1
)

echo [1/4] Construyendo imagenes Docker...
echo.
docker compose build
if errorlevel 1 (
    echo [ERROR] Fallo la construccion de imagenes
    pause
    exit /b 1
)

echo.
echo [2/4] Levantando servicios...
echo.
docker compose up -d
if errorlevel 1 (
    echo [ERROR] Fallo al levantar servicios
    pause
    exit /b 1
)

echo.
echo [3/4] Esperando que los servicios esten listos (60 segundos)...
timeout /t 60 /nobreak >nul

echo.
echo [4/4] Ejecutando health checks...
echo.

set ALL_OK=1

REM Fleet Service
echo Testing Fleet Service (8084)...
curl -s http://localhost:8084/actuator/health | findstr /C:"UP" >nul 2>&1
if errorlevel 1 (
    echo   [FAIL] Fleet Service no responde
    set ALL_OK=0
) else (
    echo   [OK] Fleet Service: UP
)

REM Orders Service
echo Testing Orders Service (8082)...
curl -s http://localhost:8082/actuator/health | findstr /C:"UP" >nul 2>&1
if errorlevel 1 (
    echo   [FAIL] Orders Service no responde
    set ALL_OK=0
) else (
    echo   [OK] Orders Service: UP
)

REM Logistics Service
echo Testing Logistics Service (8083)...
curl -s http://localhost:8083/actuator/health | findstr /C:"UP" >nul 2>&1
if errorlevel 1 (
    echo   [FAIL] Logistics Service no responde
    set ALL_OK=0
) else (
    echo   [OK] Logistics Service: UP
)

REM API Gateway
echo Testing API Gateway (8080)...
curl -s http://localhost:8080/actuator/health | findstr /C:"UP" >nul 2>&1
if errorlevel 1 (
    echo   [FAIL] API Gateway no responde
    set ALL_OK=0
) else (
    echo   [OK] API Gateway: UP
)

echo.
echo ==========================================
if %ALL_OK%==1 (
    echo [SUCCESS] Todos los servicios estan UP!
    echo.
    echo Puedes acceder a:
    echo   - Fleet Swagger:     http://localhost:8084/swagger-ui.html
    echo   - Orders Swagger:    http://localhost:8082/swagger-ui.html
    echo   - Logistics Swagger: http://localhost:8083/swagger-ui.html
    echo   - Gateway Health:    http://localhost:8080/actuator/health
    echo.
    echo Para detener los servicios: docker compose down
) else (
    echo [WARNING] Algunos servicios fallaron
    echo.
    echo Para ver logs: docker compose logs -f
    echo Para reintentar: docker compose restart
)
echo ==========================================
echo.
pause
