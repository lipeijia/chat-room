package com.chat_room.chat_server;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final WebSocketHandler handler;

    WebSocketConfig(WebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // registry.addHandler(handler, "/ws").setAllowedOrigins("http://localhost:3000");
        registry.addHandler(handler, "/ws").setAllowedOrigins("*");
        registry.addHandler(handler, "/ws2").setAllowedOrigins("*");
    }
}