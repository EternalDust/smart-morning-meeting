package com.smartmeeting.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartmeeting.common.Result;
import com.smartmeeting.dao.MemberMapper;
import com.smartmeeting.entity.Member;
import com.smartmeeting.entity.SignIn;
import com.smartmeeting.service.SignService;
import com.smartmeeting.ws.RealtimeServer;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meeting/sign")
public class SignController {

    @Resource
    private SignService signService;

    @Resource
    private MemberMapper memberMapper;

    private static final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/in")
    public Result<?> signIn(@RequestBody Map<String, Object> body) {
        Long meetingId = Long.valueOf(body.get("meetingId").toString());
        String userId = (String) body.get("userId");
        Integer signType = (Integer) body.getOrDefault("signType", 2);

        SignService.Result sr = signService.signIn(meetingId, userId, signType);
        if (!sr.success) {
            return Result.fail(400, sr.error);
        }

        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", "sign");
            msg.put("userId", userId);
            msg.put("signTime", sr.data.getSignTime());
            RealtimeServer.broadcast(meetingId, mapper.writeValueAsString(msg));
        } catch (Exception ignored) {}
        return Result.ok(sr.data);
    }

    @GetMapping("/list/{meetingId}")
    public Result<Map<String, Object>> list(@PathVariable Long meetingId) {
        List<SignIn> records = signService.listByMeetingId(meetingId);
        int shouldAttend = signService.countAttendees(meetingId);
        int normal = signService.countByStatus(meetingId, 0);
        int late = signService.countByStatus(meetingId, 1);
        int signed = normal + late;
        int absent = shouldAttend - signed;

        List<String> userIds = records.stream().map(SignIn::getUserId).distinct().collect(Collectors.toList());
        Map<String, String> nameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<Member> qw = new LambdaQueryWrapper<>();
            qw.in(Member::getUserId, userIds);
            List<Member> members = memberMapper.selectList(qw);
            for (Member m : members) {
                nameMap.put(m.getUserId(), m.getName());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("nameMap", nameMap);
        result.put("shouldAttend", shouldAttend);
        result.put("normal", normal);
        result.put("late", late);
        result.put("absent", absent);
        result.put("signed", signed);
        return Result.ok(result);
    }
}
