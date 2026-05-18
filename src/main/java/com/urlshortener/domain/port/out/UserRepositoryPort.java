package com.urlshortener.domain.port.out;

import com.urlshortener.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * OUTPUT PORT — what the domain requires from persistence.
 *
 * The domain defines this interface; infrastructure implements it.
 * Inverting the dependency means the domain never knows about JPA or PostgreSQL.
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);

    boolean existsByEmail(String email);
}
