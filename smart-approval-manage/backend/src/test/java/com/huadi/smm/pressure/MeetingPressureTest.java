package com.huadi.smm.pressure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huadi.smm.dto.MeetingCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class MeetingPressureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testConcurrentCreateMeeting() throws Exception {
        int threadCount = 20;
        int requestsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        MeetingCreateRequest req = new MeetingCreateRequest();
                        req.setTitle("压测-" + threadIndex + "-" + j);
                        req.setMeetingType(1);              // 改成 Integer
                        req.setDeptId(1L);
                        req.setHostId(1L);
                        req.setStartTime(new Date());       // 改成 Date
                        req.setEndTime(new Date(System.currentTimeMillis() + 3600000));
                        req.setLocation("会议室A");

                        try {
                            mockMvc.perform(post("/api/agenda/meetings")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(req)))
                                    .andExpect(result -> {
                                        if (result.getResponse().getStatus() == 200) {
                                            successCount.incrementAndGet();
                                        } else {
                                            failCount.incrementAndGet();
                                        }
                                    });
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long endTime = System.currentTimeMillis();
        executor.shutdown();

        int total = threadCount * requestsPerThread;
        long duration = endTime - startTime;

        System.out.println("========== 压测结果 ==========");
        System.out.println("并发线程数: " + threadCount);
        System.out.println("单线程请求数: " + requestsPerThread);
        System.out.println("总请求数: " + total);
        System.out.println("成功: " + successCount.get());
        System.out.println("失败: " + failCount.get());
        System.out.println("总耗时(ms): " + duration);
        System.out.println("平均TPS: " + String.format("%.2f", total * 1000.0 / duration));
        System.out.println("==============================");
    }
}