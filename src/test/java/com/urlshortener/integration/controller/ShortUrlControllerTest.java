package com.urlshortener.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.adapter.in.web.dto.request.CreateShortUrlRequest;
import com.urlshortener.adapter.in.web.dto.request.RegisterRequest;
import com.urlshortener.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ShortUrlControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void registerAndLogin() throws Exception {
        RegisterRequest register = new RegisterRequest("urltest@example.com", "Password@123");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andReturn();

        // If already registered (test isolation), login instead
        String body = result.getResponse().getContentAsString();
        if (result.getResponse().getStatus() == 409) {
            result = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new com.urlshortener.adapter.in.web.dto.request.LoginRequest(
                                            "urltest@example.com", "Password@123"))))
                    .andReturn();
            body = result.getResponse().getContentAsString();
        }

        jwtToken = objectMapper.readTree(body).get("token").asText();
    }

    @Test
    @DisplayName("POST /api/v1/urls — should create a short URL")
    void create_validRequest_shouldReturnShortUrl() throws Exception {
        CreateShortUrlRequest request = new CreateShortUrlRequest(
                "https://example.com/very/long/path", null, null);

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl").isNotEmpty())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/very/long/path"))
                .andExpect(jsonPath("$.clickCount").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/urls — should use custom alias when provided")
    void create_withCustomAlias_shouldUseAlias() throws Exception {
        CreateShortUrlRequest request = new CreateShortUrlRequest(
                "https://example.com", "my-custom-alias", null);

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customAlias").value("my-custom-alias"))
                .andExpect(jsonPath("$.shortUrl").value(
                        org.hamcrest.Matchers.containsString("my-custom-alias")));
    }

    @Test
    @DisplayName("GET /api/v1/urls — should list user URLs with pagination")
    void list_shouldReturnPaginatedUrls() throws Exception {
        mockMvc.perform(get("/api/v1/urls")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/urls — should return 401 without JWT")
    void list_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/urls"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/v1/urls/{id} — should delete own URL")
    void delete_ownUrl_shouldReturn204() throws Exception {
        CreateShortUrlRequest request = new CreateShortUrlRequest(
                "https://example.com/to-delete", null, null);

        MvcResult created = mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(delete("/api/v1/urls/" + id)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }
}
