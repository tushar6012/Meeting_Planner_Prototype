package com.example.meetingplanner.service;

import com.example.meetingplanner.dto.MeetingRequest;
import com.example.meetingplanner.dto.MeetingResponse;
import com.example.meetingplanner.dto.UserResponse;
import com.example.meetingplanner.model.Meeting;
import com.example.meetingplanner.model.User;
import com.example.meetingplanner.repository.MeetingRepository;
import com.example.meetingplanner.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    public MeetingService(MeetingRepository meetingRepository, UserRepository userRepository) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MeetingResponse createMeeting(MeetingRequest request, String hostEmail) {
        User host = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new IllegalArgumentException("Host user not found with email: " + hostEmail));

        Meeting meeting = new Meeting();
        meeting.setTitle(request.title());
        meeting.setAgenda(request.agenda());
        
        if (request.dateTime() == null) {
            throw new IllegalArgumentException("Meeting date and time is required.");
        }
        if (request.dateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Meeting date and time must be in the future.");
        }
        
        meeting.setDateTime(request.dateTime());
        meeting.setLocation(request.location());
        meeting.setHost(host);

        if (request.participantIds() == null || request.participantIds().isEmpty()) {
            throw new IllegalArgumentException("At least one participant is required.");
        }

        Set<User> participants = new HashSet<>(userRepository.findAllById(request.participantIds()));
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("Selected participants are invalid.");
        }
        meeting.setParticipants(participants);

        Meeting savedMeeting = meetingRepository.save(meeting);
        return mapToMeetingResponse(savedMeeting);
    }

    @Transactional(readOnly = true)
    public List<MeetingResponse> getMeetingsForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        List<Meeting> meetings = meetingRepository.findAllMeetingsForUser(user.getId());
        return meetings.stream()
                .map(this::mapToMeetingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MeetingResponse getMeetingDetails(Long meetingId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found with ID: " + meetingId));

        // Security check: only host or participants can view meeting details
        boolean isParticipant = meeting.getParticipants().stream().anyMatch(p -> p.getId().equals(user.getId()));
        boolean isHost = meeting.getHost().getId().equals(user.getId());

        if (!isHost && !isParticipant) {
            throw new SecurityException("You do not have permission to view this meeting.");
        }

        return mapToMeetingResponse(meeting);
    }

    private MeetingResponse mapToMeetingResponse(Meeting meeting) {
        UserResponse hostDto = new UserResponse(
                meeting.getHost().getId(),
                meeting.getHost().getFullName(),
                meeting.getHost().getEmail()
        );

        Set<UserResponse> participantsDto = meeting.getParticipants().stream()
                .map(p -> new UserResponse(p.getId(), p.getFullName(), p.getEmail()))
                .collect(Collectors.toSet());

        return new MeetingResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getAgenda(),
                meeting.getDateTime(),
                meeting.getLocation(),
                hostDto,
                participantsDto
        );
    }
}
