import { useState } from 'react';
import { useNavigate } from 'react-router';
import { Button, Center, Input, VStack, SimpleGrid, Box, Text} from '@chakra-ui/react';

function Login() {
  const navigate = useNavigate();


  // const [name, setName] = useState('');
  // const handleName = (e) => {
  //   let str = e.target.value;
  //   if (!!str.trim()) {
  //     setName(str);
  //   } else {
  //     setName('');
  //   }
  // };

  // const handleLogin = () => {
  //   navigate(`/ws?name=${name}`);
  // };

  // return (
  //   <Center
  //     bgGradient={[
  //       'linear(to-tr, purple.200, pink.200)',
  //       'linear(to-t, blue.200, teal.500)',
  //       'linear(to-b, orange.100, purple.300)'
  //     ]}
  //     h='100vh'
  //     w='100vw'
  //     color='white'
  //   >
  //     <VStack gap={10} w='90vw'>
  //       <Input
  //         name='name'
  //         placeholder='請輸入暱稱'
  //         size='lg'
  //         width='100%'
  //         background='rgba(256,256,256, 0.3)'
  //         color='purple'
  //         onChange={handleName}
  //       />
  //       <Button
  //         colorScheme='purple'
  //         size='lg'
  //         type='button'
  //         onClick={handleLogin}
  //         isDisabled={!name.length}
  //       >
  //         進入聊天室
  //       </Button>
  //     </VStack>
  //   </Center>
  // );
  const [name, setName] = useState("");
  const [selectedRoomId, setSelectedRoomId] = useState(-1);
  const [selectedRoomName, setSelectedRoomName] = useState(null);

  const chatRooms = ["喵喵基地 🎪🐱", "塔樓秘境 🔮🗝"];

  const handleName = (e) => setName(e.target.value);
  const handleRoomSelect = (roomId, roomName) => {
    setSelectedRoomId(roomId);
    setSelectedRoomName(roomName);
  };

  const handleLogin = () => navigate(`/ws?name=${name}&roomId=${selectedRoomId}&roomName=${selectedRoomName}`);
  
  return (
    <Center
      // bgGradient={[
      //   "linear(to-tr, purple.200, pink.200)",
      //   "linear(to-t, blue.200, teal.500)",
      //   "linear(to-b, orange.100, purple.300)",
      // ]}
      h="100vh"
      w="100vw"
      // color="white"
    >
      {selectedRoomId ==-1 ? (
        // 選擇聊天室
        <VStack spacing={6} w="80vw">
          <Text fontSize="2xl" fontWeight="bold">選擇一個聊天室</Text>
          <SimpleGrid columns={2} spacing={5} w="60%">
            {chatRooms.map((room, index) => (
              <Box
                key={index}
                h="150px"
                w="150px"
                bg="rgba(255, 255, 255, 0.3)"
                display="flex"
                alignItems="center"
                justifyContent="center"
                borderRadius="lg"
                fontSize="xl"
                fontWeight="bold"
                cursor="pointer"
                _hover={{ bg: "rgba(255, 255, 255, 0.5)" }}
                onClick={() => handleRoomSelect(index, room)}
              >
                {room}
              </Box>
            ))}
          </SimpleGrid>
        </VStack>
      ) : ( 
        // 輸入暱稱並進入聊天室
        <VStack spacing={6} w="90vw">
          <Text fontSize="2xl" fontWeight="bold">進入 {selectedRoomName} 聊天室</Text>
          <Input
            name="name"
            placeholder="請輸入暱稱"
            size="lg"
            width="60%"
            background="rgba(256,256,256, 0.3)"
            color="purple"
            onChange={handleName}
          />
          <Button
            colorScheme="purple"
            size="lg"
            type="button"
            onClick={handleLogin}
            isDisabled={!name.length}
          >
            進入聊天室
          </Button>
        </VStack>
      )}
    </Center>
  );
}

export default Login;
