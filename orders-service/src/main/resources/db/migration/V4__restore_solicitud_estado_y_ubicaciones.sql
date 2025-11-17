-- Reincorpora los campos de estado y coordenadas que exige el DER
ALTER TABLE solicitudes
    ADD COLUMN IF NOT EXISTS estado varchar(30);

UPDATE solicitudes SET estado = 'BORRADOR'
WHERE estado IS NULL;

ALTER TABLE solicitudes
    ALTER COLUMN estado SET NOT NULL;

ALTER TABLE solicitudes
    ADD COLUMN IF NOT EXISTS origen varchar(255);
ALTER TABLE solicitudes
    ADD COLUMN IF NOT EXISTS origen_lat double precision;
ALTER TABLE solicitudes
    ADD COLUMN IF NOT EXISTS origen_lng double precision;
ALTER TABLE solicitudes
    ADD COLUMN IF NOT EXISTS destino varchar(255);
ALTER TABLE solicitudes
    ADD COLUMN IF NOT EXISTS destino_lat double precision;
ALTER TABLE solicitudes
    ADD COLUMN IF NOT EXISTS destino_lng double precision;
