package com.urlshortener.adapter.in.web.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShortUrlResponse(
        UUID          id,
        String        originalUrl,
        String        shortCode,
        String        customAlias,
        String        shortUrl,          // full clickable URL, e.g. http://localhost:8080/abc123
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime lastAccessedAt,
        long          clickCount,
        boolean       active
) {}
