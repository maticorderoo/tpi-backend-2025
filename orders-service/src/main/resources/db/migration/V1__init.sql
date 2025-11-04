CREATE TABLE clientes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telefono VARCHAR(30)
);

CREATE TABLE contenedores (
    id SERIAL PRIMARY KEY,
    peso NUMERIC(15,2) NOT NULL,
    volumen NUMERIC(15,2) NOT NULL,
    estado VARCHAR(100) NOT NULL,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id)
);

CREATE TABLE solicitudes (
    id SERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    contenedor_id BIGINT NOT NULL UNIQUE REFERENCES contenedores(id),
    estado VARCHAR(30) NOT NULL,
    costo_estimado NUMERIC(15,2),
    tiempo_estimado_minutos BIGINT,
    costo_final NUMERIC(15,2),
    tiempo_real_minutos BIGINT,
    estadia_estimada NUMERIC(15,2),
    origen VARCHAR(255),
    destino VARCHAR(255),
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE solicitud_eventos (
    id SERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitudes(id) ON DELETE CASCADE,
    estado VARCHAR(30) NOT NULL,
    fecha_evento TIMESTAMP WITH TIME ZONE NOT NULL,
    descripcion VARCHAR(255)
);

CREATE INDEX idx_solicitudes_contenedor ON solicitudes(contenedor_id);
CREATE INDEX idx_solicitudes_estado ON solicitudes(estado);
CREATE INDEX idx_eventos_solicitud ON solicitud_eventos(solicitud_id);
