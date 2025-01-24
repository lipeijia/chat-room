package com.chat_room.rabbit;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class MessageController {
      private final MessageProducer messageProducer;

    @Autowired
    public MessageController(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    // @PostMapping("/send")
    // public String sendMessage(@RequestBody Map<String, String> payload) {
    //     String message = payload.get("message");
    //     messageProducer.sendBroadCastMessage(message);
    //     return "消息已发送: " + message;
    // }
}
