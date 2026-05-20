package com.huadi.smm.dao;

import com.huadi.smm.entity.ApproveRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApproveRecordRepository extends JpaRepository<ApproveRecord, Long> {
}