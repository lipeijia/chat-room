package com.chat_room.rabbit;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQConfig {
     public static final String EXCHANGE_NAME = "chatExchange";
    public static final String BROADCAST_QUEUE = "broadcastQueue";
    public static final String PRIVATE_QUEUE_PREFIX = "privateQueue_";

    // public static final String QUEUE_NAME = "testQueue";
    // public static final String EXCHANGE_NAME = "testExchange";
    // public static final String ROUTING_KEY = "testKey";
    // 定义交换机
    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    // 定义广播队列
    @Bean
    public Queue broadcastQueue() {
        return new AnonymousQueue();
        // return new Queue(BROADCAST_QUEUE, true);
    }

    // 广播队列绑定到交换机
    @Bean
    public Binding broadcastBinding(Queue userQueue, DirectExchange chatExchange) {
        return BindingBuilder.bind(userQueue).to(chatExchange).with("room.*");
    }

    // 动态创建私有队列
    public Queue privateQueue(String userId) {
        return new Queue(PRIVATE_QUEUE_PREFIX + userId, true);
    }
}
