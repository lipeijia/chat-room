package com.chat_room.chat_server;
import com.chat_room.rabbit.MessageProducer;
import org.springframework.stereotype.Controller;

import com.chat_room.chat_server.RoomService.RoomGuy;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final GenerateMessageService generateMessageService;
    private final MessageProducer messageProducer;
    public  StompChatController(RoomService rService, SimpMessagingTemplate SimpleMessageTe, 
    GenerateMessageService generateMessageService, MessageProducer messageProducer){
        this.roomService = rService;
        this.simpMessagingTemplate = SimpleMessageTe;
        this.generateMessageService = generateMessageService;
        this.messageProducer = messageProducer;
    }

    @MessageMapping("/test")
    public void handleTest(String message, Principal principal, SimpMessageHeaderAccessor sha) {
        System.out.println("Principal in Controller: " + principal);
        System.out.println("User in SimpMessageHeaderAccessor: " + sha.getUser());
        System.out.println("SessionId in SimpMessageHeaderAccessor: " + sha.getSessionId());
    }
    public static class JoinDto {
        public String name;
        public String roomId;
    
        // Getter 和 Setter
    }
    
    @MessageMapping("/chat.join")
    public void handleNewUser(@Payload JoinDto joinDto, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        if (principal == null) {
            System.out.println("handleNewUser - Principal is null");
        } else {
            System.out.println("handleNewUser - Principal: " + principal.getName());
        }
        
        String sessionId = headerAccessor.getSessionId();
        Map<String, Map<String, String>> transformedMap = new HashMap<>();
        Map<String, String> value = new HashMap<>();
        this.roomService.AddUser(joinDto.name, joinDto.roomId, sessionId, principal.getName());
        value.put("name", joinDto.name);
        value.put("sessionId", sessionId);
        transformedMap.put(principal.getName(), value);
        this.messageProducer.sendJoinMessage(transformedMap, joinDto.roomId);
    }
    @MessageMapping("/chat.send/room.{roomId}")
    public void handleChat(@DestinationVariable String roomId, String message, SimpMessageHeaderAccessor headerAccessor, Principal principal) {

        Map<String,String> msg = new HashMap<>();
        msg.put("userId", principal.getName());
        msg.put("message", message);
        this.messageProducer.sendBroadCastMessage(msg, roomId);
    }
    @MessageMapping("/chat.send.private")
    public void handleChat(@Payload PrivateMessage message, Principal principal) {
    
      
        if(message.getSender().equals("aiUser") ){
            Map<String,String> msg = new HashMap<>();
            msg.put("userId", principal.getName());
            msg.put("message", message.getText());
            generateMessageService.generateText(message.getText(), message.getSender(), principal.getName());
        }
        else{
            Map<String,String> msg = new HashMap<>();
            msg.put("userId", principal.getName());
            msg.put("message", message.getText());
            this.messageProducer.sendPrivateMessage(msg, message.getSender());
        }
           

       
     
    }
    // /app/disconnect/room.${roomId}
    @MessageMapping("/room.{roomId}/disconnect")
    public void handleDisconnect(@DestinationVariable String roomId, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
    
        String sessionId = headerAccessor.getSessionId();

        this.roomService.RemoveUser(sessionId);
        String ss = String.format("/topic/room.%s/disconnect", roomId);
        this.simpMessagingTemplate.convertAndSend(ss, principal.getName());

    }
}
