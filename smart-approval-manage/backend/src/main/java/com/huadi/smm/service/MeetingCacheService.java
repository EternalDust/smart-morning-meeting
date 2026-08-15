package com.huadi.smm.service;

import com.alibaba.fastjson2.JSON;
import com.huadi.smm.entity.MeetingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MeetingCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "meeting:";

    public void cacheMeeting(MeetingInfo meeting) {
        if (meeting == null || meeting.getId() == null) return;
        String key = PREFIX + meeting.getId();
        redisTemplate.opsForValue().set(key, JSON.toJSONString(meeting), 10, TimeUnit.MINUTES);
    }

    public MeetingInfo getCachedMeeting(Long meetingId) {
        String key = PREFIX + meetingId;
        Object val = redisTemplate.opsForValue().get(key);
        if (val == null) return null;
        return JSON.parseObject(val.toString(), MeetingInfo.class);
    }

    public void deleteMeetingCache(Long meetingId) {
        redisTemplate.delete(PREFIX + meetingId);
    }
}