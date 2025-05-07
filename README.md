# AI 即時聊天室系統（前後端主架構）

# AI Real-Time Chatroom System (Frontend & Backend Architecture)

本專案為整合前端聊天室（React）與後端即時訊息處理（Spring Boot + WebSocket + RabbitMQ）的核心系統。支援 STOMP 即時聊天、私訊功能與聊天室人員同步，並整合 AI 模型（需另部署）以提供非同步回覆與自動頭像生成。  
This project integrates a React-based frontend chatroom with a Spring Boot backend for real-time messaging via WebSocket and RabbitMQ. It supports STOMP-based chat, private messaging, and room synchronization, and incorporates AI models (deployed separately) for async replies and auto-generated avatars.

---

## 環境需求

## Environment Requirements

- Node.js 18+ / npm
    
- JDK 17+
    

---

## 核心功能

## Core Features

- 群聊、私聊、加入 / 離開事件處理  
    Group chat, private messages, and join/leave event handling
    
- STOMP over WebSocket 即時推送  
    Real-time messaging using STOMP over WebSocket
    
- 訊息統一經由 RabbitMQ queue 解耦  
    Message flow is decoupled via a unified RabbitMQ queue
    
- 支援私聊觸發 AI 回覆（FastAPI 需另部署）  
    AI auto-reply can be triggered in private chat (FastAPI service required)
    
- 進房自動生成頭像（ONNX 模型內建後端）  
    Auto avatar generation upon room entry (ONNX model integrated in backend)
