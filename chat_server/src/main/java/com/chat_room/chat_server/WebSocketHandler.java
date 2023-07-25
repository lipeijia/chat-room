package com.chat_room.chat_server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
class Room_guy {
    public Room_guy(String s, int a){
        name = s;
        age = a;
    }
    @JsonProperty("name")
    public String name;

    @JsonProperty("age")
    public int age;
}
class Message{
    public int kind;
    public String data;
}

//@RequiredArgsConstructor
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    public WebSocketHandler(){

    }
    private final HashMap<String , WebSocketSession> sessionMap = new HashMap<>();


    public HashMap<String , WebSocketSession> getSessionMap() {
        return sessionMap;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        sessionMap.put(session.getId(), session);
        ObjectMapper objectMapper = new ObjectMapper();
        Message m = new Message();
        m.kind = 1;
//        m.data = session.getId();
        Room_guy guys[] = new Room_guy[2];
        guys[0] = new Room_guy("kk", 11);
        guys[1] = new Room_guy("cc", 22);
        String d = objectMapper.writeValueAsString(guys);
        m.data = d;
        sessionMap.get(session.getId()).sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));


        System.out.println("Connection established from " + session.toString() +
                " @ " + Instant.now().toString());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
    {

        String s = message.getPayload();
        ObjectMapper mapper = new ObjectMapper();
        Room_guy guy = null;

        try{
            guy = mapper.readValue(s, Room_guy.class);
            int kk = 1;
        } catch (IOException e) {
            int kk = 1;
        }
        float a = 11;


    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        sessionMap.remove(session.getId());
        System.out.println("Connection closed by " + session.toString() +
                " @ " + Instant.now().toString());
    }
}
