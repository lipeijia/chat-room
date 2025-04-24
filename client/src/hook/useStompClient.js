// src/hooks/useStompClient.js
import { useRef, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default function useStompClient({  url,
  onConnect,
  onStompError,
  onDisconnect}) {
  const clientRef = useRef(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(url),
      reconnectDelay: 5000,
      onConnect: () => onConnect(client),
      onStompError: onStompError,
      onDisconnect: onDisconnect,   // 綁上
    });
    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [url]);

  const subscribe = (destination, callback) =>
    clientRef.current?.subscribe(destination, callback);

  const publish = (destination, body) =>
    clientRef.current?.publish({
      destination,
      body: typeof body === 'string' ? body : JSON.stringify(body),
    });

  return { subscribe, publish };
}
