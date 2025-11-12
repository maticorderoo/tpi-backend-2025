\connect fleet_service;

-- Tabla: Camión
-- Dominio (patente), nombreTransportista, teléfono, capacidad peso, capacidad volumen, disponibilidad y costos
CREATE TABLE camiones (
  id                   bigserial PRIMARY KEY,
  dominio              varchar(10) NOT NULL UNIQUE,
  transportista_nombre varchar(255) NOT NULL,
  telefono             varchar(255) NOT NULL,
  cap_peso             numeric(12,2) NOT NULL,
  cap_volumen          numeric(12,2) NOT NULL,
  disponible           boolean NOT NULL,
  costo_km_base        numeric(12,2) NOT NULL,
  consumo_l_km         numeric(12,2) NOT NULL,
  CONSTRAINT uk_camiones_dominio UNIQUE (dominio)
);

-- Tabla: Tarifa
-- De acuerdo con el diseño de cada grupo
CREATE TABLE tarifas (
  id    bigserial PRIMARY KEY,
  tipo  varchar(255) NOT NULL UNIQUE,
  valor numeric(12,2) NOT NULL
);
