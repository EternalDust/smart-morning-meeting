package com.huadi.smm.dao;

import com.huadi.smm.entity.MeetingAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingAttendeeRepository extends JpaRepository<MeetingAttendee, Long> {
}