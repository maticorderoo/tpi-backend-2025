package com.tpibackend.logistics.dto.request;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RegistrarFinTramoRequest(OffsetDateTime fechaHoraFin) {

	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	public static RegistrarFinTramoRequest from(@JsonProperty("fechaHoraFin") Object fechaHoraFin) {
		if (fechaHoraFin == null) {
			return new RegistrarFinTramoRequest(null);
		}

		try {
			if (fechaHoraFin instanceof OffsetDateTime odt) {
				return new RegistrarFinTramoRequest(odt);
			}
			if (fechaHoraFin instanceof String s) {
				try {
					return new RegistrarFinTramoRequest(OffsetDateTime.parse(s));
				} catch (DateTimeParseException e) {
					try {
						LocalDateTime ldt = LocalDateTime.parse(s);
						ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(ldt);
						return new RegistrarFinTramoRequest(OffsetDateTime.of(ldt, offset));
					} catch (DateTimeParseException ex2) {
						// fallthrough
					}
				}
			}
			if (fechaHoraFin instanceof Number n) {
				long v = n.longValue();
				Instant inst = (String.valueOf(v).length() > 10) ? Instant.ofEpochMilli(v) : Instant.ofEpochSecond(v);
				return new RegistrarFinTramoRequest(OffsetDateTime.ofInstant(inst, ZoneId.systemDefault()));
			}
		} catch (Exception e) {
			// ignore and fallback to null
		}

		return new RegistrarFinTramoRequest(null);
	}

}
