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
import java.util.List;
import java.util.Map;
class Room_guy {
    @JsonProperty("name")
    public String name;

    @JsonProperty("age")
    public int age;
}
//@RequiredArgsConstructor
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    public WebSocketHandler(){

    }
    private final List<WebSocketSession> sessionList = new ArrayList<>();


    public List<WebSocketSession> getSessionList() {
        return sessionList;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionList.add(session);
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
        sessionList.remove(session);
        System.out.println("Connection closed by " + session.toString() +
                " @ " + Instant.now().toString());
    }
}
