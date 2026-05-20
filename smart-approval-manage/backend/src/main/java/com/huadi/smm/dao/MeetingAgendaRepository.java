package com.huadi.smm.dao;

import com.huadi.smm.entity.MeetingAgenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingAgendaRepository extends JpaRepository<MeetingAgenda, Long> {
}