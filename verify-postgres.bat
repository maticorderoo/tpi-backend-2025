@echo off
echo ====================================
echo  PostgreSQL - Verificacion Completa
echo ====================================
echo.

echo [1/5] Verificando version de PostgreSQL...
docker exec postgres-tpi psql -U tpi_admin -d postgres -c "SELECT version();"
echo.

echo [2/5] Listando bases de datos...
docker exec postgres-tpi psql -U tpi_admin -c "\l"
echo.

echo [3/5] Tablas en orders_service:
docker exec postgres-tpi psql -U tpi_admin -d orders_service -c "\dt"
echo.

echo [4/5] Tablas en logistics_service:
docker exec postgres-tpi psql -U tpi_admin -d logistics_service -c "\dt"
echo.

echo [5/5] Tablas en fleet_service:
docker exec postgres-tpi psql -U tpi_admin -d fleet_service -c "\dt"
echo.

echo ====================================
echo  Conteo de registros
echo ====================================
echo.

echo Orders Service - Clients:
docker exec postgres-tpi psql -U tpi_admin -d orders_service -t -c "SELECT COUNT(*) FROM clients;"

echo Orders Service - Containers:
docker exec postgres-tpi psql -U tpi_admin -d orders_service -t -c "SELECT COUNT(*) FROM containers;"

echo Orders Service - Requests:
docker exec postgres-tpi psql -U tpi_admin -d orders_service -t -c "SELECT COUNT(*) FROM requests;"

echo.
echo Logistics Service - Deposits:
docker exec postgres-tpi psql -U tpi_admin -d logistics_service -t -c "SELECT COUNT(*) FROM deposits;"

echo Logistics Service - Routes:
docker exec postgres-tpi psql -U tpi_admin -d logistics_service -t -c "SELECT COUNT(*) FROM routes;"

echo.
echo Fleet Service - Drivers:
docker exec postgres-tpi psql -U tpi_admin -d fleet_service -t -c "SELECT COUNT(*) FROM drivers;"

echo Fleet Service - Trucks:
docker exec postgres-tpi psql -U tpi_admin -d fleet_service -t -c "SELECT COUNT(*) FROM trucks;"

echo Fleet Service - Tariffs:
docker exec postgres-tpi psql -U tpi_admin -d fleet_service -t -c "SELECT COUNT(*) FROM tariffs;"

echo.
echo ====================================
echo  Verificacion completa!
echo ====================================
