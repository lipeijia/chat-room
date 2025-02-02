package com.chat_room.rabbit;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.chat_room.chat_server.RoomService;
import com.chat_room.chat_server.RoomService.RoomGuy;
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.rabbitmq.client.AMQP.Channel;
import com.rabbitmq.client.Channel;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
@Component
public class ChatRabbitListener {
    //   private final SimpMessagingTemplate messagingTemplate;
    private final MessageProducer messageProducer;
    private final RoomService roomService;
    public ChatRabbitListener(MessageProducer messageProducer, RoomService roomService) {
        this.messageProducer = messageProducer;
        this.roomService = roomService;
        int k = 1;
        // this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = "#{broadcastQueue.name}")
    public void handleMessage(Message message, Channel channel) {
        // 获取消息内容
        String body = new String(message.getBody());
        System.out.println("收到消息：" + body);
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        System.out.println("消息的路由键：" + routingKey);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> map = objectMapper.readValue(body, Map.class);
            messageProducer.RabbitBroadCast(map, routingKey);
            System.out.println(map); // {message=pppp, userId=7F4u7IGMb5}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @RabbitListener(queues = "#{joinQueue.name}")
    public void handleJoin(Map<String, Map<String, String>>  message, Channel channel, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey
                              ) {
        // 获取消息内容
        // String body = new String(message.getBody());
        // System.out.println("收到消息：" + body);
        // String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        // String routingKey = "";
        System.out.println("消息的路由键：" + routingKey);
           // 定義正則表達式，其中 (.*?) 為捕獲組，用來取得中間的內容
        String regex = "room\\.(.*?)\\.join";
        
        // 編譯正則表達式
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(routingKey);
      
        // 如果找到匹配，則取得第一個捕獲組
        if (matcher.find()) {
            routingKey = matcher.group(1);
          
        } else {
            System.out.println("沒有匹配到");
        }
        this.messageProducer.RabbitJoin(message, routingKey);
        // var v = this.simpUserRegistry.getUser(principal.getName());
        var data = this.roomService.roomsMap.get(routingKey).getMembers().values();
        Map<String, Map<String, String>> transformedMap = new HashMap<>();
        for (RoomGuy roomGuy : data) {
            Map<String, String>  value = new HashMap<>();
            value.put("name", roomGuy.name);
            transformedMap.put(roomGuy.userId, value);
        }
        var test = (String)message.keySet().toArray()[0];
        this.messageProducer.RabbitJoinSelf(transformedMap, (String)message.keySet().toArray()[0]);
        // 
        // this.simpMessagingTemplate.convertAndSendToUser(
            //         principal.getName(),"/queue/newUser", json);
        // // 转换为 JSON
        // ObjectMapper objectMapper = new ObjectMapper();
        // try {
        //     String json = objectMapper.writeValueAsString(transformedMap);
        //     this.simpMessagingTemplate.convertAndSendToUser(
        //         principal.getName(),"/queue/newUser", json);
        // } catch (Exception e) {
        //     // TODO: handle exception
        // }
        // try {
        //     ObjectMapper objectMapper = new ObjectMapper();
        //     Map<String, String> map = objectMapper.readValue(body, Map.class);
        //     messageProducer.RabbitBroadCast(map, routingKey);
        //     System.out.println(map); // {message=pppp, userId=7F4u7IGMb5}
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        // 获取路由键
      
     
   
    }
}
