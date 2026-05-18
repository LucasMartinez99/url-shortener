package com.urlshortener.application.service;

import com.urlshortener.domain.exception.AliasAlreadyExistsException;
import com.urlshortener.domain.exception.ShortUrlNotFoundException;
import com.urlshortener.domain.exception.UnauthorizedException;
import com.urlshortener.domain.exception.UrlExpiredException;
import com.urlshortener.domain.model.AccessLog;
import com.urlshortener.domain.model.ShortUrl;
import com.urlshortener.domain.port.in.CreateShortUrlUseCase;
import com.urlshortener.domain.port.in.DeleteShortUrlUseCase;
import com.urlshortener.domain.port.in.GetUrlAnalyticsUseCase;
import com.urlshortener.domain.port.in.GetUserUrlsUseCase;
import com.urlshortener.domain.port.in.RedirectUrlUseCase;
import com.urlshortener.domain.port.out.AccessLogRepositoryPort;
import com.urlshortener.domain.port.out.ShortCodeGeneratorPort;
import com.urlshortener.domain.port.out.ShortUrlRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShortUrlService implements
        CreateShortUrlUseCase,
        RedirectUrlUseCase,
        DeleteShortUrlUseCase,
        GetUserUrlsUseCase,
        GetUrlAnalyticsUseCase {

    private final ShortUrlRepositoryPort shortUrlRepository;
    private final AccessLogRepositoryPort accessLogRepository;
    private final ShortCodeGeneratorPort  shortCodeGenerator;

    @Override
    @Transactional
    public ShortUrl create(CreateShortUrlCommand command) {
        // Always generate a system shortCode (unique, auto-generated)
        String shortCode;
        do {
            shortCode = shortCodeGenerator.generate();
        } while (shortUrlRepository.existsByCode(shortCode));

        // Validate custom alias separately if provided
        String customAlias = null;
        if (command.customAlias() != null && !command.customAlias().isBlank()) {
            if (shortUrlRepository.existsByCode(command.customAlias())) {
                throw new AliasAlreadyExistsException(command.customAlias());
            }
            customAlias = command.customAlias();
        }

        ShortUrl shortUrl = ShortUrl.builder()
                .id(UUID.randomUUID())
                .originalUrl(command.originalUrl())
                .shortCode(shortCode)
                .customAlias(customAlias)
                .userId(command.userId())
                .expiresAt(command.expiresAt())
                .createdAt(LocalDateTime.now())
                .clickCount(0)
                .active(true)
                .build();

        return shortUrlRepository.save(shortUrl);
    }

    @Override
    @Transactional
    public String resolveUrl(ResolveUrlCommand command) {
        ShortUrl shortUrl = shortUrlRepository.findByCode(command.code())
                .filter(ShortUrl::isActive)
                .orElseThrow(() -> new ShortUrlNotFoundException(command.code()));

        if (shortUrl.isExpired()) {
            throw new UrlExpiredException(command.code());
        }

        ShortUrl updated = shortUrl.withRecordedClick();
        shortUrlRepository.save(updated);

        AccessLog log = AccessLog.builder()
                .id(UUID.randomUUID())
                .shortUrlId(shortUrl.getId())
                .accessedAt(LocalDateTime.now())
                .ipAddress(command.ipAddress())
                .userAgent(command.userAgent())
                .build();
        accessLogRepository.save(log);

        return shortUrl.getOriginalUrl();
    }

    @Override
    @Transactional
    public void delete(UUID shortUrlId, UUID requestingUserId) {
        ShortUrl shortUrl = shortUrlRepository.findById(shortUrlId)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortUrlId.toString()));

        if (!shortUrl.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("You do not own this URL");
        }

        shortUrlRepository.deleteById(shortUrlId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShortUrl> getUserUrls(UUID userId, Pageable pageable) {
        return shortUrlRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ShortUrl getAnalytics(UUID shortUrlId, UUID requestingUserId) {
        ShortUrl shortUrl = shortUrlRepository.findById(shortUrlId)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortUrlId.toString()));

        if (!shortUrl.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("You do not own this URL");
        }

        return shortUrl;
    }
}
