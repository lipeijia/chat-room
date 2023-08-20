import { useState } from 'react';
import { useNavigate } from 'react-router';
import { Button, Center, Input, VStack } from '@chakra-ui/react';

function Login() {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const handleName = (e) => {
    let str = e.target.value;
    if (!!str.trim()) {
      setName(str);
    } else {
      setName('');
    }
  };

  const handleLogin = () => {
    navigate(`/ws?name=${name}`);
  };

  return (
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
          onClick={handleLogin}
          isDisabled={!name.length}
        >
          進入聊天室
        </Button>
      </VStack>
    </Center>
  );
}

export default Login;
