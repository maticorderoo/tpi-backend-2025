@echo off
echo ====================================
echo  Insertando Datos de Ejemplo
echo ====================================
echo.

echo [1/3] Insertando datos en orders_service...
docker exec -it postgres-tpi psql -U orders_user -d orders_service -c "INSERT INTO clientes (id, nombre, email, telefono, creado_en) VALUES (gen_random_uuid(), 'Juan Perez', 'juan@example.com', '1234567890', NOW()) ON CONFLICT DO NOTHING;"
docker exec -it postgres-tpi psql -U orders_user -d orders_service -c "INSERT INTO clientes (id, nombre, email, telefono, creado_en) VALUES (gen_random_uuid(), 'Maria Garcia', 'maria@example.com', '0987654321', NOW()) ON CONFLICT DO NOTHING;"
echo Insertados 2 clientes en orders_service
echo.

echo [2/3] Insertando datos en logistics_service...
docker exec -it postgres-tpi psql -U logistics_user -d logistics_service -c "INSERT INTO depositos (id, nombre, direccion, latitud, longitud, tarifa_diaria) VALUES (gen_random_uuid(), 'Deposito Central', 'Av. Principal 123', -34.6037, -58.3816, 100.00) ON CONFLICT DO NOTHING;"
docker exec -it postgres-tpi psql -U logistics_user -d logistics_service -c "INSERT INTO depositos (id, nombre, direccion, latitud, longitud, tarifa_diaria) VALUES (gen_random_uuid(), 'Deposito Norte', 'Calle Norte 456', -34.5500, -58.4500, 80.00) ON CONFLICT DO NOTHING;"
echo Insertados 2 depositos en logistics_service
echo.

echo [3/3] Insertando datos en fleet_service...
docker exec -it postgres-tpi psql -U fleet_user -d fleet_service -c "INSERT INTO tarifas (id, nombre, km_base, precio_combustible_por_litro) VALUES (gen_random_uuid(), 'Tarifa Estandar', 50.00, 150.00) ON CONFLICT DO NOTHING;"
docker exec -it postgres-tpi psql -U fleet_user -d fleet_service -c "INSERT INTO tarifas (id, nombre, km_base, precio_combustible_por_litro) VALUES (gen_random_uuid(), 'Tarifa Premium', 70.00, 180.00) ON CONFLICT DO NOTHING;"
echo Insertadas 2 tarifas en fleet_service
echo.

echo ====================================
echo  Datos insertados correctamente!
echo ====================================
echo.
echo Ahora puedes ver estos datos en pgAdmin:
echo.
echo 1. Abre pgAdmin
echo 2. Conectate a TPI Backend - Orders Service
echo 3. Ve a: Schemas - public - Tables - clientes
echo 4. Click derecho - View/Edit Data - All Rows
echo.
echo Repite para logistics_service (depositos) y fleet_service (tarifas)
echo.
pause
