package com.urlshortener.infrastructure.persistence.adapter;

import com.urlshortener.domain.model.AccessLog;
import com.urlshortener.domain.port.out.AccessLogRepositoryPort;
import com.urlshortener.infrastructure.persistence.mapper.AccessLogEntityMapper;
import com.urlshortener.infrastructure.persistence.repository.AccessLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessLogRepositoryAdapter implements AccessLogRepositoryPort {

    private final AccessLogJpaRepository jpaRepository;
    private final AccessLogEntityMapper  mapper;

    @Override
    public void save(AccessLog accessLog) {
        jpaRepository.save(mapper.toEntity(accessLog));
    }
}
