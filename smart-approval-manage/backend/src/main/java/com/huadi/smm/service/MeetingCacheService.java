package com.huadi.smm.service;

import com.alibaba.fastjson2.JSON;
import com.huadi.smm.entity.MeetingInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MeetingCacheService {

    private static final Logger log = LoggerFactory.getLogger(MeetingCacheService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "meeting:";

    public void cacheMeeting(MeetingInfo meeting) {
        if (meeting == null || meeting.getId() == null) return;
        try {
            String key = PREFIX + meeting.getId();
            redisTemplate.opsForValue().set(key, JSON.toJSONString(meeting), 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存会议失败(Redis 未启用，DB 为准): id={}", meeting.getId());
        }
    }

    public MeetingInfo getCachedMeeting(Long meetingId) {
        try {
            String key = PREFIX + meetingId;
            Object val = redisTemplate.opsForValue().get(key);
            if (val == null) return null;
            return JSON.parseObject(val.toString(), MeetingInfo.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteMeetingCache(Long meetingId) {
        try {
            redisTemplate.delete(PREFIX + meetingId);
        } catch (Exception e) {
            log.warn("删除会议缓存失败(Redis 未启用): id={}", meetingId);
        }
    }
}
