package com.smartmeeting.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartmeeting.dao.InteractionMapper;
import com.smartmeeting.dao.MeetingInfoMapper;
import com.smartmeeting.entity.Interaction;
import com.smartmeeting.entity.MeetingInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InteractionService {

    @Resource
    private InteractionMapper interactionMapper;

    @Resource
    private MeetingInfoMapper meetingInfoMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Interaction sendMessage(Interaction msg) {
        MeetingInfo meeting = meetingInfoMapper.selectById(msg.getMeetingId());
        if (meeting == null) {
            throw new RuntimeException("晨会不存在");
        }
        msg.setCreateTime(LocalDateTime.now().format(FMT));
        interactionMapper.insert(msg);
        return msg;
    }

    public Interaction replyMessage(Long id, String reply) {
        Interaction msg = interactionMapper.selectById(id);
        if (msg == null) {
            throw new RuntimeException("互动消息不存在");
        }
        msg.setReply(reply);
        interactionMapper.updateById(msg);
        return msg;
    }

    public List<Interaction> listByMeetingId(Long meetingId, Integer interactType) {
        LambdaQueryWrapper<Interaction> qw = new LambdaQueryWrapper<>();
        qw.eq(Interaction::getMeetingId, meetingId)
          .orderByDesc(Interaction::getCreateTime);
        if (interactType != null && interactType > 0) {
            qw.eq(Interaction::getInteractType, interactType);
        }
        return interactionMapper.selectList(qw);
    }

    public long[] countStats(Long meetingId) {
        long questions = countByType(meetingId, 1);
        long feedback = countByType(meetingId, 2);
        long votes = countByType(meetingId, 3);
        long replied = countReplied(meetingId);
        return new long[]{questions, feedback, votes, replied};
    }

    private long countByType(Long meetingId, int type) {
        LambdaQueryWrapper<Interaction> qw = new LambdaQueryWrapper<>();
        qw.eq(Interaction::getMeetingId, meetingId).eq(Interaction::getInteractType, type);
        return interactionMapper.selectCount(qw);
    }

    private long countReplied(Long meetingId) {
        LambdaQueryWrapper<Interaction> qw = new LambdaQueryWrapper<>();
        qw.eq(Interaction::getMeetingId, meetingId).isNotNull(Interaction::getReply)
          .ne(Interaction::getReply, "");
        return interactionMapper.selectCount(qw);
    }
}
