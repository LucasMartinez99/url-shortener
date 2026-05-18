package com.urlshortener.unit.service;

import com.urlshortener.application.service.UserService;
import com.urlshortener.domain.exception.InvalidCredentialsException;
import com.urlshortener.domain.exception.UserAlreadyExistsException;
import com.urlshortener.domain.model.User;
import com.urlshortener.domain.port.in.RegisterUserUseCase.RegisterUserCommand;
import com.urlshortener.domain.port.in.AuthenticateUserUseCase.AuthenticateUserCommand;
import com.urlshortener.domain.port.out.PasswordEncoderPort;
import com.urlshortener.domain.port.out.UserRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepositoryPort  userRepository;
    @Mock private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("should register a new user with an encoded password")
    void register_newUser_shouldSaveWithHashedPassword() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register(new RegisterUserCommand("test@example.com", "password123"));

        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("$2a$hashed");
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("should throw UserAlreadyExistsException when email is taken")
    void register_existingEmail_shouldThrow() {
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.register(new RegisterUserCommand("existing@example.com", "pass")))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should authenticate with correct credentials")
    void authenticate_validCredentials_shouldReturnUser() {
        User stored = buildUser("$2a$hashed");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("rawPass", "$2a$hashed")).thenReturn(true);

        User result = userService.authenticate(new AuthenticateUserCommand("test@example.com", "rawPass"));

        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("should throw InvalidCredentialsException for wrong password")
    void authenticate_wrongPassword_shouldThrow() {
        User stored = buildUser("$2a$hashed");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches("wrongPass", "$2a$hashed")).thenReturn(false);

        assertThatThrownBy(() ->
                userService.authenticate(new AuthenticateUserCommand("test@example.com", "wrongPass")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private User buildUser(String hashedPassword) {
        return User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password(hashedPassword)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
