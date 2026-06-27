package com.example.meetingplanner.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 2000)
    private String agenda;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false, length = 255)
    private String location;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "meeting_participants",
            joinColumns = @JoinColumn(name = "meeting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    public Meeting() {
    }

    public Meeting(Long id, String title, String agenda, LocalDateTime dateTime, String location, User host) {
        this.id = id;
        this.title = title;
        this.agenda = agenda;
        this.dateTime = dateTime;
        this.location = location;
        this.host = host;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAgenda() {
        return agenda;
    }

    public void setAgenda(String agenda) {
        this.agenda = agenda;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }
}
