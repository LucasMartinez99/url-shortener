package com.urlshortener.infrastructure.persistence.adapter;

import com.urlshortener.domain.model.User;
import com.urlshortener.domain.port.out.UserRepositoryPort;
import com.urlshortener.infrastructure.persistence.mapper.UserEntityMapper;
import com.urlshortener.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter — bridges the domain output port to the JPA repository.
 * The domain service calls UserRepositoryPort; Spring injects this adapter.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;
    private final UserEntityMapper  mapper;

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
