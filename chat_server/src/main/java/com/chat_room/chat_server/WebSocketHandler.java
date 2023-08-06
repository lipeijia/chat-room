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

    }
    @JsonProperty("name")
    public String name;

//    @JsonProperty("age")
//    public int age;

    public WebSocketSession socket;
}
class Message{
    public int kind;
    public String data;
}
class Room{
    private volatile static Room Instance;
    private Room(){
        names = new ArrayList<>();
        sockets = new ArrayList<>();
        ids = new ArrayList<>();
    }
    public static synchronized  Room getInstance(){
        if(Instance == null){
            synchronized (Room.class){
                if(Instance == null)
                    Instance = new Room();
            }
        }
        return Instance;
    }
    public boolean Remove(WebSocketSession socket){
        int index = sockets.indexOf(socket);
        if(index < 0)
            return false;
        sockets.remove(index);
        names.remove(index);
        ids.remove(index);

        return true;
    }
    public boolean Add(String name, WebSocketSession s){
        if(sockets.contains(s))
            return false;
        names.add(name);
        sockets.add(s);
        ids.add(s.getId());

        return true;
    }
    public HashMap<String , Object> Room_data(){
        HashMap<String , Object> m = new HashMap<>();
        m.put("names", names);
        m.put("ids", ids);
        return m;
    }

    private List<String> names;
    private List<String> ids;
    private List<WebSocketSession> sockets;
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
//        sessionMap.put(session.getId(), session);
        ObjectMapper objectMapper = new ObjectMapper();

//        m.data = session.getId();
//        Room_guy guys[] = new Room_guy[2];
//        guys[0] = new Room_guy("kk", 11);
//        guys[1] = new Room_guy("cc", 22);
//        String d = objectMapper.writeValueAsString(guys);
//        m.data = d;
//        String guys_id[] = (String[])sessionMap.keySet().toArray();
//        sessionMap.values();
//        sessionMap.get(session.getId()).sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));

//        boolean success = Room.getInstance().Add();
        Map<String, String> s = buildQueryMap(session.getUri().getQuery());
        if(!s.containsKey("name") || s.get("name") == ""){
            return;
        }
        Message m = new Message();
        m.kind = 0;
        boolean success = Room.getInstance().Add(s.get("name"), session);

        Map<String, Object> map = Room.getInstance().Room_data();
        m.data = objectMapper.writeValueAsString(map);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));
        System.out.println("Connection established from " + session.toString() +
                " @ " + Instant.now().toString());
    }
    private static Map<String, String> buildQueryMap(String query) {
        if (query == null)
            return null;

        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String[] currentParam = param.split("=");
            if (currentParam.length != 2)
                continue;
            String name = currentParam[0];
            String value = currentParam[1];
            map.put(name, value);
        }
        return map;
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

        Room.getInstance().Remove(session);
        System.out.println("Connection closed by " + session.toString() +
                " @ " + Instant.now().toString());
    }
}
