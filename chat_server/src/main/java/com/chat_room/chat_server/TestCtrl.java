package com.chat_room.chat_server;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(value = "http://localhost:3000/")
public class TestCtrl {
    @GetMapping("/hello")
    public String Hello(){
        return "hello";
    }
    // @MessageMapping("/send") // 客戶端發送訊息的映射
    // @SendTo("/topic/messages") // 推送到指定的訂閱位置
    // public String sendMessage(String message) {
    //     return "Server received: " + message;
    // }
}
