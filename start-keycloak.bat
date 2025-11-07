@echo off
echo ==========================================
echo Levantando Keycloak con realm tpi-2025
echo ==========================================
echo.

REM Verificar si Docker está corriendo
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker no esta corriendo.
    echo Por favor, inicia Docker Desktop y vuelve a ejecutar este script.
    echo.
    pause
    exit /b 1
)

REM Detener y eliminar contenedor existente si existe
docker stop keycloak-tpi >nul 2>&1
docker rm keycloak-tpi >nul 2>&1

echo Descargando e iniciando Keycloak...
echo.

docker run -d --name keycloak-tpi ^
  -p 8080:8080 ^
  -e KEYCLOAK_ADMIN=admin ^
  -e KEYCLOAK_ADMIN_PASSWORD=admin ^
  -v "%cd%\keycloak\realm-export:/opt/keycloak/data/import" ^
  quay.io/keycloak/keycloak:23.0 ^
  start-dev --import-realm

if errorlevel 1 (
    echo [ERROR] Fallo al iniciar Keycloak
    pause
    exit /b 1
)

echo.
echo ==========================================
echo Keycloak iniciado correctamente!
echo ==========================================
echo.
echo URL Admin Console: http://localhost:8080
echo Usuario: admin
echo Password: admin
echo.
echo Realm: tpi-2025
echo Roles disponibles: CLIENTE, OPERADOR, TRANSPORTISTA
echo.
echo Para ver los logs:
echo   docker logs -f keycloak-tpi
echo.
echo Para detener:
echo   docker stop keycloak-tpi
echo.
echo Esperando a que Keycloak este listo...
timeout /t 5 /nobreak >nul

:wait_loop
docker logs keycloak-tpi 2>&1 | findstr /C:"Running the server in development mode" >nul 2>&1
if errorlevel 1 (
    echo Keycloak iniciando...
    timeout /t 2 /nobreak >nul
    goto wait_loop
)

echo.
echo [OK] Keycloak esta listo!
echo Abre http://localhost:8080 en tu navegador
echo.
pause
