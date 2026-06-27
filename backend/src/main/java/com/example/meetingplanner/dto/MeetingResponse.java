package com.example.meetingplanner.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record MeetingResponse(
        Long id,
        String title,
        String agenda,
        LocalDateTime dateTime,
        String location,
        UserResponse host,
        Set<UserResponse> participants
) {
}
