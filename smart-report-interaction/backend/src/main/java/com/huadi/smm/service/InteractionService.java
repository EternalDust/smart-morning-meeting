package com.huadi.smm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.ai.AiClient;
import com.huadi.smm.ai.dto.SpeechLine;
import com.huadi.smm.dao.InteractionMapper;
import com.huadi.smm.dao.MeetingInfoMapper;
import com.huadi.smm.dao.SpeechRecordMapper;
import com.huadi.smm.entity.Interaction;
import com.huadi.smm.entity.MeetingInfo;
import com.huadi.smm.entity.SpeechRecord;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class InteractionService {

    @Resource
    private InteractionMapper interactionMapper;

    @Resource
    private MeetingInfoMapper meetingInfoMapper;

    @Resource
    private SpeechRecordMapper speechRecordMapper;

    @Resource
    private AiClient aiClient;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Interaction sendMessage(Interaction msg) {
        MeetingInfo meeting = meetingInfoMapper.selectById(msg.getMeetingId());
        if (meeting == null) {
            throw new RuntimeException("晨会不存在");
        }
        if (msg.getInteractType() != null && msg.getInteractType() == 4) {
            String pollId = extractPollId(msg.getContent());
            if (pollId != null) {
                LambdaQueryWrapper<Interaction> vq = new LambdaQueryWrapper<>();
                vq.eq(Interaction::getMeetingId, msg.getMeetingId())
                  .eq(Interaction::getUserId, msg.getUserId())
                  .eq(Interaction::getInteractType, 4)
                  .likeRight(Interaction::getContent, "VOTE:" + pollId + ":");
                if (interactionMapper.selectCount(vq) > 0) {
                    throw new RuntimeException("您已投过票，请勿重复投票");
                }
            }
        }
        msg.setCreateTime(LocalDateTime.now().format(FMT));
        interactionMapper.insert(msg);
        return msg;
    }

    private String extractPollId(String content) {
        if (content == null || !content.startsWith("VOTE:")) {
            return null;
        }
        String[] parts = content.split(":");
        return parts.length >= 2 && !parts[1].isEmpty() ? parts[1] : null;
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

    public Interaction aiReply(Long id) {
        Interaction msg = interactionMapper.selectById(id);
        if (msg == null) {
            throw new RuntimeException("互动消息不存在");
        }
        List<SpeechLine> context = new ArrayList<>();
        LambdaQueryWrapper<SpeechRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(SpeechRecord::getMeetingId, msg.getMeetingId());
        for (SpeechRecord r : speechRecordMapper.selectList(qw)) {
            context.add(new SpeechLine(r.getSpeakerId(), r.getContent()));
        }
        String answer = aiClient.answerQuestion(msg.getContent(), context);
        msg.setReply("【AI初步答复】" + answer);
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
