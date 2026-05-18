package com.urlshortener.infrastructure.persistence.repository;

import com.urlshortener.infrastructure.persistence.entity.ShortUrlEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShortUrlJpaRepository extends JpaRepository<ShortUrlEntity, UUID> {

    /**
     * Searches the code against both shortCode and customAlias columns.
     * Caller passes the same value for both parameters: findByShortCodeOrCustomAlias(code, code)
     */
    Optional<ShortUrlEntity> findByShortCodeOrCustomAlias(String shortCode, String customAlias);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String customAlias);

    Page<ShortUrlEntity> findByUserIdAndActiveTrue(UUID userId, Pageable pageable);
}
