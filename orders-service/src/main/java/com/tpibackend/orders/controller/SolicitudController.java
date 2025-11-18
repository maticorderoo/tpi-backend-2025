package com.tpibackend.orders.controller;

import com.tpibackend.orders.dto.request.SolicitudCostoUpdateRequest;
import com.tpibackend.orders.dto.request.SolicitudCreateRequest;
import com.tpibackend.orders.dto.response.SeguimientoResponseDto;
import com.tpibackend.orders.dto.response.SolicitudResponseDto;
import com.tpibackend.orders.service.SolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@Validated
@Tag(name = "Solicitudes", description = "Gesti\u00f3n de solicitudes de transporte")
@SecurityRequirement(name = "bearerAuth")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(
            summary = "Crear una nueva solicitud",
            description = "Registra una solicitud nueva creando el cliente y el contenedor en caso de que no existan. Requiere rol CLIENTE.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SolicitudCreateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "crearSolicitudFull",
                                            summary = "Solicitud de traslado con cliente completo",
                                            value = "{\n  \"cliente\": {\n    \"nombre\": \"ACME Corp\",\n    \"email\": \"contacto@acme.com\",\n    \"telefono\": \"+54 11 5555-1111\",\n    \"cuit\": \"30-12345678-9\"\n  },\n  \"contenedor\": {\n    \"codigo\": \"CONT-XYZ-1234\",\n    \"peso\": 1200.5,\n    \"volumen\": 28.4\n  },\n  \"origen\": {\n    \"direccion\": \"Buenos Aires\",\n    \"latitud\": -34.6037,\n    \"longitud\": -58.3816\n  },\n  \"destino\": {\n    \"direccion\": \"C\u00f3rdoba\",\n    \"latitud\": -31.4201,\n    \"longitud\": -64.1888\n  }\n}"
                                    ),
                                    @ExampleObject(
                                            name = "crearSolicitudSoloCuit",
                                            summary = "Solicitud usando cliente existente (solo cuit)",
                                            value = "{\n  \"cliente\": {\n    \"cuit\": \"30-12345678-9\"\n  },\n  \"contenedor\": {\n    \"codigo\": \"CONT-XYZ-1234\",\n    \"peso\": 1200.5,\n    \"volumen\": 28.4\n  },\n  \"origen\": {\n    \"direccion\": \"Buenos Aires\",\n    \"latitud\": -34.6037,\n    \"longitud\": -58.3816\n  },\n  \"destino\": {\n    \"direccion\": \"C\u00f3rdoba\",\n    \"latitud\": -31.4201,\n    \"longitud\": -64.1888\n  }\n}"
                                    )
                            }
                    )
            )
            ,
            responses = {
                    @ApiResponse(responseCode = "201", description = "Solicitud creada",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SolicitudResponseDto.class),
                                    examples = @ExampleObject(name = "solicitudCreada",
                                            summary = "Solicitud creada correctamente",
                                            value = "{\n" +
"  \"cliente\": {\n" +
"    \"nombre\": \"ACME Corp\",\n" +
"    \"email\": \"contacto@acme.com\",\n" +
"    \"telefono\": \"+54 11 5555-1111\",\n" +
"    \"cuit\": \"30-12345678-9\"\n" +
"  },\n" +
"  \"contenedor\": {\n" +
"    \"codigo\": \"CONT-XYZ-1234\",\n" +
"    \"peso\": 1200.5,\n" +
"    \"volumen\": 28.4\n" +
"  },\n" +
"  \"origen\": {\n" +
"    \"direccion\": \"Buenos Aires\",\n" +
"    \"latitud\": -34.6037,\n" +
"    \"longitud\": -58.3816\n" +
"  },\n" +
"  \"destino\": {\n" +
"    \"direccion\": \"Córdoba\",\n" +
"    \"latitud\": -31.4201,\n" +
"    \"longitud\": -64.1888\n" +
"  }\n" +
"}"))),
                    @ApiResponse(responseCode = "400", description = "Datos inv\u00e1lidos"),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}")))
            }
    )
    public ResponseEntity<SolicitudResponseDto> crearSolicitud(@Valid @RequestBody SolicitudCreateRequest request) {
        SolicitudResponseDto response = solicitudService.crearSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR')")
    @Operation(summary = "Listar solicitudes",
            description = "Devuelve las solicitudes del cliente autenticado o todas si el rol es OPERADOR.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado recuperado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @io.swagger.v3.oas.annotations.media.ArraySchema(
                                            schema = @Schema(implementation = SolicitudResponseDto.class))))
            })
    public ResponseEntity<List<SolicitudResponseDto>> listarSolicitudes() {
        return ResponseEntity.ok(solicitudService.listarSolicitudes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR')")
    @Operation(summary = "Obtener detalle de la solicitud",
            description = "Recupera el detalle de una solicitud existente. Requiere rol CLIENTE u OPERADOR.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Solicitud encontrada",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SolicitudResponseDto.class),
                                    examples = @ExampleObject(name = "solicitudDetalle",
                                            value = "{\n  \"id\": 42,\n  \"estado\": \"PROGRAMADA\",\n  \"costoEstimado\": 150000,\n  \"tiempoEstimadoMinutos\": 720,\n  \"costoFinal\": 185000,\n  \"tiempoRealMinutos\": 810,\n  \"origen\": \"Buenos Aires\",\n  \"origenLat\": -34.6037,\n  \"origenLng\": -58.3816,\n  \"destino\": \"C\u00f3rdoba\",\n  \"destinoLat\": -31.4201,\n  \"destinoLng\": -64.1888,\n  \"fechaCreacion\": \"2025-11-16T14:30:00Z\",\n  \"cliente\": {\n    \"id\": 15,\n    \"nombre\": \"ACME Corp\"\n  },\n  \"contenedor\": {\n    \"id\": 9,\n    \"codigo\": \"CONT-0009\",\n    \"peso\": 1200.5,\n    \"volumen\": 28.4,\n    \"estado\": \"PROGRAMADA\"\n  },\n  \"rutaResumen\": {\n    \"rutaId\": 21,\n    \"estado\": \"ASIGNADA\",\n    \"tramos\": 3\n  }\n}"))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
            })
    public ResponseEntity<SolicitudResponseDto> obtenerSolicitud(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerSolicitud(id));
    }

    @GetMapping("/{id}/tracking")
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR')")
    @Operation(summary = "Obtener seguimiento de una solicitud por contenedor",
            description = "Devuelve el estado del contenedor y la ruta asociada a una solicitud. Requiere rol CLIENTE u OPERADOR.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Seguimiento recuperado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SeguimientoResponseDto.class),
                                    examples = @ExampleObject(name = "seguimiento",
                                            value = "{\n  \"contenedorId\": 9,\n  \"solicitudId\": 42,\n  \"estadoContenedor\": \"EN_TRANSITO\",\n  \"ruta\": {\n    \"id\": 21,\n    \"cantTramos\": 3,\n    \"costoTotalAprox\": 180000\n  }\n}"))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "404", description = "Contenedor no encontrado")
            })
    public ResponseEntity<SeguimientoResponseDto> obtenerSeguimiento(@PathVariable("id") Long id) {
        return ResponseEntity.ok(solicitudService.obtenerSeguimientoPorContenedor(id));
    }

    @PutMapping("/{id}/costo")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar costo final de una solicitud",
            description = "Actualiza el costo y tiempo real de la solicitud. Requiere rol OPERADOR.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SolicitudCostoUpdateRequest.class),
                    examples = @ExampleObject(name = "actualizarCosto",
                            value = "{\n  \"costoFinal\": 185000,\n  \"tiempoRealMinutos\": 810\n}"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Costo actualizado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SolicitudResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "No autenticado",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "unauthorized",
                                            value = "{\"error\":\"unauthorized\"}"))),
                    @ApiResponse(responseCode = "403", description = "Acceso prohibido",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = @ExampleObject(name = "forbidden",
                                            value = "{\"error\":\"forbidden\"}"))),
                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
                    @ApiResponse(responseCode = "409", description = "Estado inválido para cierre")
            })
    public ResponseEntity<SolicitudResponseDto> actualizarCosto(
        @PathVariable Long id,
        @Valid @RequestBody SolicitudCostoUpdateRequest request
    ) {
        return ResponseEntity.ok(solicitudService.actualizarCosto(id, request));
    }

}

