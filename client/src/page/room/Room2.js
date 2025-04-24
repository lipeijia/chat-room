import { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router';
import config from '../../config/config'; // 路徑依實際專案結構調整
import useStompClient from '../../hooks/useStompClient';
import {
  Box,
  Text,
  Input,
  InputGroup,
  InputRightElement,
  HStack,
  Drawer,
  DrawerOverlay,
  DrawerContent,
  DrawerHeader,
  DrawerBody,
  Button,
  useBreakpointValue,
  Container,
} from '@chakra-ui/react';
import FancyButton from '../../components/buttons';
import BlobAvatar from './avatar';
function Room() {
  const userIdRef = useRef(generateRandomString());
  const userId = userIdRef.current;
  const [getConfig] = useState(config);
  const data = useRef({});
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [text, setText] = useState('');
  const [talkTo, setTalkTo] = useState('');
  const [renderTrigger, setRenderTrigger] = useState(0); // 用於觸發重新渲染
  const [messageHistory, setMessageHistory] = useState([]);
  const ref = useRef();
  const navigate = useNavigate();
  const location = useLocation();
  const isMobile = useBreakpointValue({ base: true, md: false });
  const params = new URLSearchParams(location.search);
  const name = params.get('name');
  const roomId = params.get('roomId');
  const roomName = params.get('roomName');
  const subsRef = useRef([]);
  function generateRandomString() {
    const characters =
      'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let result = '';
    for (let i = 0; i < 10; i++) {
      const randomIndex = Math.floor(Math.random() * characters.length);
      result += characters[randomIndex];
    }
    return result;
  }
    // 訂閱主題以接收訊息
    const handleMessage =(message) => {
      let d = JSON.parse(message.body);
      setMessageHistory((prev) => [
        ...prev,
        data.current[d.userId].name + '對大家說' + d.message,
      ]);
    }
    const handlePrivateMessage = (message) => {
      let d = JSON.parse(message.body);
      setMessageHistory((prev) => [
        ...prev,
        data.current[d.userId].name + '對我說' + d.message,
      ]);
    }
    const handleDisconnect = (message) => {
      let msg = data.current[message.body].name + '已離開房間';
      setMessageHistory((prev) => [...prev, msg]);
      delete data.current[message.body];
      setRenderTrigger((prev) => prev + 1);
    }
    const queueNewUser = (message) => {
      const resdata = JSON.parse(message.body);
      data.current = resdata;
      setRenderTrigger((prev) => prev + 1);
      setMessageHistory((prev) => [
        ...prev,
        '歡迎' + data.current[userId].name + '加入房間',
      ]);
    }
    const topicNewUser = (message) => {
      const resdata = JSON.parse(message.body);
      data.current = Object.assign({}, data.current, resdata);
      setMessageHistory((prev) => [
        ...prev,
        '歡迎' + Object.values(resdata)[0].name + '加入房間',
      ]);
    }
    const handlers = {
      [`/topic/room.${roomId}/message`]: handleMessage,
      [`/topic/room.${roomId}/disconnect`]: handleDisconnect,
      '/user/queue/newUser': queueNewUser,
      [`/topic/${roomId}/newUser`]: topicNewUser,
      '/user/queue/private': handlePrivateMessage,
    };
    
  
    
  const { subscribe, publish } = useStompClient({
    
    url: `http://localhost:${getConfig.API_BASE_PORT}/stomp?userId=${userId}`,
    onDisconnect: () => {
      console.log('已從 STOMP 斷開連接');
      navigate('/');
    },
    onStompError: (frame) => {
      console.error('STOMP 錯誤: ' + frame.headers['message']);
      console.error('詳細內容: ' + frame.body);
    },
    onConnect: (client) => {
      console.log('已連接 STOMP');
      // Object.values(subRef.current).forEach(s => s?.unsubscribe());
      subsRef.current.forEach(sub => sub.unsubscribe());
      subsRef.current = [];
      subsRef.current = Object.entries(handlers).map(([topic, handler]) =>
        subscribe(topic, handler)
      );
      // 發送加入聊天室訊息
      publish( '/app/chat.join', // 根據你的服務器端點進行調整
        { name: name, roomId: roomId });
    }
   
  });
  // useEffect(() => {


  //   // 添加 beforeunload 事件監聽器
  //   const handleBeforeUnload = () => {
  //     // 如有需要，可在此處進行離線通知或連線釋放
  //   };

  //   window.addEventListener('beforeunload', handleBeforeUnload);

  //   return () => {
  //     window.removeEventListener('beforeunload', handleBeforeUnload);
  //   };
  // }, [name]);

  const handleChange = (e) => {
    setText(e.target.value);
  };
  const handExit = (e)=>{

    navigate('/');
  };


  return (
    <Container maxW="container.xl" p={4}>
      <Box display="flex" justifyContent="space-between" alignItems="center" p={4} background="purple.200">
        <Text fontSize="xl" fontWeight="bold">{roomName}</Text>
        <HStack spacing={4}>
          <Button colorScheme="blue">刷新</Button>
          <Button colorScheme="red"    onClick={handExit}>退出</Button>
        </HStack>
      </Box>
      <Box display="flex" flexDirection="row" height="100vh">
        {/* 左側：聊天內容區退出域 */}
        <Box
          flex="1"
          display="flex"
          flexDirection="column"
          borderRight={{ base: 'none', md: '1px solid gray' }}
        >
          <Box flex="1" overflowY="auto" p={4}>
            <Text fontSize="2xl" mb={4}>
              聊天內容
            </Text>
            {messageHistory?.map((m, idx) => (
              <Box key={idx} mb={2}>
                <Text>{m}</Text>
              </Box>
            ))}
          </Box>
          <Box
            p={4}
            background="purple.100"
            borderTop="1px solid gray"
            position="sticky"
            bottom={0}
          >
            {/* <Box w="100%" maxW="1920px" mx="auto">  */}
            <Box display="grid" gridTemplateColumns="1fr auto" mb={2}>
              <Text>
                對 {talkTo === '' ? '大家' : data.current[talkTo].name} 說：
              </Text>
              {/* 右側按鈕，與輸入框的按鈕對齊 */}
          
              <FancyButton
                delegate={() => {
                  setTalkTo('');
                }}
                text="清空對象"
                height="2rem"
                minWidth="5rem"
              />
             
            </Box>

            <InputGroup>
              <Input
                name="text"
                value={text}
                background="#fff"
                onChange={handleChange}
                placeholder="輸入訊息..."
              />
              <InputRightElement
                width="5rem"
                display="flex"
                alignItems="center"
                justifyContent="flex-end"
              >
                <FancyButton
                  delegate={() => {
                    let _destination;
                    let _body;
                    if (talkTo === "") {
                      _destination = `/app/chat.send/room.${roomId}`;
                      _body = text;
                    } else {
                      _destination = "/app/chat.send.private";
                      _body = JSON.stringify({ text: text, sender: talkTo });
                      setMessageHistory((prev) => [
                        ...prev,
                        name + `對${data.current[talkTo].name}說：` + text,
                      ]);
                    }

                    // 假設 client.current.publish 是有效的發送方法
                    publish( _destination, _body);

                    // 清空輸入框
                    setText("");
                  }}
                  text="送出"
                  height="2rem"
                  minWidth="5rem"
                />
              </InputRightElement>
            </InputGroup>
          </Box>
        </Box>

        {/* 側邊欄按鈕 (僅在手機上顯示) */}
        {isMobile && (
          <Button
            position="absolute"
            top="1rem"
            left="1rem"
            onClick={() => setIsDrawerOpen(true)}
            zIndex="1000"
            colorScheme="purple"
          >
            人員列表
          </Button>
        )}

        {/* 右側：聊天室名稱與人員列表（Drawer） */}
        <Drawer
          isOpen={isDrawerOpen}
          placement="left"
          onClose={() => setIsDrawerOpen(false)}
        >
          <DrawerOverlay />
          <DrawerContent>
            <DrawerHeader borderBottomWidth="1px">人員列表</DrawerHeader>
            <DrawerBody>
              {Object.entries(data.current).map(([index, person]) => (
                <HStack
                  key={index}
                  borderTop="1px solid gray"
                  p={2}
                  _hover={{ cursor: 'pointer', bg: 'gray.100' }}
                  onClick={() => {
                    setTalkTo(index);
                    setIsDrawerOpen(false);
                    console.log(`與 ${person.name} 開始聊天`);
                  }}
                  justifyContent="flex-start"
                >
                  <BlobAvatar person={person} />
                  <Text fontSize="lg">{person.name}</Text>
                </HStack>
              ))}
            </DrawerBody>
          </DrawerContent>
        </Drawer>

        {/* 右側：聊天室名稱與人員列表（桌面版顯示） */}
        {!isMobile && (
          <Box width="300px" display="flex" flexDirection="column" p={4}>
            
            <Box
              flex="1"
              width="300px"
              overflowY="scroll"
              paddingRight="23px"
              ref={ref}
            >
              {Object.entries(data.current).map(([index, person]) => (
                <HStack
                  key={index}
                  borderTop="1px solid gray"
                  p={2}
                  _hover={{ cursor: 'pointer', bg: 'gray.100' }}
                  onClick={() => {
                    if (name !== person.name) {
                      setTalkTo(index);
                      console.log(`與 ${person.name} 開始聊天`);
                    }
                  }}
                  justifyContent="flex-start"
                >
                  <BlobAvatar person={person} />
                  <Text fontSize="lg">{person.name}</Text>
                </HStack>
              ))}
            </Box>
          </Box>
        )}
      </Box>
    </Container>
  );
}

export default Room;
