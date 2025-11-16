package com.tpibackend.orders.mapper;

import com.tpibackend.orders.dto.response.ClienteResponseDto;
import com.tpibackend.orders.dto.response.ContenedorResponseDto;
import com.tpibackend.orders.dto.response.SolicitudResponseDto;
import com.tpibackend.orders.model.Cliente;
import com.tpibackend.orders.model.Contenedor;
import com.tpibackend.orders.model.Solicitud;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SolicitudMapper {

    @Mapping(target = "rutaResumen", ignore = true)
    SolicitudResponseDto toDto(Solicitud solicitud);

    ClienteResponseDto toDto(Cliente cliente);

    ContenedorResponseDto toDto(Contenedor contenedor);
}
