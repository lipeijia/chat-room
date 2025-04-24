package com.chat_room.rabbit;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Component
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "chatExchange";
    public static final String BROADCAST_QUEUE = "broadcastQueue";
    public static final String PRIVATE_QUEUE_PREFIX = "privateQueue_";
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
     @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }

    // 定义交换机
    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // 定义广播队列
    @Bean
    public Queue broadcastQueue() {
        return new AnonymousQueue();
        // return new Queue(BROADCAST_QUEUE, true);
    }
    @Bean
    public Queue joinQueue() {
        return new AnonymousQueue();
    }
    @Bean
    public Queue privateQueue() {
        return new AnonymousQueue();
    }
    // 广播队列绑定到交换机
    @Bean
    public Binding broadcastBinding(Queue broadcastQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(broadcastQueue).to(chatExchange).with("room.*");
    }
    @Bean
    public Binding joinBinding(Queue joinQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(joinQueue).to(chatExchange).with("room.*.join");
    }
    @Bean
    public Binding privateBinding(Queue privateQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(privateQueue).to(chatExchange).with("private");
    }
}
