package com.chat_room.rabbit;

import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {
     private final RabbitTemplate rabbitTemplate;
     private final SimpMessagingTemplate simpMessagingTemplate;
     

    @Autowired
    public MessageProducer(RabbitTemplate rabbitTemplate, SimpMessagingTemplate simpMessagingTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }
    public void sendBroadCastMessage( Map<String,String> message, String roomId) {
        // 广播消息发送到公共交换机
        rabbitTemplate.convertAndSend("chatExchange", String.format("room.%s", roomId), message);
        System.out.println("广播消息发已发送: " + message);
    }
    public void sendJoinMessage(Map<String, Map<String, String>> message, String roomId) {
        // 广播消息发送到公共交换机
        rabbitTemplate.convertAndSend("chatExchange", String.format("room.%s.join", roomId), message);
        System.out.println("广播消息发已发送: " + message);
    }
    public void RabbitBroadCast(Map<String,String> message, String roomId) {
        String ss = String.format("/topic/%s/message", roomId);
        // 廣播新用戶訊息給其他人
        this.simpMessagingTemplate.convertAndSend(ss, message);
    }
    public void RabbitJoin(Map<String, Map<String, String>> message, String roomId) {
        String ss = String.format("/topic/%s/newUser", roomId);
        // 廣播新用戶訊息給其他人
        this.simpMessagingTemplate.convertAndSend(ss, message);
    }
    public void RabbitJoinSelf(Map<String, Map<String, String>> message, String userId) {
        String ss = String.format("/topic/%s/newUser", userId);
        // 廣播聊天室資料給新用戶
        this.simpMessagingTemplate.convertAndSendToUser(userId,"/queue/newUser", message);
    }
 
    public void sendPrivateMessage(String target, String message) {
        rabbitTemplate.convertAndSend("chatExchange", target, message);
        System.out.println("单人消息已发送: " + message);
    }
}
