// import {  useState } from 'react';
// import axios from 'axios';
import config from './config/config';

function App() {
  const url = `${config.SERVER_POINT}`;
  let socket = new WebSocket(`${url}ws`);
  //   console.log(socket);
  socket.onopen = function (e) {
    socket.send('My name is John');
  };

  return <h1>{'test'}</h1>;
}

export default App;
