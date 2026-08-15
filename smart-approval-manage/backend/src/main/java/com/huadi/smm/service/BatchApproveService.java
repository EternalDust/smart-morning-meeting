package com.huadi.smm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class BatchApproveService {

    @Autowired
    private MeetingService meetingService;

    @Async("taskExecutor")
    public CompletableFuture<String> batchArchive(List<Long> meetingIds) {
        int success = 0;
        for (Long id : meetingIds) {
            if (meetingService.archiveMeeting(id)) success++;
        }
        return CompletableFuture.completedFuture("归档完成: " + success + "/" + meetingIds.size());
    }
}