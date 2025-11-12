\connect orders_service;

-- Tabla: Cliente
-- Datos personales y de contacto
CREATE TABLE clientes (
  id       bigserial PRIMARY KEY,
  nombre   varchar(255) NOT NULL,
  email    varchar(255) NOT NULL UNIQUE,
  telefono varchar(30)
);

-- Tabla: Contenedor
-- Identificación, peso, volumen, estado, cliente asociado
CREATE TABLE contenedores (
  id         bigserial PRIMARY KEY,
  peso       numeric(19,2) NOT NULL,
  volumen    numeric(19,2) NOT NULL,
  estado     varchar(30) NOT NULL DEFAULT 'BORRADOR',
  cliente_id bigint NOT NULL REFERENCES clientes(id),
  updated_at timestamptz,
  updated_by varchar(100)
);

-- Tabla: Solicitud
-- Número, contenedor, cliente, costoEstimado, tiempoEstimado, costoFinal, tiempoReal
CREATE TABLE solicitudes (
  id                      bigserial PRIMARY KEY,
  cliente_id              bigint NOT NULL REFERENCES clientes(id),
  contenedor_id           bigint NOT NULL UNIQUE REFERENCES contenedores(id),
  estado                  varchar(30) NOT NULL,
  costo_estimado          numeric(19,2),
  tiempo_estimado_minutos bigint,
  costo_final             numeric(19,2),
  tiempo_real_minutos     bigint,
  estadia_estimada        numeric(19,2),
  origen                  varchar(255),
  origen_lat              double precision,
  origen_lng              double precision,
  destino                 varchar(255),
  destino_lat             double precision,
  destino_lng             double precision,
  fecha_creacion          timestamptz NOT NULL,
  updated_at              timestamptz,
  updated_by              varchar(100)
);

-- Tabla: SolicitudEvento
-- Historial de cambios de estado de las solicitudes
CREATE TABLE solicitud_eventos (
  id            bigserial PRIMARY KEY,
  solicitud_id  bigint NOT NULL REFERENCES solicitudes(id),
  estado        varchar(30) NOT NULL,
  fecha_evento  timestamptz NOT NULL,
  descripcion   varchar(255)
);

CREATE INDEX ix_contenedores_cliente ON contenedores(cliente_id);
CREATE INDEX ix_solicitudes_cliente ON solicitudes(cliente_id);
CREATE INDEX ix_solicitudes_contenedor ON solicitudes(contenedor_id);
CREATE INDEX ix_solicitud_eventos_solicitud ON solicitud_eventos(solicitud_id);
