\connect logistics_service;

-- Tabla: Depósito
-- Identificación, nombre, dirección, coordenadas
CREATE TABLE depositos (
  id                 bigserial PRIMARY KEY,
  nombre             varchar(120) NOT NULL,
  direccion          varchar(255),
  lat                double precision NOT NULL,
  lng                double precision NOT NULL,
  costo_estadia_dia  numeric(12,2) NOT NULL
);

-- Tabla: Ruta
-- Solicitud, cantidadTramos, cantidadDepósitos
CREATE TABLE rutas_tentativas (
  id                  bigserial PRIMARY KEY,
  solicitud_id        bigint NOT NULL,
  cant_tramos         int NOT NULL,
  cant_depositos      int NOT NULL,
  distancia_total_km  double precision,
  costo_total_aprox   numeric(14,2),
  tiempo_estimado_minutos bigint,
  estado              varchar(30) NOT NULL DEFAULT 'GENERADA',
  ruta_definitiva_id  bigint,
  created_at          timestamptz NOT NULL,
  updated_at          timestamptz NOT NULL
);

CREATE TABLE tramos_tentativos (
  id                     bigserial PRIMARY KEY,
  ruta_tentativa_id      bigint NOT NULL REFERENCES rutas_tentativas(id) ON DELETE CASCADE,
  orden                  int NOT NULL,
  origen_tipo            varchar(30),
  origen_id              bigint,
  origen_descripcion     varchar(255),
  origen_lat             double precision,
  origen_lng             double precision,
  destino_tipo           varchar(30),
  destino_id             bigint,
  destino_descripcion    varchar(255),
  destino_lat            double precision,
  destino_lng            double precision,
  tipo                   varchar(30),
  distancia_km           double precision,
  tiempo_estimado_minutos bigint,
  costo_aproximado       numeric(14,2),
  dias_estadia           int DEFAULT 0,
  costo_estadia_dia      numeric(12,2),
  costo_estadia          numeric(12,2)
);

CREATE TABLE rutas (
  id                  bigserial PRIMARY KEY,
  solicitud_id        bigint,
  cant_tramos         int NOT NULL,
  cant_depositos      int NOT NULL,
  costo_total_aprox   numeric(14,2),
  costo_total_real    numeric(14,2),
  tiempo_estimado_minutos bigint,
  tiempo_real_minutos     bigint,
  peso_total          numeric(12,2),
  volumen_total       numeric(12,2),
  created_at          timestamptz NOT NULL,
  updated_at          timestamptz NOT NULL
);

-- Tabla: Tramo
-- Origen, destino, tipo, estado, costoAproximado, costoReal, fechaHoraInicio, fechaHoraFin, camión
CREATE TABLE tramos (
  id                     bigserial PRIMARY KEY,
  ruta_id                bigint NOT NULL REFERENCES rutas(id),
  origen_tipo            varchar(30),
  origen_id              bigint,
  origen_lat             double precision,
  origen_lng             double precision,
  destino_tipo           varchar(30),
  destino_id             bigint,
  destino_lat                double precision,
  destino_lng                double precision,
  tipo                       varchar(30) NOT NULL,  -- ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO, ORIGEN_DESTINO
  estado                     varchar(30) NOT NULL DEFAULT 'ESTIMADO',  -- ESTIMADO, ASIGNADO, INICIADO, FINALIZADO
  costo_aprox                numeric(14,2),
  costo_real                 numeric(14,2),
  fecha_hora_inicio_estimada timestamptz,
  fecha_hora_fin_estimada    timestamptz,
  fecha_hora_inicio          timestamptz,
  fecha_hora_fin             timestamptz,
  camion_id                  bigint,
  distancia_km_estimada      double precision,
  distancia_km_real          double precision,
  tiempo_estimado_minutos    bigint,
  tiempo_real_minutos        bigint,
  dias_estadia               int DEFAULT 0,
  costo_estadia_dia          numeric(12,2),
  costo_estadia              numeric(12,2),
  updated_at                 timestamptz,
  updated_by                 varchar(100)
);

CREATE INDEX ix_tramos_ruta ON tramos(ruta_id);
CREATE INDEX ix_rutas_solicitud ON rutas(solicitud_id);
CREATE INDEX ix_rutas_tentativas_solicitud ON rutas_tentativas(solicitud_id);
CREATE INDEX ix_tramos_tentativos_ruta ON tramos_tentativos(ruta_tentativa_id);

INSERT INTO depositos (nombre, direccion, lat, lng, costo_estadia_dia) VALUES
  ('Depósito Buenos Aires', 'Dock Sud, Buenos Aires', -34.659, -58.329, 15000),
  ('Depósito Córdoba', 'Av. Circunvalación, Córdoba', -31.424, -64.185, 12000),
  ('Depósito Rosario', 'Zona Franca, Rosario', -32.957, -60.639, 13500);
