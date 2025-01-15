package com.chat_room.chat_server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
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

// 房間使用者類別
class Room_guy {
    @JsonProperty("name")
    public String name;

    @JsonIgnore
    public WebSocketSession socket;

    public Room_guy(String name, WebSocketSession socket) {
        this.name = name;
        this.socket = socket;
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
    public String senderIdx;
    public String receiverIdx;
    public String data;
}

// 訊息類別
class Message {
    public int kind;
    public String data;

    public void writeDisConnect(String leftIndx) {
        this.kind = 3;
        this.data = String.format("{\"leftIdx\": \"%s\"}", leftIndx);
    }

    public void writePM(String senderIdx, String receiverIdx, String data) {
        this.kind = 2;
        String msg = """
{"senderIdx": "%s", "receiverIdx": "%s", "data": "%s"}""";
        this.data = String.format(msg, senderIdx, receiverIdx, data);
    }
}

// 房間數據類別
class RoomData {
    private List<Room_guy> guys;
    private List<WebSocketSession> sockets;
    public HashMap<String, Room_guy> guysMap;

    public RoomData() {
        this.guys = new ArrayList<>();
        this.sockets = new ArrayList<>();
        this.guysMap = new HashMap<>();
    }

    public String Remove(WebSocketSession socket) {
        Room_guy result = guysMap.remove(socket.getId());
        return result != null ? result.socket.getId() : null;
    }

    public boolean Add(Room_guy roomGuy) {
        if (guysMap.containsKey(roomGuy.socket.getId())) return false;
        guysMap.put(roomGuy.socket.getId(), roomGuy);
        return true;
    }

    public List<Room_guy> RoomGuys() {
        return guys;
    }

    public List<WebSocketSession> GetSockets() {
        return sockets;
    }
}

// 房間類別 (單例模式)
class Room extends HashMap<String, RoomData> {
    private static volatile Room instance;

    private Room() {
    }

    public static RoomData getInstance(String key) {
        if (instance == null) {
            synchronized (Room.class) {
                if (instance == null) instance = new Room();
            }
        }
        return instance.computeIfAbsent(key, k -> new RoomData());
    }
}

// WebSocket 處理類別
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private volatile Stack<WebSocketSession> removedSocket = new Stack<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    public WebSocketHandler() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(500);
                    handleRemovedSockets();
                } catch (Exception ignored) {
                }
            }
        }).start();
    }

    private void handleRemovedSockets() {
        // List<Message> temp = new ArrayList<>();
        String path = removedSocket.isEmpty() ? "" : removedSocket.peek().getUri().getPath();
        
        while (!removedSocket.isEmpty()) {
            WebSocketSession session = removedSocket.pop();
            String leftIdx = Room.getInstance(path).Remove(session);

            Message m = new Message();
            m.writeDisConnect(leftIdx);
            sendMessagesToSockets(path, m);
            // temp.add(m);
        }
        // if (!path.isEmpty()) {
        //     sendMessagesToSockets(path, temp);
        // }
    }

    private void sendMessagesToSockets(String path, Message message) {
        final TextMessage msg;

        try {
            msg = new TextMessage(objectMapper.writeValueAsString(message));
        } catch (Exception ex) {
            System.err.println("Failed to convert message to JSON: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        RoomData room = Room.getInstance(path);
        if (room == null || room.guysMap == null) {
            System.err.println("Invalid room or guysMap for path: " + path);
            return;
        }

        room.guysMap.forEach((key, value) -> {
            try {
                if (value.socket != null && value.socket.isOpen()) {
                    value.socket.sendMessage(msg);
                } else {
                    System.err.println("Socket is not open or is null for key: " + key);
                }
            } catch (Exception ex) {
                System.err.println("Failed to send message to socket for key: " + key);
                ex.printStackTrace();
            }
        });

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String path = session.getUri().getPath();
        Map<String, String> queryParams = buildQueryMap(session.getUri().getQuery());

        if (!queryParams.containsKey("name") || queryParams.get("name").isEmpty()) {
            return;
        }

        Room_guy newGuy = new Room_guy(queryParams.get("name"), session);
        Room.getInstance(path).Add(newGuy);

        // 廣播新成員加入訊息
        Message m = new Message();
        m.kind = 1;
        Map<String, Object> dynamicObject = new HashMap<>();
        dynamicObject.put("guy", newGuy);
        dynamicObject.put("id", session.getId());
        m.data = objectMapper.writeValueAsString(dynamicObject);

        Room.getInstance(path).guysMap.forEach((key, value) -> {
            synchronized (value.socket) {
                try {
                    value.socket.sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 向新成員回傳房間資料
        m.kind = 0;
        Map<String, Object> response = new HashMap<>();
        response.put("guys", Room.getInstance(path).guysMap);
        response.put("id", session.getId());
        m.data = objectMapper.writeValueAsString(response);

        synchronized (session) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));
        }

        System.out.println("Connection established from " + session + " @ " + Instant.now());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String path = session.getUri().getPath();
        String payload = message.getPayload();

        Pattern pattern = Pattern.compile("^\\{\"kind\":(\\d+),.*");

        Matcher matcher = pattern.matcher(payload);

        if (matcher.find() && Integer.parseInt(matcher.group(1)) == 2) {

            handlePrivateMessage(session, payload, path);
        }
    }

    private void handlePrivateMessage(WebSocketSession session, String payload, String path) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PM pm = objectMapper.readValue(payload, PM.class);
            Message m = new Message();
            m.writePM(pm.senderIdx, pm.receiverIdx, pm.data);
            var msg = new TextMessage(objectMapper.writeValueAsString(m));
            if(pm.receiverIdx == ""){
                Room.getInstance(path).guysMap.forEach((key, value) -> {
                    synchronized (value.socket) {
                        try {
                            value.socket.sendMessage(new TextMessage(objectMapper.writeValueAsString(m)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            else{
                Room.getInstance(path).guysMap.get(pm.senderIdx).socket.sendMessage(msg);
                Room.getInstance(path).guysMap.get(pm.receiverIdx).socket.sendMessage(msg);
            }
         
          } catch (IOException ignored) {
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removedSocket.add(session);
        System.out.println("Connection closed by " + session + " @ " + Instant.now());
    }

    private static Map<String, String> buildQueryMap(String query) {
        if (query == null) return null;
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                map.put(keyValue[0], keyValue[1]);
            }
        }
        return map;
    }
}
