import { ChakraProvider } from '@chakra-ui/react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import Login from './page/login/Login';
import Room from './page/room/Room';

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
    </ChakraProvider>
  );
}

export default App;
