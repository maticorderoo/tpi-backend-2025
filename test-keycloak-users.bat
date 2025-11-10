@echo off
echo ========================================
echo Probando usuarios de Keycloak
echo ========================================
echo.

echo [1/3] Probando cliente01...
curl -s -X POST http://localhost:8085/realms/tpi-2025/protocol/openid-connect/token ^
  -d "grant_type=password" ^
  -d "client_id=tpi-client" ^
  -d "username=cliente01" ^
  -d "password=1234" | findstr /i "access_token error"
echo.
echo.

echo [2/3] Probando transportista01...
curl -s -X POST http://localhost:8085/realms/tpi-2025/protocol/openid-connect/token ^
  -d "grant_type=password" ^
  -d "client_id=tpi-client" ^
  -d "username=transportista01" ^
  -d "password=1234" | findstr /i "access_token error"
echo.
echo.

echo [3/3] Probando operador01...
curl -s -X POST http://localhost:8085/realms/tpi-2025/protocol/openid-connect/token ^
  -d "grant_type=password" ^
  -d "client_id=tpi-client" ^
  -d "username=operador01" ^
  -d "password=1234" | findstr /i "access_token error"
echo.
echo.

echo ========================================
echo Pruebas completadas
echo ========================================
