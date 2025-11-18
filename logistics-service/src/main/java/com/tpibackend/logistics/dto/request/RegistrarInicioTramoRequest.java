package com.tpibackend.logistics.dto.request;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RegistrarInicioTramoRequest(OffsetDateTime fechaHoraInicio) {

	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	public static RegistrarInicioTramoRequest from(@JsonProperty("fechaHoraInicio") Object fechaHoraInicio) {
		if (fechaHoraInicio == null) {
			return new RegistrarInicioTramoRequest(null);
		}

		try {
			if (fechaHoraInicio instanceof OffsetDateTime odt) {
				return new RegistrarInicioTramoRequest(odt);
			}
			if (fechaHoraInicio instanceof String s) {
				// Try parse as OffsetDateTime first
				try {
					return new RegistrarInicioTramoRequest(OffsetDateTime.parse(s));
				} catch (DateTimeParseException e) {
					// Try LocalDateTime (no offset provided) and assume system default zone
					try {
						LocalDateTime ldt = LocalDateTime.parse(s);
						ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(ldt);
						return new RegistrarInicioTramoRequest(OffsetDateTime.of(ldt, offset));
					} catch (DateTimeParseException ex2) {
						// Not a LocalDateTime either -> fallthrough to next
					}
				}
			}
			if (fechaHoraInicio instanceof Number n) {
				// timestamp in milliseconds or seconds
				long v = n.longValue();
				Instant inst = (String.valueOf(v).length() > 10) ? Instant.ofEpochMilli(v) : Instant.ofEpochSecond(v);
				return new RegistrarInicioTramoRequest(OffsetDateTime.ofInstant(inst, ZoneId.systemDefault()));
			}
		} catch (Exception e) {
			// ignore and return null payload (service will fallback to now())
		}

		return new RegistrarInicioTramoRequest(null);
	}

}
