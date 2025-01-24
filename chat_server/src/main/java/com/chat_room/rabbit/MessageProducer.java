package com.chat_room.rabbit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {
     private final RabbitTemplate rabbitTemplate;

    @Autowired
    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // public void sendMessage(String message) {
    //     // 使用 RabbitTemplate 发送消息到交换机
    //     rabbitTemplate.convertAndSend(
    //             RabbitMQConfig.EXCHANGE_NAME, 
    //             RabbitMQConfig.ROUTING_KEY, 
    //             message
    //     );
    //     System.out.println("消息已发送到 RabbitMQ: " + message);
    // }
    public void sendBroadCastMessage(String message, String roomId) {
        // 广播消息发送到公共交换机
        rabbitTemplate.convertAndSend("chatExchange", String.format("room.%s", roomId), message);
        System.out.println("广播消息发已发送: " + message);
    }
    public void sendPrivateMessage(String target, String message) {
        rabbitTemplate.convertAndSend("chatExchange", target, message);
        System.out.println("单人消息已发送: " + message);
    }
}
