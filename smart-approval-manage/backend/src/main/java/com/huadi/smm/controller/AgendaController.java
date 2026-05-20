package com.huadi.smm.controller;

import com.huadi.smm.config.Result;
import com.huadi.smm.dto.AiGenerateRequest;
import com.huadi.smm.dto.HandleApproveRequest;
import com.huadi.smm.entity.MeetingInfo;
import com.huadi.smm.service.AgendaService;
import com.huadi.smm.service.ApproveService;
import com.huadi.smm.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/agenda")
@Validated
public class AgendaController {

    @Autowired
    private AgendaService agendaService;

    @Autowired
    private ApproveService approveService;

    @Autowired
    private MeetingService meetingService;

    @GetMapping("/meetings")
    public Result<List<MeetingInfo>> listMeetings() {
        return Result.ok(meetingService.listAll());
    }

    @GetMapping("/meetings/{meetingId}")
    public Result<MeetingInfo> getMeeting(@PathVariable Long meetingId) {
        MeetingInfo meeting = meetingService.getById(meetingId);
        if (meeting == null) {
            return Result.fail("会议不存在", 404);
        }
        return Result.ok(meeting);
    }

    @PostMapping("/ai-generate")
    public Result<List<String>> aiAgendaGenerate(@Valid @RequestBody AiGenerateRequest data) {
        try {
            List<String> result = agendaService.createAiAgenda(data.getMeetingId(), data.getDeptName(), data.getMeetingType());
            return Result.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.fail(e.getMessage(), 400);
        } catch (Exception e) {
            return Result.fail("服务器内部错误", 500);
        }
    }

    @PostMapping("/{meetingId}/submit")
    public Result<Boolean> submitApprove(@PathVariable Long meetingId) {
        boolean result = approveService.submitApprove(meetingId);
        if (!result) {
            return Result.fail("提交审批失败，会议不存在或状态不允许", 400);
        }
        return Result.ok(true);
    }

    @PostMapping("/{meetingId}/handle")
    public Result<Boolean> handleApprove(@PathVariable Long meetingId,
                                         @Valid @RequestBody HandleApproveRequest data) {
        boolean result = approveService.handleApprove(meetingId, data.getApproverId(), data.getAction(), data.getOpinion());
        if (!result) {
            return Result.fail("审批处理失败，会议不存在或状态不允许", 400);
        }
        return Result.ok(true);
    }
}
