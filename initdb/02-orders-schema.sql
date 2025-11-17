\connect orders_service;

-- Tabla: Cliente
-- Datos personales y de contacto
CREATE TABLE clientes (
  id       bigserial PRIMARY KEY,
  nombre   varchar(255) NOT NULL,
  email    varchar(255) NOT NULL UNIQUE,
  telefono varchar(30),
  cuit     varchar(20) NOT NULL UNIQUE
);

-- Tabla: Contenedor
-- Identificación, peso, volumen, estado, cliente asociado
CREATE TABLE contenedores (
  id         bigserial PRIMARY KEY,
  codigo     varchar(50) NOT NULL,
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
  ruta_logistica_id       bigint,
  costo_final             numeric(19,2),
  tiempo_real_minutos     bigint,
  estadia_estimada        numeric(19,2),
  observaciones           varchar(500),
  origen                  varchar(255) NOT NULL,
  origen_lat              double precision NOT NULL,
  origen_lng              double precision NOT NULL,
  destino                 varchar(255) NOT NULL,
  destino_lat             double precision NOT NULL,
  destino_lng             double precision NOT NULL,
  fecha_creacion          timestamptz NOT NULL,
  updated_at              timestamptz,
  updated_by              varchar(100)
);

CREATE INDEX ix_contenedores_cliente ON contenedores(cliente_id);
CREATE INDEX ix_solicitudes_cliente ON solicitudes(cliente_id);
CREATE INDEX ix_solicitudes_contenedor ON solicitudes(contenedor_id);
CREATE UNIQUE INDEX ux_contenedores_codigo_activos
  ON contenedores(codigo)
  WHERE estado NOT IN ('ENTREGADO','CANCELADO');
