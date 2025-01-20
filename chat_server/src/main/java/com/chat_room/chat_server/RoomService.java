package com.chat_room.chat_server;

import java.util.HashMap;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Service
public class RoomService {
   
    public HashMap<String, RoomGuy> guysMap = new HashMap<>();
    class RoomGuy {
        @JsonProperty("name")
        public String name;
        public String userId;
        public RoomGuy(String name, String userId) {
            this.name = name;
            this.userId = userId;
        }
    }
    public boolean AddUser(String name, String sessionId, String userId){
        if(guysMap.containsKey(sessionId))
            return false;
        
        guysMap.put(sessionId, new RoomGuy(name, userId));
        return true;
    }
    public boolean RemoveUser(String sessionId){
        if(!guysMap.containsKey(sessionId))
            return false;
        
        guysMap.remove(sessionId);
        return true;
    }
    public String GetUserName(String sessionId){
        if(!guysMap.containsKey(sessionId))
            return "";
        
        return guysMap.get(sessionId).name;
    }
}
