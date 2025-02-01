package com.chat_room.chat_server;
import java.security.Principal;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.Message;
@Configuration
@EnableWebSocketMessageBroker
public class stompSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 配置消息代理
        config.enableSimpleBroker("/topic", "/queue"); // 訂閱的目的地
        config.setApplicationDestinationPrefixes("/app"); // 發送到伺服器
        config.setUserDestinationPrefix("/user"); // 私人消息目的地// 發送訊息的前綴
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 註冊 STOMP 端點
       
        registry.addEndpoint("/stomp")
                .setAllowedOriginPatterns("*")// 允許跨域
                .addInterceptors(new CustomHandshakeInterceptor())
                .setHandshakeHandler(new CustomHandshakeHandler())
                .withSockJS()// 使用 SockJS
                .setHeartbeatTime(30000); 
    }
    //    @Override
    // public void configureClientInboundChannel(ChannelRegistration registration) {
    //     registration.interceptors(new MyChannelInterceptor());
    // }
}
class MyChannelInterceptor implements ChannelInterceptor {
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

        // 取得命令
        StompCommand command = headerAccessor.getCommand();
        System.out.println("preSend - Command: " + command);
        System.out.println("preSend - Principal: " + headerAccessor.getUser());
        System.out.println("preSend - SessionId: " + headerAccessor.getSessionId());
        System.out.println("preSend - Destination: " + headerAccessor.getDestination());
        if (StompCommand.CONNECT.equals(command)) {
            // 1. 在 CONNECT 時，從 header 取得 userId
            String userId = headerAccessor.getFirstNativeHeader("userId");
            if (userId == null || userId.isEmpty()) {
                userId = UUID.randomUUID().toString();
            }
            // 2. 將 userId 存進 session attributes
            headerAccessor.getSessionAttributes().put("userId", userId);

            // 3. 設定 Principal
            headerAccessor.setUser(new StompPrincipal(userId));
            System.out.println("Setting Principal in CONNECT: " + userId);

            // 4. 留 mutable 並回傳新 message
            headerAccessor.setLeaveMutable(true);
            return MessageBuilder.createMessage(message.getPayload(), headerAccessor.getMessageHeaders());
        } 
        else {
            // 對於 SEND, SUBSCRIBE, DISCONNECT... 其它命令
            // 如果此時 Principal 是 null，就從 sessionAttributes 中取出
            if (headerAccessor.getUser() == null) {
                Object userIdObj = headerAccessor.getSessionAttributes().get("userId");
                if (userIdObj != null) {
                    String userId = (String) userIdObj;
                    headerAccessor.setUser(new StompPrincipal(userId));
                    System.out.println("Restore Principal in " + command + ": " + userId);

                    headerAccessor.setLeaveMutable(true);
                    return MessageBuilder.createMessage(message.getPayload(), headerAccessor.getMessageHeaders());
                }
            }
        }


        return message;
    }
}
class StompPrincipal implements Principal {
    private String name;

    public StompPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
