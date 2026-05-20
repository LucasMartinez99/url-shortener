package com.urlshortener.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

public record UpdateShortUrlRequest(

        @URL(message = "Must be a valid URL")
        @Schema(description = "New destination URL", example = "https://new-destination.com")
        String originalUrl,

        @Size(min = 3, max = 30, message = "Alias must be between 3 and 30 characters")
        @Pattern(
                regexp  = "^[a-zA-Z0-9_-]*$",
                message = "Alias can only contain letters, numbers, hyphens, and underscores"
        )
        @Schema(description = "New custom alias — omit to keep current", example = "my-new-alias")
        String customAlias,

        @Schema(description = "Set to true to remove the custom alias")
        boolean clearAlias,

        @Future(message = "Expiration date must be in the future")
        @Schema(description = "New expiration date — omit to keep current", example = "2028-12-31T23:59:59")
        LocalDateTime expiresAt,

        @Schema(description = "Set to true to remove the expiration date")
        boolean clearExpiry,

        @Schema(description = "Activate or deactivate the link")
        Boolean active
) {}
