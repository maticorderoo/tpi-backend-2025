@echo off
echo Deteniendo Keycloak...
docker stop keycloak-tpi
docker rm keycloak-tpi
echo Keycloak detenido.
pause
