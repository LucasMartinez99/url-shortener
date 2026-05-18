package com.urlshortener.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pure domain object — no JPA, no Spring, no framework.
 * This is the business representation of a user.
 * The JPA entity (UserEntity) mirrors this but lives in the infrastructure layer.
 */
@Getter
@Builder(toBuilder = true)
public class User {

    private final UUID          id;
    private final String        email;
    private final String        password;   // always stored as BCrypt hash
    private final boolean       active;
    private final LocalDateTime createdAt;

    public User(UUID id, String email, String password, boolean active, LocalDateTime createdAt) {
        this.id        = id;
        this.email     = email;
        this.password  = password;
        this.active    = active;
        this.createdAt = createdAt;
    }
}
