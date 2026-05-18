package com.urlshortener.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AccessLog {

    private final UUID          id;
    private final UUID          shortUrlId;
    private final LocalDateTime accessedAt;
    private final String        ipAddress;
    private final String        userAgent;

    public AccessLog(UUID id, UUID shortUrlId, LocalDateTime accessedAt,
                     String ipAddress, String userAgent) {
        this.id         = id;
        this.shortUrlId = shortUrlId;
        this.accessedAt = accessedAt;
        this.ipAddress  = ipAddress;
        this.userAgent  = userAgent;
    }
}
