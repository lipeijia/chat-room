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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
class Room_guy {
    public Room_guy(String name, String id){
        this.name = name;
        // this.id = id;
    }
    @JsonProperty("name")
    public String name;

    // @JsonProperty("id")
    // public String id;

}
class Message{
    public int kind;
    public String data;

    public void writeDisConnect(int leftIndx){
        this.kind = 3;
        this.data = String.format("{\"leftIdx\": %d}", leftIndx);
    }
    public void writePM(int senderIdx, int receiverIdx, String data)
    {
        this.kind = 2;
        String msg = """
{"senderIdx": %d, "receiverIdx": %d, "data": "%s"}""";
        
        this.data = String.format(msg, senderIdx, receiverIdx, data);
        //senderIdx: number (傳送者的 index),
        // receiverIdx: number (接收者的 index),
        // data: number (訊息)
    }

}
class sendMessage{
    public int kind;
    public int index;
    public String message;
}
class ReceiveBase
{
    public int kind;
}
class PM extends ReceiveBase
{
    public int senderIdx;
    public int receiverIdx;
    public String data;
}

class RoomData{
    private List<Room_guy> guys;
    private List<WebSocketSession> sockets;
    public RoomData(){
        guys = new ArrayList<>();
        sockets = new ArrayList<>();
    }
     public int Remove(WebSocketSession socket){
        int index = sockets.indexOf(socket);
        if(index < 0)
            return -1;
        try {
            sockets.remove(index);
            guys.remove(index);
        } catch (Exception e) {
            // TODO: handle exception
            return -1;
        }
      
        return index;
    }
    public boolean Add(String name, WebSocketSession s){
        if(sockets.contains(s))
            return false;
        guys.add(new Room_guy(name, s.getId()));
        sockets.add(s);
//        ids.add(s.getId());

        return true;
    }
    public List<Room_guy> RoomGuys(){
        return guys;
//        HashMap<String , Object> m = new HashMap<>();
//        m.put("names", names);
//        m.put("ids", ids);return m;
    }
    public List<WebSocketSession> GetSockets(){
        return sockets;
//        HashMap<String , Object> m = new HashMap<>();
//        m.put("names", names);
//        m.put("ids", ids);return m;
    }

}
class Room extends HashMap<String, RoomData>{
    private volatile static Room Instance;

    private Room(){ }
    public static synchronized  RoomData getInstance(String key){
        if(Instance == null){
            synchronized (Room.class){
                if(Instance == null)
                    Instance = new Room();
            }
        }
      
        if(!Instance.containsKey(key)){
            Instance.put(key, new RoomData());
        }
        return Instance.get(key);
    }
   

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
        String path = session.getUri().getPath();

        Map<String, String> s = buildQueryMap(session.getUri().getQuery());
        if(!s.containsKey("name") || s.get("name") == ""){
            return;
        }
        Message m = new Message();

        List<Room_guy> guys = null;
        List<WebSocketSession> sockets = Room.getInstance(path).GetSockets();
        Room_guy newGuy = new Room_guy(s.get("name"), session.getId());
        m.kind = 1;
        for (int i = 0; i < sockets.size(); i++) {
            m.data = objectMapper.writeValueAsString(newGuy);
            sockets.get(i).sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));
        }
     
        m.kind = 0;
        boolean success = Room.getInstance(path).Add(s.get("name"), session);

        guys = Room.getInstance(path).RoomGuys();
        m.data = objectMapper.writeValueAsString(guys);
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
        String path = session.getUri().getPath();
        String s = message.getPayload();
        ObjectMapper mapper = new ObjectMapper();
        Message recvMessage = null;
        Pattern p = Pattern.compile("^\\{\"kind\":(\\d+),.*");
        
        int kind = -1;
        Matcher mat = p.matcher(s);
        if (mat.find( )) {
            System.out.println("Found value: " + mat.group(1) );
            kind = Integer.parseInt(mat.group(1));
        } 
        else
            return;
        try{
            if(kind == 2){
                List<WebSocketSession> sockets = Room.getInstance(path).GetSockets();
                int idx = sockets.indexOf(session);
                if (idx < 0 )
                    return;

                ObjectMapper objectMapper = new ObjectMapper();
                PM pm = objectMapper.readValue(s, PM.class);
                Message m = new Message();    
                
                m.writePM(pm.senderIdx, pm.receiverIdx, pm.data);
                // m.data = _sendMessage.message;
                sockets.get(pm.receiverIdx).sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));
                // session.sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));
            }
            int kk = 1;
        } catch (IOException e) {
            int kk = 1;
        }
        float a = 11;


    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        String path = session.getUri().getPath();
        int leftIdx = Room.getInstance(path).Remove(session);
        ObjectMapper objectMapper = new ObjectMapper();
        Message m = new Message();    
        m.writeDisConnect(leftIdx);
        List<WebSocketSession> sockets = Room.getInstance(path).GetSockets();
        for (WebSocketSession webSocketSession : sockets) {
            webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));
        }
        System.out.println("Connection closed by " + session.toString() +
                " @ " + Instant.now().toString());
    }
}
