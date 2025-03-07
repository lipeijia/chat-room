import { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router';
import { useParams } from 'react-router-dom';
import config from '../../config/config'; // 路徑依實際專案結構調整
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
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
function Room() {

  const [getConfig] = useState(config);
  const data = useRef([]);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [text, setText] = useState('');
  const [talkTo, setTalkTo] = useState('');
  const [renderTrigger, setRenderTrigger] = useState(0); // 用於觸發重新渲染
  const [selfIdx, setSelfIdx] = useState(-1);
  const [messageHistory, setMessageHistory] = useState([]);
  const client = useRef(null); // 使用 useRef 保存 Stomp 客戶端實例
  const ref = useRef();
  const navigate = useNavigate();
  const location = useLocation();

  const isMobile = useBreakpointValue({ base: true, md: false });
  const params = new URLSearchParams(location.search);
  const name = params.get('name');
  const roomId = params.get('roomId');
  const roomName = params.get('roomName');

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

  useEffect(() => {
    if (!name || client.current) return;

    const userId = generateRandomString();
    // 初始化基於 SockJS 的 Stomp 客戶端
    client.current = new Client({
      // 替換為你的 WebSocket 端點，例如 config.API_BASE_URL
      webSocketFactory: () =>
        new SockJS(
          `http://localhost:${getConfig.API_BASE_PORT}/stomp?userId=${userId}`
        ),
      reconnectDelay: 5000, // 自動重連間隔（5 秒）
      debug: (str) => {
        console.log(str);
      },
      onConnect: () => {
        console.log('已連接到 STOMP');

        // 訂閱主題以接收訊息
        client.current.subscribe(
          `/topic/room.${roomId}/message`,
          (message) => {
            let d = JSON.parse(message.body);
            setMessageHistory((prev) => [
              ...prev,
              data.current[d.userId].name + '對大家說' + d.message,
            ]);
          }
        );

        client.current.subscribe(
          `/topic/room.${roomId}/disconnect`,
          (message) => {
            let msg = data.current[message.body].name + '已離開房間';
            setMessageHistory((prev) => [...prev, msg]);
            delete data.current[message.body];
            setRenderTrigger((prev) => prev + 1);
          }
        );

        client.current.subscribe('/user/queue/newUser', (message) => {
          client.current.subscribe(`/topic/${roomId}/newUser`, (message) => {
            const resdata = JSON.parse(message.body);
            data.current = Object.assign({}, data.current, resdata);
            setMessageHistory((prev) => [
              ...prev,
              '歡迎' + Object.values(resdata)[0].name + '加入房間',
            ]);
          });
          const resdata = JSON.parse(message.body);
          data.current = resdata;
          // setSelfIdx(resdata.id);
          setRenderTrigger((prev) => prev + 1);
          setMessageHistory((prev) => [
            ...prev,
            '歡迎' + data.current[userId].name + '加入房間',
          ]);
        });

        client.current.subscribe('/user/queue/private', (message) => {
          let d = JSON.parse(message.body);
          setMessageHistory((prev) => [
            ...prev,
            data.current[d.userId].name + '對我說' + d.message,
          ]);
        });

        // 發送加入聊天室訊息
        client.current.publish({
          destination: '/app/chat.join', // 根據你的服務器端點進行調整
          body: JSON.stringify({ name: name, roomId: roomId }),
        });
      },
      onDisconnect: () => {
        console.log('已從 STOMP 斷開連接');
        navigate('/');
      },
      onStompError: (frame) => {
        console.error('代理報告錯誤: ' + frame.headers['message']);
        console.error('詳細信息: ' + frame.body);
      },
    });

    // 添加 beforeunload 事件監聽器
    const handleBeforeUnload = () => {
      // 如有需要，可在此處進行離線通知或連線釋放
    };

    window.addEventListener('beforeunload', handleBeforeUnload);

    client.current.activate();

    return () => {
      if (client.current) {
        client.current.deactivate();
      }
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [name, getConfig.API_BASE_PORT, navigate]);

  const handleChange = (e) => {
    setText(e.target.value);
  };
  const handExit = (e)=>{

    navigate('/');
  };

  const handleTalk = (val) => {
    if (client.current && client.current.connected) {
      const message = {
        kind: 2, // 假設 2 表示發送消息
        data: val,
        senderIdx: selfIdx,
        receiverIdx: talkTo,
      };
      client.current.publish({
        destination: '/app/chat.send', // 根據你的服務器端點進行調整
        body: JSON.stringify(message),
      });
      setText('');
    }
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
                    client.current.publish({
                      body: _body,
                      destination: _destination,
                    });

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
