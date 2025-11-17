-- Agrega CUIT obligatorio al cliente, observaciones en solicitudes y restringe
-- el uso de códigos de contenedor únicamente para registros activos.

ALTER TABLE clientes
    ADD COLUMN IF NOT EXISTS cuit VARCHAR(20);

UPDATE clientes
SET cuit = CONCAT('TMP-', id)
WHERE cuit IS NULL;

ALTER TABLE clientes
    ALTER COLUMN cuit SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ux_clientes_cuit'
    ) THEN
        ALTER TABLE clientes
            ADD CONSTRAINT ux_clientes_cuit UNIQUE (cuit);
    END IF;
END $$;

ALTER TABLE contenedores
    DROP CONSTRAINT IF EXISTS ux_contenedores_codigo;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_class WHERE relname = 'ux_contenedores_codigo_activos'
    ) THEN
        CREATE UNIQUE INDEX ux_contenedores_codigo_activos
            ON contenedores (codigo)
            WHERE estado NOT IN ('ENTREGADO','CANCELADO');
    END IF;
END $$;

ALTER TABLE solicitudes
    ADD COLUMN IF NOT EXISTS observaciones VARCHAR(500);
