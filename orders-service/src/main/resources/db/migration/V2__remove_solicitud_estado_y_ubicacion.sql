-- Migración Flyway para alinear la entidad Solicitud con el nuevo modelo
-- Elimina las columnas de estado y ubicación de la tabla solicitudes.

ALTER TABLE solicitudes
    DROP COLUMN IF EXISTS estado;

ALTER TABLE solicitudes
    DROP COLUMN IF EXISTS origen;

ALTER TABLE solicitudes
    DROP COLUMN IF EXISTS origen_lat;

ALTER TABLE solicitudes
    DROP COLUMN IF EXISTS origen_lng;

ALTER TABLE solicitudes
    DROP COLUMN IF EXISTS destino;

ALTER TABLE solicitudes
    DROP COLUMN IF EXISTS destino_lat;

ALTER TABLE solicitudes
    DROP COLUMN IF EXISTS destino_lng;

