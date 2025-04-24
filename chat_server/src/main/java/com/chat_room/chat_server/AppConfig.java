package com.chat_room.chat_server;

import reactor.netty.http.client.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
@Configuration
public class AppConfig {
    // @Bean
    // public RestTemplate restTemplate() {
    //     HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
    //     factory.setConnectTimeout(2000); // 連線超時（毫秒）
    //     factory.setReadTimeout(2000);    // 讀取超時（毫秒）

    //     return new RestTemplate(factory);
    // }
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(2)) // 2 秒 timeout
                ))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}