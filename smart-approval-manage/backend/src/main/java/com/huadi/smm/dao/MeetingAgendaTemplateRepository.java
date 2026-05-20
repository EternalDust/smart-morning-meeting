package com.huadi.smm.dao;

import com.huadi.smm.entity.MeetingAgendaTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingAgendaTemplateRepository extends JpaRepository<MeetingAgendaTemplate, Long> {
}