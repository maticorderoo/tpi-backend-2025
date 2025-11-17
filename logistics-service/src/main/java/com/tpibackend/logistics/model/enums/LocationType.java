package com.tpibackend.logistics.model.enums;

public enum LocationType {
    // backward-compatible aliases (used by older tests/code)
    ORIGEN,
    DESTINO,

    // canonical names
    ORIGEN_SOLICITUD,
    DESTINO_SOLICITUD,
    DEPOSITO,
    SOLICITUD,
    PUNTO_INTERMEDIO
}
