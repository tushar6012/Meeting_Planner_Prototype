package com.example.meetingplanner.service;

import com.example.meetingplanner.dto.UserResponse;
import com.example.meetingplanner.model.User;
import com.example.meetingplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registerUser_Success_WithoutAvatar() {
        String fullName = "Alice Smith";
        String email = "alice@example.com";
        String plainPassword = "password123";
        String encodedPassword = "encodedPassword123";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(plainPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User registered = userService.registerUser(fullName, email, plainPassword, null);

        assertNotNull(registered);
        assertEquals(1L, registered.getId());
        assertEquals(fullName, registered.getFullName());
        assertEquals(email, registered.getEmail());
        assertEquals(encodedPassword, registered.getPassword());
        assertNull(registered.getAvatar());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_Success_WithAvatar() {
        String fullName = "Alice Smith";
        String email = "alice@example.com";
        String plainPassword = "password123";
        byte[] avatarBytes = "fake-image-bytes".getBytes();
        MockMultipartFile avatarFile = new MockMultipartFile("avatar", "test.jpg", "image/jpeg", avatarBytes);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(plainPassword)).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User registered = userService.registerUser(fullName, email, plainPassword, avatarFile);

        assertNotNull(registered);
        assertEquals(1L, registered.getId());
        assertArrayEquals(avatarBytes, registered.getAvatar());
        assertEquals("image/jpeg", registered.getAvatarContentType());
    }

    @Test
    void registerUser_ThrowsException_WhenAvatarExceeds1MB() {
        String fullName = "Alice Smith";
        String email = "alice@example.com";
        String plainPassword = "password123";
        byte[] largeBytes = new byte[1024 * 1024 + 1];
        MockMultipartFile avatarFile = new MockMultipartFile("avatar", "large.jpg", "image/jpeg", largeBytes);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(fullName, email, plainPassword, avatarFile);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ThrowsException_WhenEmailExists() {
        String email = "duplicate@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("Name", email, "password", null);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ThrowsException_WhenEmailInvalid() {
        String invalidEmail = "invalid-email-format";
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser("Name", invalidEmail, "password", null);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserProfile_Success() {
        String email = "bob@example.com";
        User user = new User(2L, "Bob", email, "encoded");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserResponse profile = userService.getUserProfile(email);

        assertNotNull(profile);
        assertEquals(2L, profile.id());
        assertEquals("Bob", profile.fullName());
        assertEquals(email, profile.email());
    }
}
