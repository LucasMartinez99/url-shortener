package com.urlshortener.domain.port.in;

import com.urlshortener.domain.model.ShortUrl;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UpdateShortUrlUseCase {

    ShortUrl update(UpdateShortUrlCommand command);

    record UpdateShortUrlCommand(
            UUID          shortUrlId,
            UUID          requestingUserId,
            String        originalUrl,    // null = keep current
            String        customAlias,    // null = keep current
            boolean       clearAlias,     // true = remove alias
            LocalDateTime expiresAt,      // null = keep current
            boolean       clearExpiry,    // true = remove expiration
            Boolean       active          // null = keep current
    ) {}
}
