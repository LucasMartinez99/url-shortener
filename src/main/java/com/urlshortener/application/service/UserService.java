package com.urlshortener.application.service;

import com.urlshortener.domain.exception.InvalidCredentialsException;
import com.urlshortener.domain.exception.UserAlreadyExistsException;
import com.urlshortener.domain.exception.UserNotFoundException;
import com.urlshortener.domain.model.User;
import com.urlshortener.domain.port.in.AuthenticateUserUseCase;
import com.urlshortener.domain.port.in.RegisterUserUseCase;
import com.urlshortener.domain.port.out.PasswordEncoderPort;
import com.urlshortener.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * APPLICATION SERVICE — implements use cases by orchestrating domain objects and output ports.
 *
 * It has NO knowledge of HTTP, JSON, or database technology.
 * Its only dependencies are domain interfaces (ports).
 */
@Service
@RequiredArgsConstructor
public class UserService implements RegisterUserUseCase, AuthenticateUserUseCase {

    private final UserRepositoryPort  userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    @Transactional
    public User register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(command.email());
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(command.email())
                .password(passwordEncoder.encode(command.password()))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User authenticate(AuthenticateUserCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}
