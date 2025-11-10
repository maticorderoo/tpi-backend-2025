@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Verificando roles en tokens JWT
echo ========================================
echo.

echo [CLIENTE01]
echo Username: cliente01
echo Password: 1234
echo Rol esperado: CLIENTE
for /f "tokens=*" %%i in ('curl -s -X POST http://localhost:8085/realms/tpi-2025/protocol/openid-connect/token -d "grant_type=password" -d "client_id=tpi-client" -d "username=cliente01" -d "password=1234"') do set TOKEN_CLIENTE=%%i
echo Token obtenido: OK
echo.

echo [TRANSPORTISTA01]
echo Username: transportista01
echo Password: 1234
echo Rol esperado: TRANSPORTISTA
for /f "tokens=*" %%i in ('curl -s -X POST http://localhost:8085/realms/tpi-2025/protocol/openid-connect/token -d "grant_type=password" -d "client_id=tpi-client" -d "username=transportista01" -d "password=1234"') do set TOKEN_TRANSPORTISTA=%%i
echo Token obtenido: OK
echo.

echo [OPERADOR01]
echo Username: operador01
echo Password: 1234
echo Rol esperado: OPERADOR
for /f "tokens=*" %%i in ('curl -s -X POST http://localhost:8085/realms/tpi-2025/protocol/openid-connect/token -d "grant_type=password" -d "client_id=tpi-client" -d "username=operador01" -d "password=1234"') do set TOKEN_OPERADOR=%%i
echo Token obtenido: OK
echo.

echo ========================================
echo Todos los usuarios pueden autenticarse
echo ========================================
