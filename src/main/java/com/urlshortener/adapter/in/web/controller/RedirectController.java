package com.urlshortener.adapter.in.web.controller;

import com.urlshortener.domain.port.in.RedirectUrlUseCase;
import com.urlshortener.domain.port.in.RedirectUrlUseCase.ResolveUrlCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Tag(name = "Redirect")
public class RedirectController {

    private final RedirectUrlUseCase redirectUrlUseCase;

    @GetMapping("/{code}")
    @Operation(summary = "Redirect to the original URL")
    public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletRequest request) {
        String originalUrl = redirectUrlUseCase.resolveUrl(new ResolveUrlCommand(
                code,
                extractClientIp(request),
                request.getHeader("User-Agent")
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
