CREATE TABLE IF NOT EXISTS camiones (
    id SERIAL PRIMARY KEY,
    dominio VARCHAR(10) NOT NULL UNIQUE,
    transportista_nombre VARCHAR(255) NOT NULL,
    telefono VARCHAR(100) NOT NULL,
    cap_peso NUMERIC(12,2) NOT NULL CHECK (cap_peso > 0),
    cap_volumen NUMERIC(12,2) NOT NULL CHECK (cap_volumen > 0),
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    costo_km_base NUMERIC(12,2) NOT NULL CHECK (costo_km_base > 0),
    consumo_l_km NUMERIC(12,2) NOT NULL CHECK (consumo_l_km > 0)
);

CREATE TABLE IF NOT EXISTS tarifas (
    id SERIAL PRIMARY KEY,
    tipo VARCHAR(255) NOT NULL UNIQUE,
    valor NUMERIC(12,2) NOT NULL CHECK (valor > 0)
);
