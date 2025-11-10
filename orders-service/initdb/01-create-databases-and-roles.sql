-- Bases por micro
CREATE DATABASE orders_service  OWNER tpi_admin;
CREATE DATABASE logistics_service OWNER tpi_admin;
CREATE DATABASE fleet_service    OWNER tpi_admin;

-- Roles por micro (opcional, para credenciales separadas)
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'orders_user') THEN
      CREATE ROLE orders_user LOGIN PASSWORD 'Orders_!2025';
   END IF;
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'logistics_user') THEN
      CREATE ROLE logistics_user LOGIN PASSWORD 'Logistics_!2025';
   END IF;
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'fleet_user') THEN
      CREATE ROLE fleet_user LOGIN PASSWORD 'Fleet_!2025';
   END IF;
END$$;

-- Dar privilegios sobre las bases de datos
GRANT ALL PRIVILEGES ON DATABASE orders_service   TO orders_user;
GRANT ALL PRIVILEGES ON DATABASE logistics_service TO logistics_user;
GRANT ALL PRIVILEGES ON DATABASE fleet_service     TO fleet_user;

-- Dar permisos sobre el schema public en cada base de datos
\c orders_service
GRANT ALL ON SCHEMA public TO orders_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO orders_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO orders_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO orders_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO orders_user;

\c logistics_service
GRANT ALL ON SCHEMA public TO logistics_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO logistics_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO logistics_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO logistics_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO logistics_user;

\c fleet_service
GRANT ALL ON SCHEMA public TO fleet_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO fleet_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO fleet_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO fleet_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO fleet_user;
