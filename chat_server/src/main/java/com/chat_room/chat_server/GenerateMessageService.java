package com.chat_room.chat_server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import java.time.Duration;
import reactor.util.retry.Retry;

@Service
public class GenerateMessageService {
    private final RestTemplate restTemplate;
    private final WebClient webClient;
    @Value("${chatbot}")
    private String chatbot;
    public GenerateMessageService(RestTemplate restTemplate, WebClient webClient) {
        this.webClient = webClient;
        this.restTemplate = restTemplate;
    }

    public String generateText(String prompt, String userId, String sender) {

        String apiUrl = String.format("http://%s:8000/generate", chatbot);


        // 構造 JSON 請求資料
        JSONObject request = new JSONObject();
        request.put("userId", userId);
        request.put("prompt", prompt);
        request.put("sender", sender);
        
       
            try {
                // 發送 POST 請求並取得結果
                String responseBody =  webClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request.toString()) // 傳送 JSON 字串
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ClientResponse::createException)
                    .bodyToMono(String.class)// 將回傳 body 當作字串
                    .timeout(Duration.ofSeconds(2)) // 額外保險 timeout（非必要）
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500))) // 最多重試 2 次，間隔 500ms
                    .map(body -> new JSONObject(body).getString("response"))
                    .onErrorReturn("AI暫時無回應，請稍後再試。") // fallback 機制
                    .block(); // 阻塞取得結果（同步）
             
               

                JSONObject jsonResponse = new JSONObject(responseBody);
                return jsonResponse.getString("response");
            } 
        
            catch (Exception e) {
                // ➕ 可以在這裡記錄死信／存到資料庫／推到佇列
                // logDeadMessage(userId, prompt, sender, e);
                return "發生錯誤，請聯絡客服。";
            }
       
    }
}
//舊方法保留
// String apiUrl = String.format("http://%s:8000/generate", chatbot);
// // 構造 JSON 請求
// JSONObject request = new JSONObject();

// request.put("userId", userId);
// request.put("prompt", prompt);
// request.put("sender", sender);

// // 設置 HTTP 請求 Headers
// HttpHeaders headers = new HttpHeaders();
// headers.setContentType(MediaType.APPLICATION_JSON);
// HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);

// // 發送 POST 請求
// ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

// // 解析回應
// if (response.getStatusCode() == HttpStatus.OK) {
    
//     JSONObject jsonResponse = new JSONObject(response.getBody());
//     return jsonResponse.getString("response");
// } else {
//     return "Error: " + response.getStatusCode();
// }