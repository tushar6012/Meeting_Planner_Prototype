package com.example.meetingplanner.controller;

import com.example.meetingplanner.dto.MeetingRequest;
import com.example.meetingplanner.model.User;
import com.example.meetingplanner.repository.MeetingRepository;
import com.example.meetingplanner.repository.UserRepository;
import com.example.meetingplanner.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MeetingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User hostUser;
    private User participantUser;
    private User externalUser;
    
    private String hostToken;
    private String participantToken;
    private String externalToken;

    @BeforeEach
    void setUp() {
        meetingRepository.deleteAll();
        userRepository.deleteAll();

        // Create Host
        hostUser = new User();
        hostUser.setFullName("Host User");
        hostUser.setEmail("host@example.com");
        hostUser.setPassword(passwordEncoder.encode("pass"));
        hostUser = userRepository.save(hostUser);
        hostToken = tokenProvider.generateToken(hostUser.getEmail());

        // Create Participant
        participantUser = new User();
        participantUser.setFullName("Participant User");
        participantUser.setEmail("part@example.com");
        participantUser.setPassword(passwordEncoder.encode("pass"));
        participantUser = userRepository.save(participantUser);
        participantToken = tokenProvider.generateToken(participantUser.getEmail());

        // Create External User
        externalUser = new User();
        externalUser.setFullName("External User");
        externalUser.setEmail("external@example.com");
        externalUser.setPassword(passwordEncoder.encode("pass"));
        externalUser = userRepository.save(externalUser);
        externalToken = tokenProvider.generateToken(externalUser.getEmail());
    }

    @Test
    void testCreateAndGetMeeting_SuccessAndForbiddenChecks() throws Exception {
        LocalDateTime meetingTime = LocalDateTime.of(2026, 8, 15, 14, 30);
        MeetingRequest request = new MeetingRequest(
                "Sprint Planning",
                "Plan the next development cycle",
                meetingTime,
                "Conference Room A",
                Set.of(participantUser.getId())
        );

        // 1. Create meeting as host
        MvcResult createResult = mockMvc.perform(post("/api/meetings")
                        .header("Authorization", "Bearer " + hostToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title").value("Sprint Planning"))
                .andExpect(jsonPath("$.host.email").value("host@example.com"))
                .andExpect(jsonPath("$.participants", hasSize(1)))
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        Long meetingId = objectMapper.readTree(responseContent).get("id").asLong();

        // 2. Host gets meeting details -> OK
        mockMvc.perform(get("/api/meetings/" + meetingId)
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sprint Planning"));

        // 3. Participant gets meeting details -> OK
        mockMvc.perform(get("/api/meetings/" + meetingId)
                        .header("Authorization", "Bearer " + participantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sprint Planning"));

        // 4. External user gets meeting details -> 403 Forbidden
        mockMvc.perform(get("/api/meetings/" + meetingId)
                        .header("Authorization", "Bearer " + externalToken))
                .andExpect(status().isForbidden());

        // 5. Get all meetings for participant -> Should contain the meeting
        mockMvc.perform(get("/api/meetings")
                        .header("Authorization", "Bearer " + participantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(meetingId));

        // 6. Get all meetings for external user -> Should be empty
        mockMvc.perform(get("/api/meetings")
                        .header("Authorization", "Bearer " + externalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
