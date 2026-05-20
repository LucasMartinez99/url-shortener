package com.urlshortener.infrastructure.persistence.adapter;

import com.urlshortener.domain.model.ShortUrl;
import com.urlshortener.domain.port.out.ShortUrlRepositoryPort;
import com.urlshortener.infrastructure.persistence.mapper.ShortUrlEntityMapper;
import com.urlshortener.infrastructure.persistence.repository.ShortUrlJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShortUrlRepositoryAdapter implements ShortUrlRepositoryPort {

    private final ShortUrlJpaRepository jpaRepository;
    private final ShortUrlEntityMapper  mapper;

    @Override
    public ShortUrl save(ShortUrl shortUrl) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(shortUrl)));
    }

    @Override
    public Optional<ShortUrl> findByCode(String code) {
        return jpaRepository.findByShortCodeOrCustomAlias(code, code).map(mapper::toDomain);
    }

    @Override
    public Optional<ShortUrl> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<ShortUrl> findByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserIdAndActiveTrue(userId, pageable).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByShortCode(code) || jpaRepository.existsByCustomAlias(code);
    }

    @Override
    public boolean existsByCodeExcludingId(String code, UUID excludeId) {
        return jpaRepository.existsByShortCodeAndIdNot(code, excludeId)
                || jpaRepository.existsByCustomAliasAndIdNot(code, excludeId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
