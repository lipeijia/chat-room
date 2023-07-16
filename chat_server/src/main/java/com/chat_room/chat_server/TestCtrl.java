package com.chat_room.chat_server;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(value = "http://localhost:3000")
public class TestCtrl {
    @GetMapping("/hello")
    public String Hello(){
        return "hello";
    }
}
