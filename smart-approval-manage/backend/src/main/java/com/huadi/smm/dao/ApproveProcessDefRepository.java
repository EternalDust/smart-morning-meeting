package com.huadi.smm.dao;

import com.huadi.smm.entity.ApproveProcessDef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApproveProcessDefRepository extends JpaRepository<ApproveProcessDef, Long> {
}