package com.example.meetingplanner.service;

import com.example.meetingplanner.dto.UserResponse;
import com.example.meetingplanner.model.User;
import com.example.meetingplanner.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(String fullName, String email, String password, MultipartFile avatarFile) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already taken");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                user.setAvatar(avatarFile.getBytes());
                user.setAvatarContentType(avatarFile.getContentType());
            } catch (IOException e) {
                throw new RuntimeException("Could not read avatar image file", e);
            }
        }

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public byte[] getAvatarBytes(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return user.getAvatar();
    }

    @Transactional(readOnly = true)
    public String getAvatarContentType(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return user.getAvatarContentType();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsersExcept(String email) {
        return userRepository.findAll().stream()
                .filter(user -> !user.getEmail().equalsIgnoreCase(email))
                .map(user -> new UserResponse(user.getId(), user.getFullName(), user.getEmail()))
                .collect(Collectors.toList());
    }
}
