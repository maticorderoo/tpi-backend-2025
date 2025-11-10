\connect fleet_service;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE drivers (
  id        uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  name      varchar(120) NOT NULL,
  phone     varchar(50),
  is_active boolean NOT NULL DEFAULT true
);

CREATE TABLE trucks (
  id            uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  plate         varchar(20) UNIQUE NOT NULL,
  driver_id     uuid REFERENCES drivers(id),
  capacity_kg   numeric(12,2) NOT NULL,
  capacity_m3   numeric(12,3) NOT NULL,
  cost_per_km   numeric(12,2) NOT NULL,
  avg_consumption_l_per_km numeric(8,4) NOT NULL, -- p/ costo combustible
  availability  varchar(20) NOT NULL DEFAULT 'libre' -- libre|ocupado|mantenimiento
);

CREATE TABLE tariffs (
  id          uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  name        varchar(100) NOT NULL,
  km_base     numeric(12,2) NOT NULL DEFAULT 0,
  fuel_price_per_l numeric(10,2) NOT NULL DEFAULT 0
);

CREATE INDEX ix_trucks_driver ON trucks(driver_id);
