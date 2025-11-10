\connect logistics_service;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE deposits (
  id          uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  name        varchar(120) NOT NULL,
  address     varchar(180),
  lat         numeric(10,6) NOT NULL,
  lng         numeric(10,6) NOT NULL,
  daily_fee   numeric(12,2) NOT NULL DEFAULT 0
);

CREATE TABLE routes (
  id            uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  request_ref   varchar(100) NOT NULL,  -- referencia lógica a Orders.requests
  legs_count    int NOT NULL DEFAULT 0,
  deposits_count int NOT NULL DEFAULT 0,
  created_at    timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE legs (
  id            uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  route_id      uuid NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
  origin_lat    numeric(10,6) NOT NULL,
  origin_lng    numeric(10,6) NOT NULL,
  dest_lat      numeric(10,6) NOT NULL,
  dest_lng      numeric(10,6) NOT NULL,
  kind          varchar(25) NOT NULL,  -- origen-deposito|deposito-deposito|deposito-destino|origen-destino
  status        varchar(20) NOT NULL DEFAULT 'estimado', -- estimado|asignado|iniciado|finalizado
  truck_ref     varchar(100), -- referencia lógica a Fleet
  cost_est      numeric(14,2),
  cost_real     numeric(14,2),
  start_time    timestamptz,
  end_time      timestamptz
);

CREATE TABLE layovers (
  id          uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  leg_id      uuid NOT NULL REFERENCES legs(id) ON DELETE CASCADE,
  deposit_id  uuid NOT NULL REFERENCES deposits(id),
  arrival_at  timestamptz,
  depart_at   timestamptz
);

CREATE INDEX ix_legs_route ON legs(route_id);
CREATE INDEX ix_layovers_deposit ON layovers(deposit_id);
