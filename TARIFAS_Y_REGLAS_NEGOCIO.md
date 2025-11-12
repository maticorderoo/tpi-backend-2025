# Cálculo de Tarifas - Reglas de Negocio

## Fórmula de Cálculo de Tarifa Final

La tarifa final del envío se calcula sumando:

### 1. Cargos de Gestión (Valor Fijo)
```
Cargo de Gestión = TARIFA_CARGO_GESTION_POR_TRAMO × cantidad_de_tramos
```

**Configuración requerida:**
- El OPERADOR debe crear una tarifa con `tipo = "CARGO_GESTION_POR_TRAMO"` en fleet-service
- Ejemplo: `POST /tarifas {"tipo": "CARGO_GESTION_POR_TRAMO", "valor": 5000}`

### 2. Costo por Kilómetro de cada Camión
```
Costo KM = costoKmBase_del_camión × distancia_real_km
```

- Cada camión tiene su propio `costoKmBase` diferenciado según capacidad
- Se registra al crear/actualizar el camión

### 3. Costo de Combustible
```
Costo Combustible = (consumoLKm_del_camión × distancia_real_km) × precio_combustible
```

**Componentes:**
- `consumoLKm`: Consumo específico del camión en litros/km
- `distancia_real_km`: Distancia real recorrida en el tramo
- `precio_combustible`: Precio por litro configurado como tarifa

**Configuración requerida:**
- El OPERADOR debe crear una tarifa con `tipo = "PRECIO_COMBUSTIBLE"` 
- Ejemplo: `POST /tarifas {"tipo": "PRECIO_COMBUSTIBLE", "valor": 750}`

### 4. Costo por Estadía en Depósito
```
Costo Estadía = costoEstadiaDia_del_deposito × días_de_estadía
```

- Cada depósito define su `costoEstadiaDia`
- Los días de estadía se calculan entre la entrada y salida del tramo

## Fórmula Completa por Tramo

```
Costo_Tramo = (costoKmBase × distancia) + 
              (consumoLKm × distancia × precioCombustible) + 
              (costoEstadiaDia × diasEstadia)
```

## Fórmula Total de la Ruta

```
Costo_Total = SUMA(Costo_Tramo_1...N) + (CARGO_GESTION_POR_TRAMO × N)
```

Donde N = cantidad de tramos

## Costos Diferenciados por Camión

Los camiones tienen costos diferenciados basados en sus capacidades:

| Capacidad | Rango Peso (kg) | Rango Volumen (m³) | Costo Base por KM Sugerido |
|-----------|-----------------|--------------------|-----------------------------|
| Pequeño   | 0 - 5,000      | 0 - 20            | $800 - $900                 |
| Mediano   | 5,001 - 15,000 | 21 - 40           | $950 - $1,100               |
| Grande    | 15,001 - 30,000| 41 - 80           | $1,200 - $1,500             |

**Nota:** Estos son valores sugeridos. El OPERADOR define el `costoKmBase` al crear cada camión.

## Tarifa Aproximada (Estimación)

Para calcular la tarifa aproximada se utilizan **promedios** de los camiones elegibles:

1. Se filtran los camiones que soporten el peso y volumen del contenedor
2. Se calcula el promedio de `costoKmBase` entre esos camiones
3. Se calcula el promedio de `consumoLKm` entre esos camiones
4. Se usa el endpoint `GET /metrics/promedios` de fleet-service

**Ejemplo de uso:**
```
GET /metrics/promedios
Response: {
  "consumoPromedio": 0.31,
  "costoKmPromedio": 940
}
```

Estos promedios se usan en la estimación inicial antes de asignar camiones específicos.

## Tiempo Estimado

El tiempo estimado se calcula en base a:

1. **Distancias entre puntos**: Se usa distance-client (Google Maps) para calcular:
   - Origen → Depósito 1
   - Depósito 1 → Depósito 2
   - Depósito N → Destino

2. **Tiempos de viaje**: distance-client retorna tiempo estimado en minutos

3. **Tiempos de estadía**: Se suma el tiempo de permanencia en cada depósito (en días convertidos a minutos)

## Seguimiento Cronológico

Los eventos de seguimiento se almacenan en la tabla `solicitud_eventos` con:
- `estado`: Estado del contenedor en ese momento
- `fecha_evento`: Timestamp del cambio
- `descripcion`: Descripción del evento

Los eventos se ordenan cronológicamente por `fecha_evento ASC` al consultar el seguimiento.

## Fechas Estimadas y Reales en Tramos

Cada tramo registra:

### Fechas Estimadas (en planificación)
- `distancia_km_estimada`: Distancia calculada con distance-client
- Tiempo estimado se calcula automáticamente

### Fechas Reales (en ejecución)
- `fecha_hora_inicio`: Cuando el TRANSPORTISTA inicia el tramo
- `fecha_hora_fin`: Cuando el TRANSPORTISTA finaliza el tramo
- `distancia_km_real`: Distancia real recorrida
- `dias_estadia`: Días reales de permanencia en depósito

Estas fechas permiten calcular el desempeño del servicio comparando tiempos estimados vs reales.

## Validación de Capacidad

**Regla obligatoria:** Un camión NO puede transportar contenedores que superen su capacidad.

Validaciones automáticas al asignar camión a tramo:
```java
if (camion.capacidadPeso < contenedor.peso) {
    throw new BusinessException("El camión no soporta el peso requerido");
}

if (camion.capacidadVolumen < contenedor.volumen) {
    throw new BusinessException("El camión no soporta el volumen requerido");
}
```

## Configuración Inicial Recomendada

Para que el sistema funcione correctamente, el OPERADOR debe:

1. **Crear tarifas básicas:**
   ```
   POST /tarifas {"tipo": "CARGO_GESTION_POR_TRAMO", "valor": 5000}
   POST /tarifas {"tipo": "PRECIO_COMBUSTIBLE", "valor": 750}
   ```

2. **Crear depósitos con costos de estadía:**
   ```
   POST /api/logistics/depositos {
     "nombre": "Depósito Central",
     "direccion": "...",
     "lat": -34.6037,
     "lng": -58.3816,
     "costoEstadiaDia": 15000
   }
   ```

3. **Crear camiones con costos diferenciados:**
   ```
   POST /api/trucks {
     "dominio": "AA123BB",
     "transportistaNombre": "...",
     "telefono": "...",
     "capPeso": 25000,
     "capVolumen": 60,
     "disponible": true,
     "costoKmBase": 1200,  // Diferenciado por capacidad
     "consumoLKm": 0.32
   }
   ```
