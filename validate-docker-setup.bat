@echo off
echo ========================================== 
echo Validacion de Configuracion Docker
echo TPI Backend 2025
echo ==========================================
echo.

set ERRORS=0

echo [1/6] Verificando Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [FAIL] Docker no esta instalado
    set /a ERRORS+=1
) else (
    echo [OK] Docker instalado
)

docker info >nul 2>&1
if errorlevel 1 (
    echo [FAIL] Docker no esta corriendo
    set /a ERRORS+=1
) else (
    echo [OK] Docker esta corriendo
)

echo.
echo [2/6] Verificando archivos Docker...

if exist "docker-compose.yml" (
    echo [OK] docker-compose.yml
) else (
    echo [FAIL] docker-compose.yml no encontrado
    set /a ERRORS+=1
)

if exist "fleet-service\Dockerfile" (
    echo [OK] fleet-service\Dockerfile
) else (
    echo [FAIL] fleet-service\Dockerfile no encontrado
    set /a ERRORS+=1
)

if exist "orders-service\DockerFile" (
    echo [OK] orders-service\DockerFile
) else (
    echo [FAIL] orders-service\DockerFile no encontrado
    set /a ERRORS+=1
)

if exist "logistics-service\Dockerfile" (
    echo [OK] logistics-service\Dockerfile
) else (
    echo [FAIL] logistics-service\Dockerfile no encontrado
    set /a ERRORS+=1
)

if exist "api-gateway\Dockerfile" (
    echo [OK] api-gateway\Dockerfile
) else (
    echo [FAIL] api-gateway\Dockerfile no encontrado
    set /a ERRORS+=1
)

echo.
echo [3/6] Verificando configuraciones dev-docker...

if exist "fleet-service\src\main\resources\application-dev-docker.yml" (
    echo [OK] fleet-service/application-dev-docker.yml
) else (
    echo [FAIL] fleet-service/application-dev-docker.yml no encontrado
    set /a ERRORS+=1
)

if exist "orders-service\src\main\resources\application-dev-docker.yml" (
    echo [OK] orders-service/application-dev-docker.yml
) else (
    echo [FAIL] orders-service/application-dev-docker.yml no encontrado
    set /a ERRORS+=1
)

if exist "logistics-service\src\main\resources\application-dev-docker.yml" (
    echo [OK] logistics-service/application-dev-docker.yml
) else (
    echo [FAIL] logistics-service/application-dev-docker.yml no encontrado
    set /a ERRORS+=1
)

if exist "api-gateway\src\main\resources\application-dev-docker.yml" (
    echo [OK] api-gateway/application-dev-docker.yml
) else (
    echo [FAIL] api-gateway/application-dev-docker.yml no encontrado
    set /a ERRORS+=1
)

echo.
echo [4/6] Verificando .dockerignore...

for %%s in (fleet-service orders-service logistics-service api-gateway) do (
    if exist "%%s\.dockerignore" (
        echo [OK] %%s\.dockerignore
    ) else (
        echo [WARN] %%s\.dockerignore no encontrado
    )
)

echo.
echo [5/6] Verificando scripts...

if exist "smoke-test.bat" (
    echo [OK] smoke-test.bat
) else (
    echo [FAIL] smoke-test.bat no encontrado
    set /a ERRORS+=1
)

if exist "start-keycloak.bat" (
    echo [OK] start-keycloak.bat
) else (
    echo [WARN] start-keycloak.bat no encontrado
)

if exist "stop-keycloak.bat" (
    echo [OK] stop-keycloak.bat
) else (
    echo [WARN] stop-keycloak.bat no encontrado
)

echo.
echo [6/6] Verificando documentacion...

if exist "README.md" (
    echo [OK] README.md
) else (
    echo [FAIL] README.md no encontrado
    set /a ERRORS+=1
)

if exist "DOCKER_INTEGRATION.md" (
    echo [OK] DOCKER_INTEGRATION.md
) else (
    echo [WARN] DOCKER_INTEGRATION.md no encontrado
)

if exist "DOCKER_QUICKSTART.md" (
    echo [OK] DOCKER_QUICKSTART.md
) else (
    echo [WARN] DOCKER_QUICKSTART.md no encontrado
)

if exist "KEYCLOAK.md" (
    echo [OK] KEYCLOAK.md
) else (
    echo [WARN] KEYCLOAK.md no encontrado
)

echo.
echo ==========================================
if %ERRORS%==0 (
    echo [SUCCESS] Configuracion Docker completa!
    echo.
    echo Siguiente paso:
    echo   1. Ejecutar: docker compose build
    echo   2. Ejecutar: docker compose up -d
    echo   3. Esperar ~60 segundos
    echo   4. Verificar: smoke-test.bat
    echo.
    echo O ejecutar directamente: smoke-test.bat
) else (
    echo [ERROR] Se encontraron %ERRORS% errores
    echo Por favor, revisa los archivos faltantes
)
echo ==========================================
echo.
pause
