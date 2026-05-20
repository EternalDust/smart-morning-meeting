package com.smartmeeting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmeeting.common.Result;
import com.smartmeeting.dao.MemberMapper;
import com.smartmeeting.entity.Interaction;
import com.smartmeeting.entity.Member;
import com.smartmeeting.service.InteractionService;
import com.smartmeeting.ws.RealtimeServer;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meeting/interaction")
public class InteractionController {

    @Resource
    private InteractionService interactionService;

    @Resource
    private MemberMapper memberMapper;

    private static final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/message")
    public Result<?> sendMessage(@RequestBody Interaction msg) {
        try {
            Interaction result = interactionService.sendMessage(msg);
            try {
                Map<String, Object> wsMsg = new HashMap<>();
                wsMsg.put("type", "interaction");
                wsMsg.put("interactType", msg.getInteractType());
                wsMsg.put("userId", msg.getUserId());
                wsMsg.put("content", msg.getContent());
                RealtimeServer.broadcast(msg.getMeetingId(), mapper.writeValueAsString(wsMsg));
            } catch (Exception ignored) {}
            return Result.ok(result);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/reply/{id}")
    public Result<?> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            return Result.ok(interactionService.replyMessage(id, body.get("reply")));
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @GetMapping("/list/{meetingId}")
    public Result<Map<String, Object>> list(@PathVariable Long meetingId,
                                            @RequestParam(required = false) Integer type) {
        List<Interaction> messages = interactionService.listByMeetingId(meetingId, type);
        List<String> userIds = messages.stream().map(Interaction::getUserId).distinct().collect(Collectors.toList());
        Map<String, String> nameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<Member> qw = new LambdaQueryWrapper<>();
            qw.in(Member::getUserId, userIds);
            for (Member m : memberMapper.selectList(qw)) {
                nameMap.put(m.getUserId(), m.getName());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        result.put("nameMap", nameMap);
        return Result.ok(result);
    }

    @GetMapping("/stats/{meetingId}")
    public Result<Map<String, Long>> stats(@PathVariable Long meetingId) {
        long[] s = interactionService.countStats(meetingId);
        Map<String, Long> result = new HashMap<>();
        result.put("questions", s[0]);
        result.put("feedback", s[1]);
        result.put("votes", s[2]);
        result.put("replied", s[3]);
        return Result.ok(result);
    }
}
