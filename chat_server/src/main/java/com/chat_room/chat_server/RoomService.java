package com.chat_room.chat_server;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.chat_room.chat_server.RoomService.RoomGuy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Service
public class RoomService {
   
    public HashMap<String, RoomGuy> guysMap = new HashMap<>();
    public HashMap<String, Room> roomsMap = new HashMap<>();
    class RoomGuy {
        @JsonProperty("name")
        public String name;
        public String userId;
        public String roomId;
        public RoomGuy(String name, String userId, String roomId) {
            this.name = name;
            this.userId = userId;
            this.roomId = roomId;
        }
    }
    public boolean AddUser(String name, String roomId, String sessionId, String userId){
        if(guysMap.containsKey(sessionId))
            return false;
        
        roomsMap.putIfAbsent(roomId, new Room(roomId));

        Room room = roomsMap.get(roomId);
        RoomGuy newGuy = new RoomGuy(name, userId, roomId);
        boolean checkmember = room.addMember(sessionId, newGuy);
        if(!checkmember)
            return false;

        guysMap.put(sessionId, newGuy);
        return true;
    }
    public boolean RemoveUser(String sessionId){
        if(!guysMap.containsKey(sessionId))
            return false;
        String roomId = guysMap.get(sessionId).roomId;
        roomsMap.get(roomId).removeMember(sessionId);
        int n =   roomsMap.get(roomId).getMemeberSize();
        if( n == 0){
            roomsMap.remove(roomId);
        }
        guysMap.remove(sessionId);

        return true;
    }
    public String GetUserName(String sessionId){
        if(!guysMap.containsKey(sessionId))
            return "";
        
        return guysMap.get(sessionId).name;
    }
    public void GetRoomMembers(){

    }


    class Room {
        private String roomId;
        private Map<String, RoomGuy> members; // 存放每個房間中的成員

        public Room(String roomId) {
            this.roomId = roomId;
            this.members = new HashMap<>();
        }

        public String getRoomId() {
            return roomId;
        }

        public Map<String, RoomGuy> getMembers() {
            return members;
        }

        public boolean addMember(String sessionId, RoomGuy guy) {
            if (members.containsKey(sessionId)) {
                return false; // 成員已經在房間內
            }
            members.put(sessionId, guy);
            return true;
        }

        public boolean removeMember(String sessionId) {
            if (members.containsKey(sessionId)) {
                members.remove(sessionId);
                return true;
            }
            return false; // 成員不存在
        }
        public int getMemeberSize(){
            return members.keySet().size();
        }
    }

}
