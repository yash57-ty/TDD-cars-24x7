package org.example.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.dto.LoginRequest;
import org.example.backend.dto.RegisterRequest;
import org.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 201 and token on success")
    void register_ShouldReturn201_WithToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 400 when fields are missing")
    void register_ShouldReturn400_WhenFieldsMissing() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        // name and password missing

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - should return 409 when email already exists")
    void register_ShouldReturn409_WhenEmailDuplicate() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("dup@example.com");
        request.setPassword("password123");

        // Register once
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Register again with same email
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 200 and token on valid credentials")
    void login_ShouldReturn200_WithToken() throws Exception {
        // First register a user
        RegisterRequest reg = new RegisterRequest();
        reg.setName("Login User");
        reg.setEmail("login@example.com");
        reg.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Then login
        LoginRequest login = new LoginRequest();
        login.setEmail("login@example.com");
        login.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/login - should return 401 on bad credentials")
    void login_ShouldReturn401_OnWrongPassword() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("nobody@example.com");
        login.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
