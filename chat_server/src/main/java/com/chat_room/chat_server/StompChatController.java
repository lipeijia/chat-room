package com.chat_room.chat_server;

import org.springframework.stereotype.Controller;

import com.chat_room.chat_server.RoomService.RoomGuy;
import com.chat_room.chat_server.StompChatController.PrivateMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
@Controller
public class StompChatController {
    static class  PrivateMessage {
        private String text;
        private String sender;
    
        // Getters and setters
        public String getText() {
            return text;
        }
    
        public void setText(String text) {
            this.text = text;
        }
    
        public String getSender() {
            return sender;
        }
    
        public void setSender(String sender) {
            this.sender = sender;
        }
    }
    private final RoomService roomService;
    private final  SimpMessagingTemplate simpMessagingTemplate;
    private final SimpUserRegistry simpUserRegistry;
    public  StompChatController(RoomService rService, SimpMessagingTemplate SimpleMessageTe, SimpUserRegistry simpUserRegistry){
        this.roomService = rService;
        this.simpMessagingTemplate = SimpleMessageTe;
        this.simpUserRegistry = simpUserRegistry;
    }

    //  @MessageMapping("/chat")
    // @SendTo("/topic/messages")
    // public ChatMessage sendMessage(ChatMessage message) {
    //     // chatService.addMessage(message);
    //     return message;
    // }
    @MessageMapping("/test")
    public void handleTest(String message, Principal principal, SimpMessageHeaderAccessor sha) {
        System.out.println("Principal in Controller: " + principal);
        System.out.println("User in SimpMessageHeaderAccessor: " + sha.getUser());
        System.out.println("SessionId in SimpMessageHeaderAccessor: " + sha.getSessionId());
    }
    @MessageMapping("/chat.join")
    public void handleNewUser(String message, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        if (principal == null) {
            System.out.println("handleNewUser - Principal is null");
        } else {
            System.out.println("handleNewUser - Principal: " + principal.getName());
        }
        // 廣播新用戶訊息給其他人
      
        String sessionId = headerAccessor.getSessionId();
        Map<String, Map<String, String>> transformedMap = new HashMap<>();
        Map<String, String> value = new HashMap<>();
        this.roomService.AddUser(message, sessionId, principal.getName());
        value.put("name", message);
        transformedMap.put(principal.getName(), value);
        this.simpMessagingTemplate.convertAndSend("/topic/newUser", transformedMap);
        var v = this.simpUserRegistry.getUser(principal.getName());
        var data = this.roomService.guysMap.values();
        transformedMap = new HashMap<>();
        for (RoomGuy roomGuy : data) {
            value = new HashMap<>();
            value.put("name", roomGuy.name);
            transformedMap.put(roomGuy.userId, value);
        }

        // 转换为 JSON
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(transformedMap);
            this.simpMessagingTemplate.convertAndSendToUser(
                principal.getName(),"/queue/newUser", json);
        } catch (Exception e) {
            // TODO: handle exception
        }
        int k =1;
    
        // 傳送歷史訊息給新用戶
        // this.simpMessagingTemplate.convertAndSendToUser(
        //     message.getSender(), "/queue/history", chatService.getMessageHistory()
        // );
    }
    @MessageMapping("/chat.send")
    public void handleChat(String message, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        Principal userPrincipal = headerAccessor.getUser();
        // String userName = this.roomService.GetUserName(headerAccessor.getSessionId());
        Map<String,String> msg = new HashMap<>();
        msg.put("userId", principal.getName());
        msg.put("message", message);

        // 廣播新用戶訊息給其他人
        this.simpMessagingTemplate.convertAndSend("/topic/message", msg);
     
        // 傳送歷史訊息給新用戶
        // this.simpMessagingTemplate.convertAndSendToUser(
        //     message.getSender(), "/queue/history", chatService.getMessageHistory()
        // );
    }
    @MessageMapping("/chat.send.private")
    public void handleChat(@Payload PrivateMessage message, Principal principal) {
    
        Map<String,String> msg = new HashMap<>();
        msg.put("userId", principal.getName());
        msg.put("message", message.getText());
        // 廣播新用戶訊息給其他人
        this.simpMessagingTemplate.convertAndSendToUser(message.getSender(),"/queue/private", msg);
     
        // 傳送歷史訊息給新用戶
        // this.simpMessagingTemplate.convertAndSendToUser(
        //     message.getSender(), "/queue/history", chatService.getMessageHistory()
        // );
    }
    @MessageMapping("/disconnect")
    public void handleDisconnect(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
    
        String sessionId = headerAccessor.getSessionId();
        // Map<String,String> msg = new HashMap<>();
        // msg.put("userId", principal.getName());
        // // 廣播新用戶訊息給其他人
        // this.simpMessagingTemplate.convertAndSendToUser(message.getSender(),"/queue/private", msg);
        this.roomService.RemoveUser(sessionId);
        this.simpMessagingTemplate.convertAndSend("/topic/disconnect", principal.getName());
        // 傳送歷史訊息給新用戶
        // this.simpMessagingTemplate.convertAndSendToUser(
        //     message.getSender(), "/queue/history", chatService.getMessageHistory()
        // );
    }
}
