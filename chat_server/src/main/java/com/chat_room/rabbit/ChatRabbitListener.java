package com.chat_room.rabbit;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.chat_room.chat_server.ImageGenerationService;
import com.chat_room.chat_server.RoomService;
import com.chat_room.chat_server.RoomService.RoomGuy;
import com.rabbitmq.client.Channel;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Base64;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

@Component
public class ChatRabbitListener {

    private final MessageProducer messageProducer;
    private final RoomService roomService;
    private final ImageGenerationService imageGenerationService;
    
    // 預編譯 join 路由鍵的正則表達式，用來提取房間標識
    private final Pattern joinRoutingKeyPattern = Pattern.compile("room\\.(.*?)\\.join");

    public ChatRabbitListener(MessageProducer messageProducer, RoomService roomService, ImageGenerationService imageGenerationService) {
        this.messageProducer = messageProducer;
        this.roomService = roomService;
        this.imageGenerationService = imageGenerationService;
    }

    /**
     * 處理廣播消息
     *
     * @param message    消息內容，格式為 Map<String, String>
     * @param channel    RabbitMQ 通道（可以用於手動ACK等操作）
     * @param routingKey 消息的路由鍵
     */
    @RabbitListener(queues = "#{broadcastQueue.name}")
    public void handleMessage(Map<String, String> message, Channel channel,
                              @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        messageProducer.RabbitBroadCast(message, routingKey);
    }

    /**
     * 處理用戶加入房間的消息
     *
     * @param message    消息內容，格式為 Map<String, Map<String, String>>
     * @param channel    RabbitMQ 通道
     * @param routingKey 路由鍵，例如 room.{roomId}.join
     */
    @RabbitListener(queues = "#{joinQueue.name}")
    public void handleJoin(Map<String, Map<String, String>> message, Channel channel,
                           @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        System.out.println("消息的路由鍵：" + routingKey);
        // 提取房間標識
        String roomKey = extractRoomKey(routingKey);
        if (roomKey == null) {
            System.out.println("沒有匹配到房間標識");
            return; // 若無法提取房間標識，則不再處理
        }
        RoomService.Room room = roomService.roomsMap.get(roomKey);
        try{
            var img = this.imageGenerationService.generateImage();
            String nameId = (String)message.keySet().toArray()[0];
            var guy = room.getMember(message.get(nameId).get("sessionId"));
            message.get(nameId).remove("sessionId");
            message.get(nameId).put("img", img);
            guy.image = img;

            var k = 1;
        }catch(Exception ex){

        }
        // 將 join 消息轉發給消息生產者
        messageProducer.RabbitJoin(message, roomKey);
        
        // 從房間服務中獲取當前房間成員資訊
       
        if (room == null) {
            System.out.println("找不到房間：" + roomKey);
            return;
        }
        Collection<RoomGuy> members = room.getMembers().values();
        Map<String, Map<String, String>> transformedMap = new HashMap<>();
        // 將 RoomGuy 資料轉換為 Map 格式，方便後續使用
        for (RoomGuy roomGuy : members) {
            Map<String, String> value = new HashMap<>();
            value.put("name", roomGuy.name);
            value.put("img", roomGuy.image);
            transformedMap.put(roomGuy.userId, value);
        }
        // 從消息中取得發送者的 ID（假設消息的 key 為發送者的 userId）
        String senderId = message.keySet().stream().findFirst().orElse("");
        messageProducer.RabbitJoinSelf(transformedMap, senderId);
    }
  
    /**
     * 處理私聊消息
     *
     * @param message 消息內容，格式為 Map<String, String>
     * @param channel RabbitMQ 通道
     * @param userId  從消息 header 中取得的用戶 ID
     */
    @RabbitListener(queues = "#{privateQueue.name}")
    public void handlePrivateMessage(Map<String, String> message, Channel channel,
                                     @Header("userId") String userId) {
        messageProducer.RabbitPrivateMessage(message, userId);
    }

    /**
     * 根據路由鍵提取房間標識。
     * 例如：對於路由鍵 "room.123.join"，返回 "123"。
     *
     * @param routingKey 路由鍵字符串
     * @return 提取的房間標識，如果無法匹配則返回 null
     */
    private String extractRoomKey(String routingKey) {
        Matcher matcher = joinRoutingKeyPattern.matcher(routingKey);
        return matcher.find() ? matcher.group(1) : null;
    }
}
