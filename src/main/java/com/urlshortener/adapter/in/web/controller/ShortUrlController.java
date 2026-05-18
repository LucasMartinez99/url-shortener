package com.urlshortener.adapter.in.web.controller;

import com.urlshortener.adapter.in.web.dto.request.CreateShortUrlRequest;
import com.urlshortener.adapter.in.web.dto.response.PageResponse;
import com.urlshortener.adapter.in.web.dto.response.ShortUrlResponse;
import com.urlshortener.adapter.in.web.mapper.ShortUrlWebMapper;
import com.urlshortener.domain.model.ShortUrl;
import com.urlshortener.domain.port.in.CreateShortUrlUseCase;
import com.urlshortener.domain.port.in.CreateShortUrlUseCase.CreateShortUrlCommand;
import com.urlshortener.domain.port.in.DeleteShortUrlUseCase;
import com.urlshortener.domain.port.in.GetUrlAnalyticsUseCase;
import com.urlshortener.domain.port.in.GetUserUrlsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
@Tag(name = "URLs")
@SecurityRequirement(name = "bearerAuth")
public class ShortUrlController {

    private final CreateShortUrlUseCase  createShortUrlUseCase;
    private final DeleteShortUrlUseCase  deleteShortUrlUseCase;
    private final GetUserUrlsUseCase     getUserUrlsUseCase;
    private final GetUrlAnalyticsUseCase getUrlAnalyticsUseCase;
    private final ShortUrlWebMapper      mapper;
    private final UserLookupHelper       userLookup;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a short URL")
    public ShortUrlResponse create(
            @Valid @RequestBody CreateShortUrlRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        UUID userId = userLookup.resolveId(principal.getUsername());
        ShortUrl shortUrl = createShortUrlUseCase.create(new CreateShortUrlCommand(
                userId,
                request.originalUrl(),
                request.customAlias(),
                request.expiresAt()
        ));
        return mapper.toResponse(shortUrl, baseUrl);
    }

    @GetMapping
    @Operation(summary = "List all URLs for the authenticated user (paginated)")
    public PageResponse<ShortUrlResponse> list(
            @AuthenticationPrincipal UserDetails principal,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        UUID userId = userLookup.resolveId(principal.getUsername());
        Page<ShortUrl> page = getUserUrlsUseCase.getUserUrls(userId, pageable);
        return PageResponse.from(page.map(url -> mapper.toResponse(url, baseUrl)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a URL")
    public void delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        UUID userId = userLookup.resolveId(principal.getUsername());
        deleteShortUrlUseCase.delete(id, userId);
    }

    @GetMapping("/{id}/analytics")
    @Operation(summary = "Get click analytics for a URL")
    public ShortUrlResponse analytics(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        UUID userId = userLookup.resolveId(principal.getUsername());
        ShortUrl shortUrl = getUrlAnalyticsUseCase.getAnalytics(id, userId);
        return mapper.toResponse(shortUrl, baseUrl);
    }
}
