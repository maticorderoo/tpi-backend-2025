\connect orders_service;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE clients (
  id           uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  name         varchar(120) NOT NULL,
  email        varchar(160),
  phone        varchar(50),
  created_at   timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE containers (
  id           uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  code         varchar(50) UNIQUE NOT NULL,
  weight_kg    numeric(12,2) NOT NULL,
  volume_m3    numeric(12,3) NOT NULL,
  state        varchar(30) NOT NULL DEFAULT 'nuevo'
);

CREATE TABLE requests (
  id             uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  client_id      uuid NOT NULL REFERENCES clients(id),
  container_id   uuid NOT NULL REFERENCES containers(id),
  origin_lat     numeric(10,6) NOT NULL,
  origin_lng     numeric(10,6) NOT NULL,
  dest_lat       numeric(10,6) NOT NULL,
  dest_lng       numeric(10,6) NOT NULL,
  status         varchar(20) NOT NULL DEFAULT 'borrador', -- borrador|programada|en_transito|entregada
  route_ref      varchar(100), -- referencia lógica a Logistics
  cost_estimate  numeric(14,2),
  time_estimate_min integer,
  cost_final     numeric(14,2),
  time_real_min  integer,
  created_at     timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE tracking (
  id           bigserial PRIMARY KEY,
  request_id   uuid NOT NULL REFERENCES requests(id),
  status       varchar(30) NOT NULL, -- retirado|en_viaje|en_deposito|entregado
  note         varchar(300),
  at_time      timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX ix_requests_client ON requests(client_id);
CREATE INDEX ix_tracking_request_time ON tracking(request_id, at_time DESC);
