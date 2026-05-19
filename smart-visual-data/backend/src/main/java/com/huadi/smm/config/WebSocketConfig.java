package com.huadi.smm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.huadi.smm.websocket.MorningMeetingDataHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /**
     * 设计文档要求：通过WebSocket搭建前后端实时通信通道，建立长连接实现数据推模式更新。
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器，并允许跨域
        registry.addHandler(new MorningMeetingDataHandler(), "/ws/morning-meeting")
                .setAllowedOrigins("*");
    }
}
