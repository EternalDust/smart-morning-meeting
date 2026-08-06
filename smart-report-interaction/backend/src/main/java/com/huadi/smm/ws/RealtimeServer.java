package com.huadi.smm.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/api/meeting/realtime/push/{meetingId}")
public class RealtimeServer {

    private static final Logger log = LoggerFactory.getLogger(RealtimeServer.class);

    private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, Session>> rooms = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("meetingId") Long meetingId) {
        rooms.computeIfAbsent(meetingId, k -> new ConcurrentHashMap<>())
              .put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("meetingId") Long meetingId) {
        removeSession(meetingId, session.getId());
    }

    @OnError
    public void onError(Session session, @PathParam("meetingId") Long meetingId, Throwable error) {
        removeSession(meetingId, session.getId());
        log.warn("WebSocket 连接异常 meetingId={}: {}", meetingId, error.getMessage());
    }

    // 纯服务端推送，客户端不上行业务消息
    @OnMessage
    public void onMessage(String message, Session session) {
    }

    private static void removeSession(Long meetingId, String sessionId) {
        ConcurrentHashMap<String, Session> room = rooms.get(meetingId);
        if (room != null) {
            room.remove(sessionId);
            if (room.isEmpty()) rooms.remove(meetingId);
        }
    }

    public static void broadcast(Long meetingId, String message) {
        ConcurrentHashMap<String, Session> room = rooms.get(meetingId);
        if (room != null) {
            room.values().removeIf(session -> {
                try {
                    session.getBasicRemote().sendText(message);
                    return false;
                } catch (IOException e) {
                    return true;
                }
            });
            if (room.isEmpty()) rooms.remove(meetingId);
        }
    }
}
