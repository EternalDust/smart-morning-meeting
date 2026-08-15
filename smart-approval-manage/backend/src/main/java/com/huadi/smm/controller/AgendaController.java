package com.huadi.smm.controller;

import com.huadi.smm.config.Result;
import com.huadi.smm.dao.ApproveTaskMapper;
import com.huadi.smm.dto.AiGenerateRequest;
import com.huadi.smm.dto.HandleApproveRequest;
import com.huadi.smm.dto.MeetingCreateRequest;
import com.huadi.smm.entity.*;
import com.huadi.smm.service.*;
import com.huadi.smm.service.ApproveFlowEngine;
import com.huadi.smm.service.BatchApproveService;
import com.huadi.smm.service.LlmAuditService;
import com.huadi.smm.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    @Autowired
    private AttendeeService attendeeService;
    @Autowired
    private MaterialService materialService;
    @Autowired
    private ApproveRecordService approveRecordService;

    @Value("${upload.path}")
    private String uploadPath;

    // ========== 会议信息 ==========
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

    @PostMapping("/meetings")
    public Result<MeetingInfo> createMeeting(@Valid @RequestBody MeetingCreateRequest req) {
        MeetingInfo meeting = new MeetingInfo();
        meeting.setTitle(req.getTitle());
        meeting.setMeetingType(req.getMeetingType());
        meeting.setDeptId(req.getDeptId());
        meeting.setHostId(req.getHostId());
        meeting.setStartTime(req.getStartTime());
        meeting.setEndTime(req.getEndTime());
        meeting.setLocation(req.getLocation());
        meeting.setCreatorId(1L);
        MeetingInfo saved = meetingService.save(meeting);

        if (req.getAttendeeIds() != null && !req.getAttendeeIds().isEmpty()) {
            for (Long userId : req.getAttendeeIds()) {
                MeetingAttendee attendee = new MeetingAttendee();
                attendee.setMeetingId(saved.getId());
                attendee.setUserId(userId);
                attendee.setRoleType(2);
                attendee.setAttendStatus(0);
                attendee.setInviteTime(new Date());
                attendeeService.addAttendee(attendee);
            }
        }
        return Result.ok(saved);
    }

    // ========== 议程 ==========
    @PostMapping("/ai-generate")
    public Result<List<String>> aiAgendaGenerate(@Valid @RequestBody AiGenerateRequest data) {
        try {
            List<String> result = agendaService.createAiAgenda(data.getMeetingId(), data.getDeptName(),
                    data.getMeetingType());
            return Result.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.fail(e.getMessage(), 400);
        } catch (Exception e) {
            return Result.fail("服务器内部错误", 500);
        }
    }

    @Autowired
    private ApproveFlowEngine approveFlowEngine;
    @Autowired
    private ApproveTaskMapper approveTaskMapper;

    @PostMapping("/{meetingId}/submit")
    public Result<Boolean> submitApprove(@PathVariable Long meetingId) {
        approveFlowEngine.start(meetingId, 1L);
        return Result.ok(true);
    }

    @PostMapping("/{meetingId}/handle")
    public Result<Boolean> handleApprove(@PathVariable Long meetingId,
                                         @Valid @RequestBody HandleApproveRequest data) {
        List<ApproveTask> tasks = approveTaskMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ApproveTask>()
                        .eq("meeting_id", meetingId)
                        .eq("approver_id", data.getApproverId())
                        .eq("status", 0));
        if (tasks.isEmpty()) return Result.fail("没有待审批任务", 400);
        approveFlowEngine.handle(tasks.get(0).getId(), data.getAction(), data.getOpinion());
        return Result.ok(true);
    }

    @GetMapping("/{meetingId}/records")
    public Result<List<ApproveRecord>> listRecords(@PathVariable Long meetingId) {
        return Result.ok(approveRecordService.listByMeetingId(meetingId));
    }

    // ========== 归档 ==========
    @PostMapping("/{meetingId}/archive")
    public Result<Boolean> archiveMeeting(@PathVariable Long meetingId) {
        boolean result = meetingService.archiveMeeting(meetingId);
        if (!result) {
            return Result.fail("归档失败，会议不存在或状态不允许", 400);
        }
        return Result.ok(true);
    }

    // ========== 参会人 ==========
    @GetMapping("/{meetingId}/attendees")
    public Result<List<MeetingAttendee>> listAttendees(@PathVariable Long meetingId) {
        return Result.ok(attendeeService.listByMeetingId(meetingId));
    }

    @PostMapping("/{meetingId}/attendees")
    public Result<MeetingAttendee> addAttendee(@PathVariable Long meetingId, @RequestBody MeetingAttendee attendee) {
        attendee.setMeetingId(meetingId);
        return Result.ok(attendeeService.addAttendee(attendee));
    }

    @DeleteMapping("/attendees/{attendeeId}")
    public Result<Boolean> deleteAttendee(@PathVariable Long attendeeId) {
        attendeeService.deleteAttendee(attendeeId);
        return Result.ok(true);
    }

    // ========== 材料 ==========
    @GetMapping("/{meetingId}/materials")
    public Result<List<MeetingMaterial>> listMaterials(@PathVariable Long meetingId) {
        return Result.ok(materialService.listByMeetingId(meetingId));
    }

    @PostMapping("/{meetingId}/materials/upload")
    public Result<MeetingMaterial> uploadMaterial(
            @PathVariable Long meetingId,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return Result.fail("文件不能为空", 400);
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            return Result.fail("文件名获取失败", 400);
        }

        File dir = new File(uploadPath);
        if (!dir.exists() && !dir.mkdirs()) {
            return Result.fail("创建上传目录失败", 500);
        }

        int lastDot = originalName.lastIndexOf(".");
        String suffix = lastDot > 0 ? originalName.substring(lastDot) : "";
        String fileName = UUID.randomUUID() + suffix;

        File dest = new File(dir, fileName);
        file.transferTo(dest);

        MeetingMaterial material = new MeetingMaterial();
        material.setMeetingId(meetingId);
        material.setMaterialName(originalName);
        material.setFileUrl("/uploads/materials/" + fileName);
        material.setFileSize(file.getSize());

        materialService.saveMaterial(material);

        try {
            String auditResult = llmAuditService.auditMaterial(material.getMaterialName(), "材料内容待提取");
            System.out.println("[合规预审] materialId=" + material.getId() + ", result=" + auditResult);
        } catch (Exception e) {
            System.out.println("[合规预审] 调用失败: " + e.getMessage());
        }

        return Result.ok(material);
    }

    @DeleteMapping("/materials/{materialId}")
    public Result<Boolean> deleteMaterial(@PathVariable Long materialId) {
        materialService.deleteMaterial(materialId);
        return Result.ok(true);
    }

    @Autowired
    private LlmAuditService llmAuditService;

    @PostMapping("/materials/{materialId}/audit")
    public Result<String> auditMaterial(@PathVariable Long materialId, @RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "");
        String result = llmAuditService.auditMaterial("材料" + materialId, content);
        return Result.ok(result);
    }

    @PostMapping("/{meetingId}/pre-opinion")
    public Result<String> preOpinion(@PathVariable Long meetingId) {
        MeetingInfo meeting = meetingService.getById(meetingId);
        if (meeting == null) return Result.fail("会议不存在", 404);
        String result = llmAuditService.generatePreOpinion(meeting.getTitle(), "会议材料摘要待补充");
        return Result.ok(result);
    }

    @PostMapping("/{meetingId}/publish")
    public Result<Boolean> publishMeeting(@PathVariable Long meetingId) {
        boolean result = meetingService.publishMeeting(meetingId);
        if (!result) {
            return Result.fail("发布失败，会议不存在或状态不允许", 400);
        }
        return Result.ok(true);
    }

    @Autowired
    private BatchApproveService batchApproveService;

    @PostMapping("/batch-archive")
    public Result<String> batchArchive(@RequestBody List<Long> meetingIds) {
        batchApproveService.batchArchive(meetingIds);
        return Result.ok("批量归档任务已提交");
    }

    @GetMapping("/meetings/search")
    public Result<List<MeetingInfo>> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer approveStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MeetingInfo> qw = new
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (title != null && !title.isEmpty()) qw.like("title", title);
        if (approveStatus != null) qw.eq("approve_status", approveStatus);
        if (startDate != null) qw.ge("DATE(start_time)", startDate);
        if (endDate != null) qw.le("DATE(end_time)", endDate);
        qw.orderByDesc("create_time");
        return Result.ok(meetingService.list(qw));
    }
}