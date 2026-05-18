package com.urlshortener.adapter.in.web.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        int           status,
        String        error,
        String        message,
        LocalDateTime timestamp
) {}
