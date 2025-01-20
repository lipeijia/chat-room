package com.chat_room.chat_server;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
public class CustomHandshakeInterceptor implements HandshakeInterceptor  {
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Exception exception) {
        // TODO Auto-generated method stub
                                                                     int k = 1;
    }
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {
        String query = request.getURI().getQuery();
        String userId = null;
        String uri = request.getURI().toString();
        if (uri.contains("/stomp/info")) {
            return true; // 跳过处理
        }
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && pair[0].equals("userId")) {
                    userId = pair[1];
                    break;
                }
            }
        }
        else {
            // 生成一個隨機的 userId，並設定 Principal
            userId = UUID.randomUUID().toString();
          
            };
        // userId = UUID.randomUUID().toString();
        if (userId != null) {
            attributes.put("user", userId); // 存储到 attributes 中
        }
        
        return true; // 允許握手
     
    }
    
}
