package com.urlshortener.unit.service;

import com.urlshortener.application.service.ShortUrlService;
import com.urlshortener.domain.exception.AliasAlreadyExistsException;
import com.urlshortener.domain.exception.UnauthorizedException;
import com.urlshortener.domain.exception.UrlExpiredException;
import com.urlshortener.domain.model.ShortUrl;
import com.urlshortener.domain.port.in.CreateShortUrlUseCase.CreateShortUrlCommand;
import com.urlshortener.domain.port.in.RedirectUrlUseCase.ResolveUrlCommand;
import com.urlshortener.domain.port.out.AccessLogRepositoryPort;
import com.urlshortener.domain.port.out.ShortCodeGeneratorPort;
import com.urlshortener.domain.port.out.ShortUrlRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests — no Spring context, no database.
 * All dependencies are mocked. Tests run in milliseconds.
 */
@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    @Mock private ShortUrlRepositoryPort shortUrlRepository;
    @Mock private AccessLogRepositoryPort accessLogRepository;
    @Mock private ShortCodeGeneratorPort  shortCodeGenerator;

    @InjectMocks
    private ShortUrlService service;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("should generate a short code when no custom alias is provided")
    void create_withNoAlias_shouldGenerateCode() {
        when(shortCodeGenerator.generate()).thenReturn("abc1234");
        when(shortUrlRepository.existsByCode("abc1234")).thenReturn(false);
        when(shortUrlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateShortUrlCommand cmd = new CreateShortUrlCommand(userId, "https://example.com", null, null);
        ShortUrl result = service.create(cmd);

        assertThat(result.getShortCode()).isEqualTo("abc1234");
        assertThat(result.getCustomAlias()).isNull();
        verify(shortCodeGenerator).generate();
    }

    @Test
    @DisplayName("should use custom alias when provided and alias is available")
    void create_withAvailableAlias_shouldUseAlias() {
        when(shortCodeGenerator.generate()).thenReturn("auto123");
        when(shortUrlRepository.existsByCode("auto123")).thenReturn(false);
        when(shortUrlRepository.existsByCode("my-link")).thenReturn(false);
        when(shortUrlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateShortUrlCommand cmd = new CreateShortUrlCommand(userId, "https://example.com", "my-link", null);
        ShortUrl result = service.create(cmd);

        assertThat(result.getCustomAlias()).isEqualTo("my-link");
        assertThat(result.getShortCode()).isEqualTo("auto123");
    }

    @Test
    @DisplayName("should throw AliasAlreadyExistsException when alias is taken")
    void create_withTakenAlias_shouldThrow() {
        when(shortCodeGenerator.generate()).thenReturn("auto123");
        when(shortUrlRepository.existsByCode("auto123")).thenReturn(false);
        when(shortUrlRepository.existsByCode("taken")).thenReturn(true);

        CreateShortUrlCommand cmd = new CreateShortUrlCommand(userId, "https://example.com", "taken", null);
        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(AliasAlreadyExistsException.class);
    }

    @Test
    @DisplayName("should retry code generation on collision")
    void create_withCollision_shouldRetry() {
        when(shortCodeGenerator.generate()).thenReturn("clash11", "unique2");
        when(shortUrlRepository.existsByCode("clash11")).thenReturn(true);
        when(shortUrlRepository.existsByCode("unique2")).thenReturn(false);
        when(shortUrlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ShortUrl result = service.create(new CreateShortUrlCommand(userId, "https://x.com", null, null));

        assertThat(result.getShortCode()).isEqualTo("unique2");
        verify(shortCodeGenerator, times(2)).generate();
    }

    @Test
    @DisplayName("should resolve URL and record a click")
    void resolveUrl_valid_shouldReturnOriginalUrlAndLog() {
        ShortUrl shortUrl = buildActiveShortUrl(null);
        when(shortUrlRepository.findByCode("abc1234")).thenReturn(Optional.of(shortUrl));
        when(shortUrlRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String result = service.resolveUrl(new ResolveUrlCommand("abc1234", "127.0.0.1", "TestAgent"));

        assertThat(result).isEqualTo("https://example.com");
        verify(accessLogRepository).save(any());
    }

    @Test
    @DisplayName("should throw UrlExpiredException for expired URLs")
    void resolveUrl_expired_shouldThrow() {
        ShortUrl expired = buildActiveShortUrl(LocalDateTime.now().minusDays(1));
        when(shortUrlRepository.findByCode("expired1")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.resolveUrl(new ResolveUrlCommand("expired1", null, null)))
                .isInstanceOf(UrlExpiredException.class);
    }

    @Test
    @DisplayName("should throw UnauthorizedException when deleting someone else's URL")
    void delete_notOwner_shouldThrow() {
        ShortUrl shortUrl = buildActiveShortUrl(null);
        when(shortUrlRepository.findById(shortUrl.getId())).thenReturn(Optional.of(shortUrl));

        UUID differentUserId = UUID.randomUUID();
        assertThatThrownBy(() -> service.delete(shortUrl.getId(), differentUserId))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private ShortUrl buildActiveShortUrl(LocalDateTime expiresAt) {
        return ShortUrl.builder()
                .id(UUID.randomUUID())
                .originalUrl("https://example.com")
                .shortCode("abc1234")
                .userId(userId)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .clickCount(0)
                .active(true)
                .build();
    }
}
