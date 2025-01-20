import { ChakraProvider } from '@chakra-ui/react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import Login from './page/login/Login';
// import Room from './page/room/Room';
import Room from './page/room/Room2';
import Test from './page/room/test';

import WebSocketClient from './page/room/stmp';
// TODO: websocket 元件
function App() {
  const router = createBrowserRouter([
    {
      path: '/',
      element: <Login />
    },
    { path: '/ws', element: <Room /> }
  ]);

  return (
    <ChakraProvider>
      <RouterProvider router={router} />
      {/* <Test/> */}
    </ChakraProvider>
  );
}

export default App;
