package com.urlshortener.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

public record CreateShortUrlRequest(

        @NotBlank(message = "Original URL is required")
        @URL(message = "Must be a valid URL")
        @Schema(example = "https://example.com/very/long/path/to/something")
        String originalUrl,

        @Size(min = 3, max = 30, message = "Alias must be between 3 and 30 characters")
        @Pattern(
                regexp  = "^[a-zA-Z0-9_-]*$",
                message = "Alias can only contain letters, numbers, hyphens, and underscores"
        )
        @Schema(description = "Optional custom alias", example = "my-link")
        String customAlias,

        @Future(message = "Expiration date must be in the future")
        @Schema(description = "Optional expiration (ISO-8601)", example = "2027-12-31T23:59:59")
        LocalDateTime expiresAt
) {}
