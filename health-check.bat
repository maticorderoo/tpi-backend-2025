@echo off
echo ====================================
echo  TPI Backend - Health Check
echo ====================================
echo.

echo Verificando estado de los servicios...
echo.

curl -s http://localhost:8084/actuator/health | findstr "UP" >nul
if %errorlevel%==0 (
    echo [OK] Fleet Service esta saludable
) else (
    echo [ERROR] Fleet Service no responde
)

curl -s http://localhost:8082/actuator/health | findstr "UP" >nul
if %errorlevel%==0 (
    echo [OK] Orders Service esta saludable
) else (
    echo [ERROR] Orders Service no responde
)

curl -s http://localhost:8083/actuator/health | findstr "UP" >nul
if %errorlevel%==0 (
    echo [OK] Logistics Service esta saludable
) else (
    echo [ERROR] Logistics Service no responde
)

curl -s http://localhost:8081/actuator/health | findstr "UP" >nul
if %errorlevel%==0 (
    echo [OK] API Gateway esta saludable
) else (
    echo [ERROR] API Gateway no responde
)

echo.
echo Estado de contenedores:
docker ps --format "table {{.Names}}\t{{.Status}}"
echo.
