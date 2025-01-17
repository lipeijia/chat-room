import React, { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const WebSocketClient = () => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");

  useEffect(() => {
    // 初始化 STOMP 客戶端
    const socket = new SockJS("http://localhost:8080/ws");
    const client = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        console.log("Connected");
        client.subscribe("/topic/messages", (message) => {
          setMessages((prevMessages) => [...prevMessages, message.body]);
        });
      },
      onStompError: (error) => {
        console.error("STOMP Error:", error);
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);

  const sendMessage = () => {
    const socket = new SockJS("http://localhost:8080/ws");
    const client = new Client({
      webSocketFactory: () => socket,
    });

    client.onConnect = () => {
      client.publish({
        destination: "/app/send",
        body: input,
      });
    };

    client.activate();
  };

  return (
    <div>
      <h1>WebSocket with STOMP</h1>
      <div>
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
        />
        <button onClick={sendMessage}>Send Message</button>
      </div>
      <div>
        <h2>Messages:</h2>
        <ul>
          {messages.map((msg, index) => (
            <li key={index}>{msg}</li>
          ))}
        </ul>
      </div>
    </div>
  );
};

export default WebSocketClient;
