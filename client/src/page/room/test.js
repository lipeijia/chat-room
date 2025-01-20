// src/page/room/stmp.js
import { useEffect } from 'react';
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const Test = () => {
  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/stomp');
    const stompClient = Stomp.over(socket);

    stompClient.connect(
      { userId: "testUser123" }, // Send userId on connect
      (frame) => {
        console.log('Connected: ' + frame);
        stompClient.send("/app/test", {}, "Hello World"); // Send a message after connecting
      },
      (error) => {
        console.error('Error connecting: ', error);
      }
    );

    return () => {
      stompClient.disconnect(() => {
        console.log('Disconnected');
      });
    };
  }, []);

  return (
    <div>
      <h1>WebSocket Client</h1>
      <p>WebSocket connection is active.</p>
    </div>
  );
};

export default Test;
