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
import java.util.*;
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
    private   List<WebSocketSession> sockets;
    public RoomData(){
        guys = new ArrayList<>();
        sockets = new ArrayList<>();
    }
    public int Remove(WebSocketSession socket){
        int index;

        index = sockets.indexOf(socket);
        if(index == -1)
            return -1;
        sockets.remove(index);
        guys.remove(index);


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
    public List<WebSocketSession>  GetSockets(){
        return sockets;
//        HashMap<String , Object> m = new HashMap<>();
//        m.put("names", names);
//        m.put("ids", ids);return m;
    }

}
class Room extends HashMap<String, RoomData>{
    private volatile static Room Instance;

    private Room(){ }
    public static RoomData getInstance(String key){
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
    volatile Stack<WebSocketSession> removedSocket = new Stack<>();

    public WebSocketHandler(){
        new Thread(()->{
            while (true){
                try {
                    Thread.sleep(600);
                    List<Message> temp = new ArrayList<>();
                    final  String path = removedSocket.size() > 0  ?  removedSocket.get(0).getUri().getPath() : "";
                    while (removedSocket.size() > 0){
                        WebSocketSession session = removedSocket.pop();
                        int leftIdx = Room.getInstance(path).Remove(session);
                        Message m = new Message();
                        m.writeDisConnect(leftIdx);
                        temp.add(m);
                    }
                    if(path != ""){
                        new Thread(() -> {
                            ObjectMapper objectMapper = new ObjectMapper();
                            List<WebSocketSession> sockets  = Room.getInstance(path).GetSockets();
                            for (int i = 0; i < sockets.size(); i++) {

                                for (int j = 0; j < temp.size(); j++) {
                                    try{
                                        sockets.get(i).sendMessage(new TextMessage(objectMapper.writeValueAsString(temp.get(j))));
                                    }
                                    catch (Exception exception){
                                        System.out.println(exception.toString());
                                    }
                                }
                            }
                        }).start();
                 }
                }
                catch (Exception ex){
                }
            }
        }).start();
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
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
        WebSocketSession ss = null;
        for (int i = 0; i < sockets.size(); i++) {
            m.data = objectMapper.writeValueAsString(newGuy);
            synchronized(sockets.get(i)){
                sockets.get(i).sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));
       
            }
        }
        m.kind = 0;
        boolean success = Room.getInstance(path).Add(s.get("name"), session);
        guys = Room.getInstance(path).RoomGuys();
        m.data = objectMapper.writeValueAsString(guys);
        int p2 = session.hashCode();
        synchronized(session){
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));
        }

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
                
                for (int i = 0; i < sockets.size(); i++) {
                    sockets.get(i).sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));
                }
                //pm
                // sockets.get(pm.receiverIdx).sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));
             
            }
            int kk = 1;
        } catch (IOException e) {
            int kk = 1;
        }
        float a = 11;

    }
    @Override
    public   void   afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception {
        removedSocket.add(session);

        System.out.println("Connection closed by " + session.toString() +
                " @ " + Instant.now().toString());
    }
}
