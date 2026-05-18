package com.urlshortener.adapter.in.web.controller;

import com.urlshortener.domain.exception.UserNotFoundException;
import com.urlshortener.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the authenticated user's email to their UUID.
 * Extracted into a helper to keep controllers clean.
 */
@Component
@RequiredArgsConstructor
public class UserLookupHelper {

    private final UserRepositoryPort userRepository;

    public UUID resolveId(String email) {
        return userRepository.findByEmail(email)
                .map(com.urlshortener.domain.model.User::getId)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}
