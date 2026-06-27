package com.example.meetingplanner.service;

import com.example.meetingplanner.dto.MeetingRequest;
import com.example.meetingplanner.dto.MeetingResponse;
import com.example.meetingplanner.model.Meeting;
import com.example.meetingplanner.model.User;
import com.example.meetingplanner.repository.MeetingRepository;
import com.example.meetingplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private UserRepository userRepository;

    private MeetingService meetingService;

    private User host;
    private User participant;

    @BeforeEach
    void setUp() {
        meetingService = new MeetingService(meetingRepository, userRepository);
        host = new User(1L, "Host User", "host@example.com", "pass");
        participant = new User(2L, "Participant User", "part@example.com", "pass");
    }

    @Test
    void createMeeting_Success() {
        LocalDateTime meetingTime = LocalDateTime.of(2026, 12, 25, 10, 0);
        MeetingRequest request = new MeetingRequest(
                "Project Sync",
                "Weekly project alignment meeting",
                meetingTime,
                "Room 404",
                Set.of(2L)
        );

        when(userRepository.findByEmail("host@example.com")).thenReturn(Optional.of(host));
        when(userRepository.findAllById(Set.of(2L))).thenReturn(List.of(participant));
        
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> {
            Meeting m = invocation.getArgument(0);
            m.setId(10L);
            return m;
        });

        MeetingResponse response = meetingService.createMeeting(request, "host@example.com");

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("Project Sync", response.title());
        assertEquals("Weekly project alignment meeting", response.agenda());
        assertEquals(meetingTime, response.dateTime());
        assertEquals("Room 404", response.location());
        assertEquals(host.getId(), response.host().id());
        assertEquals(1, response.participants().size());
        assertTrue(response.participants().stream().anyMatch(p -> p.id().equals(2L)));
    }

    @Test
    void getMeetingsForUser_Success() {
        LocalDateTime meetingTime = LocalDateTime.now();
        Meeting meeting = new Meeting(10L, "Sprint Review", "Demo", meetingTime, "Teams", host);
        meeting.setParticipants(Set.of(participant));

        when(userRepository.findByEmail("part@example.com")).thenReturn(Optional.of(participant));
        when(meetingRepository.findAllMeetingsForUser(2L)).thenReturn(List.of(meeting));

        List<MeetingResponse> list = meetingService.getMeetingsForUser("part@example.com");

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Sprint Review", list.get(0).title());
    }

    @Test
    void getMeetingDetails_AuthorizedForHost() {
        Meeting meeting = new Meeting(10L, "Sprint Review", "Demo", LocalDateTime.now(), "Teams", host);
        meeting.setParticipants(Set.of(participant));

        when(userRepository.findByEmail("host@example.com")).thenReturn(Optional.of(host));
        when(meetingRepository.findById(10L)).thenReturn(Optional.of(meeting));

        MeetingResponse response = meetingService.getMeetingDetails(10L, "host@example.com");

        assertNotNull(response);
        assertEquals(10L, response.id());
    }

    @Test
    void getMeetingDetails_AuthorizedForParticipant() {
        Meeting meeting = new Meeting(10L, "Sprint Review", "Demo", LocalDateTime.now(), "Teams", host);
        meeting.setParticipants(Set.of(participant));

        when(userRepository.findByEmail("part@example.com")).thenReturn(Optional.of(participant));
        when(meetingRepository.findById(10L)).thenReturn(Optional.of(meeting));

        MeetingResponse response = meetingService.getMeetingDetails(10L, "part@example.com");

        assertNotNull(response);
        assertEquals(10L, response.id());
    }

    @Test
    void getMeetingDetails_ForbiddenForExternalUser() {
        User externalUser = new User(3L, "External", "ext@example.com", "pass");
        Meeting meeting = new Meeting(10L, "Sprint Review", "Demo", LocalDateTime.now(), "Teams", host);
        meeting.setParticipants(Set.of(participant));

        when(userRepository.findByEmail("ext@example.com")).thenReturn(Optional.of(externalUser));
        when(meetingRepository.findById(10L)).thenReturn(Optional.of(meeting));

        assertThrows(SecurityException.class, () -> {
            meetingService.getMeetingDetails(10L, "ext@example.com");
        });
    }
}
