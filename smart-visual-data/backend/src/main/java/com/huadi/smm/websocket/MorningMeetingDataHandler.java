package com.huadi.smm.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
public class MorningMeetingDataHandler extends TextWebSocketHandler {

    // 保存所有在线的WebSocket会话
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("新的前端大屏建立连接，当前连接数：{}", sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("大屏连接断开，当前连接数：{}", sessions.size());
    }

    /**
     * 模拟Spark实时计算后，后端向前端推送更新（推送参会率、问题解决率及风险预警）
     * 实际业务中应由Kafka消费者或Redis监听器触发推送。
     */
    public void pushRealTimeDataToDashboard(String jsonData) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonData));
                } catch (IOException e) {
                    log.error("WebSocket 数据推送失败", e);
                }
            }
        }
    }
}
