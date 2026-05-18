package com.urlshortener.domain.port.in;

import com.urlshortener.domain.model.ShortUrl;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CreateShortUrlUseCase {

    ShortUrl create(CreateShortUrlCommand command);

    record CreateShortUrlCommand(
            UUID          userId,
            String        originalUrl,
            String        customAlias,   // null = auto-generate
            LocalDateTime expiresAt      // null = never expires
    ) {}
}
