package com.example.meetingplanner.controller;

import com.example.meetingplanner.dto.LoginRequest;
import com.example.meetingplanner.model.User;
import com.example.meetingplanner.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUserAndLogin_Success() throws Exception {
        // 1. Sign Up
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar", "avatar.png", "image/png", "fake-image-data".getBytes());

        mockMvc.perform(multipart("/api/auth/signup")
                        .file(avatarFile)
                        .param("fullName", "Tushar Patel")
                        .param("email", "tushar@example.com")
                        .param("password", "tusharpass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.fullName").value("Tushar Patel"))
                .andExpect(jsonPath("$.email").value("tushar@example.com"));

        // Verify user exists in database
        assertTrue(userRepository.findByEmail("tushar@example.com").isPresent());

        // 2. Login
        LoginRequest loginRequest = new LoginRequest("tushar@example.com", "tusharpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.email").value("tushar@example.com"));
    }

    @Test
    void testRegisterUser_DuplicateEmail_Fails() throws Exception {
        // Pre-create user
        User user = new User();
        user.setFullName("Existing User");
        user.setEmail("existing@example.com");
        user.setPassword(passwordEncoder.encode("pass"));
        userRepository.save(user);

        // Attempt duplicate signup
        mockMvc.perform(multipart("/api/auth/signup")
                        .param("fullName", "New User")
                        .param("email", "existing@example.com")
                        .param("password", "newpass"))
                .andExpect(status().isBadRequest()); // our error handler or Spring's default error page for Exceptions
    }

    @Test
    void testLogin_BadCredentials_Fails() throws Exception {
        LoginRequest badRequest = new LoginRequest("nonexistent@example.com", "wrongpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isUnauthorized());
    }
}
