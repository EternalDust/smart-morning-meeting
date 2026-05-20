package com.huadi.smm.dao;

import com.huadi.smm.entity.MeetingMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingMaterialRepository extends JpaRepository<MeetingMaterial, Long> {
}
