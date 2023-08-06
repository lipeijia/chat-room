import { useState } from 'react';
import { ChakraProvider } from '@chakra-ui/react';
import config from './config/config';
import {
  Button,
  Center,
  Input,
  VStack,
  Avatar,
  HStack,
  Text,
  Box,
  InputGroup,
  InputRightElement
} from '@chakra-ui/react';

// TODO: websocket 元件
function App() {
  const [name, setName] = useState('');
  const [kind, setKind] = useState(0);
  const [data, setData] = useState(null);
  const [text, setText] = useState('安安!');

  const url = `${config.SERVER_POINT}`;

  const handleName = (e) => {
    let str = e.target.value;
    if (!!str.trim()) {
      setName(str);
    } else {
      setName('');
    }
  };

  const handleSubmit = () => {
    const socket = new WebSocket(`${url}ws?name=${name}`);
    socket.onmessage = (res) => {
      const data = JSON.parse(res.data);
      setKind(data.kind);
      const parseData = JSON.parse(data.data);
      setData(parseData);
    };
  };

  const handleTalk = () => {
    console.log(text);
    const req = JSON.stringify({ text, ids: '', kind: 0 });
    // const socket = new WebSocket(`${url}ws?name=${name}`);
    alert(req);
  };

  return (
    <ChakraProvider>
      {!data ? (
        <Center
          bgGradient={[
            'linear(to-tr, purple.200, pink.200)',
            'linear(to-t, blue.200, teal.500)',
            'linear(to-b, orange.100, purple.300)'
          ]}
          h='100vh'
          w='100vw'
          color='white'
        >
          <VStack gap={10} w='90vw'>
            <Input
              name='name'
              placeholder='請輸入暱稱'
              size='lg'
              width='100%'
              background='rgba(256,256,256, 0.3)'
              color='purple'
              onChange={handleName}
            />
            <Button
              colorScheme='purple'
              size='lg'
              type='button'
              onClick={handleSubmit}
              isDisabled={!name.length}
            >
              進入聊天室
            </Button>
          </VStack>
        </Center>
      ) : (
        <>
          <Box px={3} py={3} position='relative'>
            <Text fontSize='2xl'>匿名聊天室</Text>
          </Box>
          <Box mx={2}>
            {data?.names?.map((person, index) => (
              <HStack
                key={`${index}-${person}`}
                borderTop='1px solid gray'
                p={2}
              >
                <Avatar name={person} />
                <Text fontSize='lg'>{person}</Text>
              </HStack>
            ))}
            <Box
              position='fixed'
              bottom={0}
              left={0}
              zIndex={9}
              w='100%'
              p={4}
              background='purple.100'
            >
              <Text mb={2}>對大家說：</Text>
              <InputGroup>
                <Input
                  defaultValue={text}
                  background='#fff'
                  name='text'
                  onChange={(e) => setText(e.target.value)}
                />
                <InputRightElement width='4.5rem'>
                  <Button
                    h='1.75rem'
                    size='sm'
                    onClick={handleTalk}
                    type='button'
                  >
                    送出
                  </Button>
                </InputRightElement>
              </InputGroup>
            </Box>
          </Box>
        </>
      )}
    </ChakraProvider>
  );
}

export default App;
