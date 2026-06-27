package com.example.meetingplanner.controller;

import com.example.meetingplanner.dto.MeetingRequest;
import com.example.meetingplanner.dto.MeetingResponse;
import com.example.meetingplanner.service.MeetingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(@RequestBody MeetingRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        MeetingResponse response = meetingService.createMeeting(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MeetingResponse>> getMyMeetings(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<MeetingResponse> meetings = meetingService.getMeetingsForUser(principal.getName());
        return ResponseEntity.ok(meetings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponse> getMeetingDetails(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            MeetingResponse meeting = meetingService.getMeetingDetails(id, principal.getName());
            return ResponseEntity.ok(meeting);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
