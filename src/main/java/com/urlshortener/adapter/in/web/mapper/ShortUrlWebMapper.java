package com.urlshortener.adapter.in.web.mapper;

import com.urlshortener.adapter.in.web.dto.response.ShortUrlResponse;
import com.urlshortener.domain.model.ShortUrl;
import org.springframework.stereotype.Component;

@Component
public class ShortUrlWebMapper {

    public ShortUrlResponse toResponse(ShortUrl shortUrl, String baseUrl) {
        String fullShortUrl = baseUrl + "/" + shortUrl.getEffectiveCode();
        return new ShortUrlResponse(
                shortUrl.getId(),
                shortUrl.getOriginalUrl(),
                shortUrl.getShortCode(),
                shortUrl.getCustomAlias(),
                fullShortUrl,
                shortUrl.getExpiresAt(),
                shortUrl.getCreatedAt(),
                shortUrl.getLastAccessedAt(),
                shortUrl.getClickCount(),
                shortUrl.isActive()
        );
    }
}
