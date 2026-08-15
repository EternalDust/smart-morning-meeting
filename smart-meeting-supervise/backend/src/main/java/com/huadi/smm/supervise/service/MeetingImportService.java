package com.huadi.smm.supervise.service;

import java.util.Map;

public interface MeetingImportService {

    /**
     * 从汇报交互读接口导入问题（互动提问/反馈 + 会议摘要）
     * @param meetingId 会议ID
     * @return {meetingId, imported, skipped, messages}
     */
    Map<String, Object> importFromMeeting(Long meetingId);
}
