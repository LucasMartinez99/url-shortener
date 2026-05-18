package com.urlshortener.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        @Email
        @Schema(example = "user@example.com")
        String email,

        @NotBlank
        @Schema(example = "securePassword123")
        String password
) {}
