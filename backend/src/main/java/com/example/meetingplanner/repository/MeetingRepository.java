package com.example.meetingplanner.repository;

import com.example.meetingplanner.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @Query("SELECT DISTINCT m FROM Meeting m LEFT JOIN m.participants p WHERE m.host.id = :userId OR p.id = :userId ORDER BY m.dateTime ASC")
    List<Meeting> findAllMeetingsForUser(@Param("userId") Long userId);
}
