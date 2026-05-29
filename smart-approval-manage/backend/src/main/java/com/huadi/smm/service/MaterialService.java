package com.huadi.smm.service;

import com.huadi.smm.dao.MeetingMaterialMapper;
import com.huadi.smm.entity.MeetingMaterial;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MaterialService {

    @Autowired
    private MeetingMaterialMapper meetingMaterialMapper;

    public List<MeetingMaterial> listByMeetingId(Long meetingId) {
        QueryWrapper<MeetingMaterial> wrapper = new QueryWrapper<>();
        wrapper.eq("meeting_id", meetingId);
        return meetingMaterialMapper.selectList(wrapper);
    }

    public MeetingMaterial saveMaterial(MeetingMaterial material) {
        material.setCreateTime(new Date());
        meetingMaterialMapper.insert(material);
        return material;
    }

    public void deleteMaterial(Long id) {
        meetingMaterialMapper.deleteById(id);
    }
}