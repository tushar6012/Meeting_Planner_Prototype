package com.example.meetingplanner.controller;

import com.example.meetingplanner.dto.UserResponse;
import com.example.meetingplanner.model.User;
import com.example.meetingplanner.repository.UserRepository;
import com.example.meetingplanner.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse response = userService.getUserProfile(principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsersExceptSelf(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<UserResponse> users = userService.getAllUsersExcept(principal.getName());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> getUserAvatar(@PathVariable Long id) {
        try {
            byte[] avatarBytes = userService.getAvatarBytes(id);
            String contentType = userService.getAvatarContentType(id);

            if (avatarBytes != null && avatarBytes.length > 0) {
                MediaType mediaType = MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg");
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=604800") // cache for 1 week
                        .body(avatarBytes);
            }
        } catch (Exception ignored) {
        }

        // Fallback: Dynamically generate a premium SVG avatar with initials
        String fullName = "User";
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                fullName = user.getFullName();
            }
        } catch (Exception ignored) {
        }

        String initials = getInitials(fullName);
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='120' height='120' viewBox='0 0 120 120'>" +
                "<rect width='120' height='120' rx='60' fill='#6366f1'/>" +
                "<text x='50%' y='54%' font-family='System-UI, -apple-system, sans-serif' font-weight='bold' font-size='48' fill='#ffffff' dominant-baseline='middle' text-anchor='middle'>" +
                initials +
                "</text></svg>";

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(svg.getBytes());
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "U";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 0) return "U";
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
