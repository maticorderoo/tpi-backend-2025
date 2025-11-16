-- Agregar columna de identificaci\u00f3n de negocio para contenedores

ALTER TABLE contenedores
    ADD COLUMN IF NOT EXISTS codigo VARCHAR(50);

UPDATE contenedores
SET codigo = CONCAT('CONT-', id)
WHERE codigo IS NULL;

ALTER TABLE contenedores
    ALTER COLUMN codigo SET NOT NULL;

ALTER TABLE contenedores
    ADD CONSTRAINT ux_contenedores_codigo UNIQUE (codigo);

