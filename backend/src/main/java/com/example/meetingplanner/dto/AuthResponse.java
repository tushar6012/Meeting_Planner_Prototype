package com.example.meetingplanner.dto;

public record AuthResponse(String token, Long id, String fullName, String email) {
}
