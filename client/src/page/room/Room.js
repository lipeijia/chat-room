import { useState, useMemo, useEffect } from 'react';
import { useLocation } from 'react-router';
import io from 'socket.io-client';
import {
  Button,
  Input,
  Avatar,
  HStack,
  Text,
  Box,
  InputGroup,
  InputRightElement
} from '@chakra-ui/react';

function Room() {
  const location = useLocation();
  const name = useMemo(
    () => new URLSearchParams(location.search).get('name'),
    [location.search]
  );

  const url = `http://localhost:3000/ws?name=${name}`;
  const socket = io.connect(url);

  useEffect(() => {
    socket.on('receive_message', (data) => {
      alert(data.message);
    });
  }, [socket]);

  //   const [kind, setKind] = useState(0);
  const [data] = useState(null);
  const [text, setText] = useState('');

  const handleChange = (e) => {
    setText(e.target.value);
  };

  const handleTalk = () => {
    console.log(text);
    socket.emit('send_message', { message: text });
    setText('');
  };
  return (
    <>
      <Box px={3} py={3} position='relative'>
        <Text fontSize='2xl'>匿名聊天室</Text>
      </Box>
      <Box mx={2}>
        {data?.names?.map((person, index) => (
          <HStack key={`${index}-${person}`} borderTop='1px solid gray' p={2}>
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
              name='text'
              value={text}
              background='#fff'
              onChange={handleChange}
            />
            <InputRightElement width='4.5rem'>
              <Button h='1.75rem' size='sm' onClick={handleTalk} type='button'>
                送出
              </Button>
            </InputRightElement>
          </InputGroup>
        </Box>
      </Box>
    </>
  );
}

export default Room;
