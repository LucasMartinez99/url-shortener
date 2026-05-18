package com.urlshortener.adapter.in.web.controller;

import com.urlshortener.adapter.in.web.dto.request.LoginRequest;
import com.urlshortener.adapter.in.web.dto.request.RegisterRequest;
import com.urlshortener.adapter.in.web.dto.response.AuthResponse;
import com.urlshortener.domain.model.User;
import com.urlshortener.domain.port.in.AuthenticateUserUseCase;
import com.urlshortener.domain.port.in.AuthenticateUserUseCase.AuthenticateUserCommand;
import com.urlshortener.domain.port.in.RegisterUserUseCase;
import com.urlshortener.domain.port.in.RegisterUserUseCase.RegisterUserCommand;
import com.urlshortener.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final RegisterUserUseCase    registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final JwtService             jwtService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        User user  = registerUserUseCase.register(new RegisterUserCommand(request.email(), request.password()));
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail());
    }

    @PostMapping("/login")
    @Operation(summary = "Login and obtain a JWT token")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user  = authenticateUserUseCase.authenticate(new AuthenticateUserCommand(request.email(), request.password()));
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail());
    }
}
