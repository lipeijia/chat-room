// package com.chat_room.chat_server;
// import java.io.UnsupportedEncodingException;
// import java.security.Principal;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.UUID;

// import org.springframework.context.event.EventListener;
// import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
// import org.springframework.messaging.support.GenericMessage;
// import org.springframework.stereotype.Component;
// import org.springframework.web.socket.messaging.SessionConnectEvent;
// import org.springframework.web.socket.messaging.SessionConnectedEvent;
// import org.springframework.web.socket.messaging.SessionDisconnectEvent;


// @Component
// public class WebSocketLinster {
//     private final RoomService roomService;
//     public WebSocketLinster(RoomService roomService){
//         this.roomService = roomService;
//     }

//     @EventListener
//     public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
//         String sessionId = event.getSessionId();
//         System.out.println("用戶斷線，Session ID: " + sessionId);
        
//         // 如果需要其他屬性，可以從事件中獲取
//         // 如需獲取用戶名稱等，需在連接時將其存儲在 Session 屬性中
//     }
//     // @EventListener
//     // public void handleWebSocketConnectListener(SessionConnectEvent event) {
//     //     StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//     //     // headerAccessor.setLeaveMutable(true);
//     //     // 取得 WebSocket Session ID
     
//     //     // 從 Headers 中取得 userId
//     //     // String userId = headerAccessor.getFirstNativeHeader("userId");
//     //     var u = headerAccessor.getUser();
//     //     String sessionId = headerAccessor.getSessionId();

//     // }
//     // @EventListener
//     // public void handleSessionConnected(SessionConnectedEvent event) {
//     //     StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//     //     Principal user = headerAccessor.getUser();
//     //     String sessionId = headerAccessor.getSessionId();

//     //     if (user != null) {
//     //         System.out.println("SessionConnectedEvent - user: " + user.getName() + ", sessionId: " + sessionId);
//     //     } else {
//     //         System.out.println("SessionConnectedEvent - user is null, sessionId: " + sessionId);
//     //     }
//     // }
// }


//    // headerAccessor.setLeaveMutable(true);
//         // 取得 WebSocket Session ID
     
//         // 從 Headers 中取得 userId
//         // String userId = headerAccessor.getFirstNativeHeader("userId");
//         // StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
//         // headerAccessor.setLeaveMutable(true);
//         // // 取得 WebSocket Session ID
//         // String sessionId = headerAccessor.getSessionId();
//         // // 從 Headers 中取得 userId
//         // String userId = headerAccessor.getFirstNativeHeader("userId");

//         // if (userId != null) {
//         //     // 設定 Principal 為 userId
//         //     headerAccessor.setUser(new Principal() {
//         //         @Override
//         //         public String getName() {
//         //             return userId;
//         //         }
//         //     });
//         //     System.out.println("Principal set to userId: " + userId);
//         // } else {
//         //     // 生成一個隨機的 userId，並設定 Principal
//         //     String generatedUserId = UUID.randomUUID().toString();
//         //     headerAccessor.setUser(new Principal() {
//         //         @Override
//         //         public String getName() {
//         //             return generatedUserId;
//         //         }
//         //     });
//         //     System.out.println("userId is null, generated userId: " + generatedUserId);
//         // }

//         // // 將 userId 存入 Session Attributes
//         // // headerAccessor.getSessionAttributes().put("userId", headerAccessor.getUser().getName());

//         // System.out.println("WebSocket 連線建立，Session ID: " + sessionId + ", userId: " + headerAccessor.getUser().getName());