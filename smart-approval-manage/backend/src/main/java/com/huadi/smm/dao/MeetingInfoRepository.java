package com.huadi.smm.dao;

import com.huadi.smm.entity.MeetingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingInfoRepository extends JpaRepository<MeetingInfo, Long> {
}