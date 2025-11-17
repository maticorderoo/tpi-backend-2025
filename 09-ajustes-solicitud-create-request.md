# 09 - Ajustes sobre el contrato de creación de solicitudes

## Campos definitivos del body `POST /orders`
- `cliente` → objeto con `nombre`, `email`, `telefono`. Se reutiliza cliente existente si viene `id`.
- `contenedor` → objeto con `id` (opcional), `peso`, `volumen` y `codigo` opcional cuando se quiere reutilizar uno existente.
- `origen` / `destino` → descripción textual obligatoria.
- `origenLat`, `origenLng`, `destinoLat`, `destinoLng` → coordenadas obligatorias del planificador.

> El cliente **no** envía `estadiaEstimada`, costos ni tiempos. Logistics calculará y actualizará esos valores cuando corresponda.

## Mapeo contra la entidad `Solicitud`
| Campo request | Campo entidad | Notas |
|---------------|---------------|-------|
| `cliente` | `solicitud.cliente` | Se crea o reusa el cliente antes de persistir la solicitud. |
| `contenedor` | `solicitud.contenedor` | Se reutiliza el contenedor existente si se informa `id`/`codigo`; de lo contrario se genera código propio. |
| `origen` | `solicitud.origen` | Persistido como `NOT NULL`. |
| `origenLat` | `solicitud.origenLat` | `double precision NOT NULL`. |
| `origenLng` | `solicitud.origenLng` | `double precision NOT NULL`. |
| `destino` | `solicitud.destino` | Persistido como `NOT NULL`. |
| `destinoLat` | `solicitud.destinoLat` | `double precision NOT NULL`. |
| `destinoLng` | `solicitud.destinoLng` | `double precision NOT NULL`. |

Los campos económicos (`costoEstimado`, `tiempoEstimado`, `estadiaEstimada`, etc.) permanecen en la entidad pero solo Logistics los actualiza a través de los procesos internos; no forman parte del request del cliente.

## Ejemplo de request válido
```json
{
  "cliente": {
    "nombre": "ACME Corp",
    "email": "contacto@acme.com",
    "telefono": "+54 11 5555-1111"
  },
  "contenedor": {
    "peso": 1200.5,
    "volumen": 28.4
  },
  "origen": "Buenos Aires, Puerto Madero",
  "origenLat": -34.6037,
  "origenLng": -58.3816,
  "destino": "Córdoba, barrio Güemes",
  "destinoLat": -31.4201,
  "destinoLng": -64.1888
}
```

## Resultado
- `SolicitudCreateRequest` valida que cada coordenada esté presente y elimina el campo `estadiaEstimada`.
- La entidad `Solicitud` y el schema (`initdb` + Flyway) marcan origen/destino y coordenadas como `NOT NULL`.
- Tests, colección Postman y guía documentan este contrato y no incluyen `estadiaEstimada` para clientes.
