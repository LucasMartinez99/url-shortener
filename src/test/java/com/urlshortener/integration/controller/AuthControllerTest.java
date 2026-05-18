package com.urlshortener.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.adapter.in.web.dto.request.LoginRequest;
import com.urlshortener.adapter.in.web.dto.request.RegisterRequest;
import com.urlshortener.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test — full Spring Boot context + real PostgreSQL via Testcontainers.
 * Tests the entire stack from HTTP request to database.
 */
class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;

    @Test
    @DisplayName("POST /api/v1/auth/register — should create user and return token")
    void register_validRequest_shouldReturnToken() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser@example.com", "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register — should return 409 when email is already taken")
    void register_duplicateEmail_shouldReturn409() throws Exception {
        RegisterRequest request = new RegisterRequest("duplicate@example.com", "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register — should return 400 for invalid email")
    void register_invalidEmail_shouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest("not-an-email", "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — should return token for valid credentials")
    void login_validCredentials_shouldReturnToken() throws Exception {
        RegisterRequest register = new RegisterRequest("login@example.com", "password123");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest("login@example.com", "password123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login — should return 401 for wrong password")
    void login_wrongPassword_shouldReturn401() throws Exception {
        RegisterRequest register = new RegisterRequest("wrongpass@example.com", "password123");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest("wrongpass@example.com", "wrongPassword");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
