package com.smartmeeting.ws;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/api/meeting/realtime/push/{meetingId}")
public class RealtimeServer {

    private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, Session>> rooms = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("meetingId") Long meetingId) {
        rooms.computeIfAbsent(meetingId, k -> new ConcurrentHashMap<>())
              .put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("meetingId") Long meetingId) {
        ConcurrentHashMap<String, Session> room = rooms.get(meetingId);
        if (room != null) {
            room.remove(session.getId());
            if (room.isEmpty()) rooms.remove(meetingId);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
    }

    public static void broadcast(Long meetingId, String message) {
        ConcurrentHashMap<String, Session> room = rooms.get(meetingId);
        if (room != null) {
            room.values().forEach(session -> {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
