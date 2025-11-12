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
CREATE TABLE rutas (
  id                  bigserial PRIMARY KEY,
  solicitud_id        bigint,
  cant_tramos         int NOT NULL,
  cant_depositos      int NOT NULL,
  costo_total_aprox   numeric(14,2),
  costo_total_real    numeric(14,2),
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
  dias_estadia               int DEFAULT 0,
  costo_estadia_dia          numeric(12,2),
  costo_estadia              numeric(12,2),
  updated_at                 timestamptz,
  updated_by                 varchar(100)
);

CREATE INDEX ix_tramos_ruta ON tramos(ruta_id);
CREATE INDEX ix_rutas_solicitud ON rutas(solicitud_id);
