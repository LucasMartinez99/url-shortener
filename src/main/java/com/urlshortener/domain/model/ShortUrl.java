package com.urlshortener.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core aggregate of the application.
 *
 * Domain behaviour lives here — business rules like isExpired() and
 * withRecordedClick() encode invariants that exist regardless of framework.
 *
 * withRecordedClick() returns a NEW instance (immutable update pattern),
 * keeping the object safe to share across threads without defensive copying.
 */
@Getter
@Builder(toBuilder = true)
public class ShortUrl {

    private final UUID          id;
    private final String        originalUrl;
    private final String        shortCode;
    private final String        customAlias;     // nullable
    private final UUID          userId;
    private final LocalDateTime expiresAt;       // nullable = never expires
    private final LocalDateTime createdAt;
    private final LocalDateTime lastAccessedAt;
    private final long          clickCount;
    private final boolean       active;

    public ShortUrl(UUID id, String originalUrl, String shortCode, String customAlias,
                    UUID userId, LocalDateTime expiresAt, LocalDateTime createdAt,
                    LocalDateTime lastAccessedAt, long clickCount, boolean active) {
        this.id             = id;
        this.originalUrl    = originalUrl;
        this.shortCode      = shortCode;
        this.customAlias    = customAlias;
        this.userId         = userId;
        this.expiresAt      = expiresAt;
        this.createdAt      = createdAt;
        this.lastAccessedAt = lastAccessedAt;
        this.clickCount     = clickCount;
        this.active         = active;
    }

    // ── Domain behaviour ──────────────────────────────────────────────────────

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /** Returns customAlias when set, otherwise falls back to the generated shortCode. */
    public String getEffectiveCode() {
        return (customAlias != null && !customAlias.isBlank()) ? customAlias : shortCode;
    }

    /** Immutable update: returns a new ShortUrl with incremented click count and updated access time. */
    public ShortUrl withRecordedClick() {
        return this.toBuilder()
                .clickCount(this.clickCount + 1)
                .lastAccessedAt(LocalDateTime.now())
                .build();
    }
}
