package com.urlshortener.domain.port.out;

import com.urlshortener.domain.model.ShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ShortUrlRepositoryPort {

    ShortUrl save(ShortUrl shortUrl);

    /** Searches both shortCode and customAlias — whichever matches. */
    Optional<ShortUrl> findByCode(String code);

    Optional<ShortUrl> findById(UUID id);

    Page<ShortUrl> findByUserId(UUID userId, Pageable pageable);

    boolean existsByCode(String code);

    void deleteById(UUID id);
}
