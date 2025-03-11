package com.chat_room.chat_server;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import org.json.JSONObject;
@Service
public class GenerateMessageService {
    private final RestTemplate restTemplate;

    public GenerateMessageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateText(String prompt, String userId, String sender) {
        String apiUrl = "http://localhost:8000/generate"; // Python FastAPI URL

        // 構造 JSON 請求
        JSONObject request = new JSONObject();
        
        request.put("userId", userId);
        request.put("prompt", prompt);
        request.put("sender", sender);
        
        // 設置 HTTP 請求 Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);
        
        // 發送 POST 請求
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
        
        // 解析回應
        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            return jsonResponse.getString("response");
        } else {
            return "Error: " + response.getStatusCode();
        }
    }
}