-- Garantiza que las solicitudes tengan origen/destino y coordenadas obligatorias
UPDATE solicitudes
SET origen = COALESCE(origen, 'ORIGEN_PENDIENTE'),
    destino = COALESCE(destino, 'DESTINO_PENDIENTE'),
    origen_lat = COALESCE(origen_lat, 0),
    origen_lng = COALESCE(origen_lng, 0),
    destino_lat = COALESCE(destino_lat, 0),
    destino_lng = COALESCE(destino_lng, 0);

ALTER TABLE solicitudes
    ALTER COLUMN origen SET NOT NULL,
    ALTER COLUMN destino SET NOT NULL,
    ALTER COLUMN origen_lat SET NOT NULL,
    ALTER COLUMN origen_lng SET NOT NULL,
    ALTER COLUMN destino_lat SET NOT NULL,
    ALTER COLUMN destino_lng SET NOT NULL;
