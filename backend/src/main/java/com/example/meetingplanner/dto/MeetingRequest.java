package com.example.meetingplanner.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record MeetingRequest(
        String title,
        String agenda,
        LocalDateTime dateTime,
        String location,
        Set<Long> participantIds
) {
}
