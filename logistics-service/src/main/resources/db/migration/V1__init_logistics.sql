CREATE TABLE IF NOT EXISTS depositos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    direccion VARCHAR(255),
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    costo_estadia_dia NUMERIC(12,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS rutas (
    id SERIAL PRIMARY KEY,
    solicitud_id BIGINT,
    cant_tramos INTEGER NOT NULL,
    cant_depositos INTEGER NOT NULL,
    costo_total_aprox NUMERIC(14,2) DEFAULT 0,
    costo_total_real NUMERIC(14,2) DEFAULT 0,
    peso_total NUMERIC(12,2) DEFAULT 0,
    volumen_total NUMERIC(12,2) DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tramos (
    id SERIAL PRIMARY KEY,
    ruta_id BIGINT NOT NULL REFERENCES rutas(id) ON DELETE CASCADE,
    origen_tipo VARCHAR(30),
    origen_id BIGINT,
    destino_tipo VARCHAR(30),
    destino_id BIGINT,
    tipo VARCHAR(30) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    costo_aprox NUMERIC(14,2) DEFAULT 0,
    costo_real NUMERIC(14,2) DEFAULT 0,
    fecha_hora_inicio TIMESTAMP WITH TIME ZONE,
    fecha_hora_fin TIMESTAMP WITH TIME ZONE,
    camion_id BIGINT,
    distancia_km_estimada DOUBLE PRECISION,
    distancia_km_real DOUBLE PRECISION,
    dias_estadia INTEGER DEFAULT 0,
    costo_estadia_dia NUMERIC(12,2) DEFAULT 0,
    costo_estadia NUMERIC(12,2) DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_tramos_estado ON tramos(estado);
CREATE INDEX IF NOT EXISTS idx_tramos_destino ON tramos(destino_tipo, destino_id);
