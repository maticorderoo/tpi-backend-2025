package com.tpibackend.orders.exception;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiError {
    OffsetDateTime timestamp;
    int status;
    String error;
    String message;
    String path;
}
