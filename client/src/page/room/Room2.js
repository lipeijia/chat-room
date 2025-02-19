import { useState, useMemo, useEffect, useRef } from 'react';
import { json, useLocation, useNavigate } from 'react-router';
import { useParams } from "react-router-dom";
import config from '../../config/config'; // 路徑依實際專案結構調整
import {
  Box,
  Text,
  Input,
  InputGroup,
  InputRightElement,
  HStack,
  Avatar,
  Drawer,
  DrawerOverlay,
  DrawerContent,
  DrawerHeader,
  DrawerBody,
  Button,
  useBreakpointValue,
  Container
} from '@chakra-ui/react';
import FancyButton from "../../components/buttons";
import BlobAvatar from './avatar';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
// const roomId = Math.floor(Math.random() * 2) + 1;
function Room() {
  const [getConfig, setConfig] = useState(config);
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
  // const name = useMemo(
  //   () => new URLSearchParams(location.search).get('name'),
  //   [location.search]
  // );
  const params = new URLSearchParams(location.search);  
  // const { room, name} = useParams();
  const name = params.get('name');
  const roomId = params.get('roomId');
  const roomName = params.get('roomName');
  function generateRandomString() {
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let result = '';
    for (let i = 0; i < 10; i++) {
      const randomIndex = Math.floor(Math.random() * characters.length);
      result += characters[randomIndex];
    }
    return result;
  }
  const [base64, setBase64] = useState(
    "data:image/png;base64,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"
  );
  useEffect(() => {
    if (!name || client.current) return;
    // console.log("Config:", config);
    const userId = generateRandomString();
    // 初始化基於 SockJS 的 Stomp 客戶端
    client.current = new Client({
      // 替換為你的 WebSocket 端點config.API_BASE_URL
      webSocketFactory: () => new SockJS(`http://localhost:${getConfig.API_BASE_PORT}/stomp?userId=${userId}`),
      reconnectDelay: 5000, // 自動重連間隔（5 秒）
      debug: (str) => {
        console.log(str);
      },
      onConnect: () => {
        console.log('已連接到 STOMP');

        // 訂閱主題以接收訊息
        client.current.subscribe(`/topic/room.${roomId}/message`, (message)=>{
          let d = JSON.parse(message.body);
          setMessageHistory((prev) => [...prev, data.current[d.userId].name + "對大家說" + d.message]);
        });
        client.current.subscribe(`/topic/room.${roomId}/disconnect`, (message)=>{
          var k = 1;
          let msg = data.current[message.body].name + "已離開房間";
          setMessageHistory((prev) => [...prev, msg]);
          delete data.current[message.body];
          setRenderTrigger((prev) => prev + 1);
   
          // setMessageHistory((prev) => [...prev, data.current[d.userId].name + "對大家說" + d.message]);
        });
       
        client.current.subscribe('/user/queue/newUser', (message)=>{
          client.current.subscribe(`/topic/${roomId}/newUser`, (message)=>{
            const resdata = JSON.parse(message.body);
            data.current = Object.assign({}, data.current, resdata);
            
            setMessageHistory((prev) => [...prev, '歡迎'+ Object.values(resdata)[0].name+ "加入房間"]);
          });
          const resdata = JSON.parse(message.body);
          data.current = resdata;
          // setSelfIdx(resdata.id);
          setRenderTrigger((prev) => prev + 1);
          setMessageHistory((prev) => [...prev, '歡迎'+ data.current[userId].name+ "加入房間"]);
        });
        client.current.subscribe('/user/queue/private', (message)=>{
          let d = JSON.parse(message.body);
          setMessageHistory((prev) => [...prev, data.current[d.userId].name + "對我說" + d.message]);
        });
        // 發送消息加入聊天室
        // const joinMessage = {
        //   kind: 0, // 假設 0 表示加入聊天室
        //   data: JSON.stringify({ name }),
        // };
        client.current.publish({
          destination: '/app/chat.join', // 根據你的服務器端點進行調整
          body: JSON.stringify({ 
            name: name, 
            roomId: roomId 
        }) 
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
      // 添加 beforeunload 事件监听器
      const handleBeforeUnload = () => {
        // if (client.current && client.current.connected) {
        //   client.current.publish({
        //     destination: `/app/room.${roomId}/disconnect`,
        //     body: JSON.stringify({ message: 'Client is disconnecting' }),
        //   });
        //   var kk = 1;
        //   client.current.deactivate(); // 优雅关闭连接
        // }
      };
  
      window.addEventListener('beforeunload', handleBeforeUnload);
  
      // 清理：组件卸载时移除事件监听器和关闭连接
 
    client.current.activate();

    return () => {
      if (client.current) {
        client.current.deactivate();
      }
      window.removeEventListener('beforeunload', handleBeforeUnload);

    };
  }, [name, navigate]);

  

  const handleChange = (e) => {
    setText(e.target.value);
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
    <Box display="flex" flexDirection="row" height="100vh">
      {/* 左側：聊天內容區域 */}
      <Box flex="1" display="flex" flexDirection="column" borderRight={{ base: "none", md: "1px solid gray" }}>
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
        <Box p={4} background="purple.100" borderTop="1px solid gray" position="sticky" bottom={0}>
          <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
            <Text>對大家說：</Text>
            {/* 新按鈕與文字平行 */}
            <FancyButton
              delegate={() => {
                console.log("新按鈕被點擊");
                // 這裡可以添加更多新按鈕的功能
              }}
              text="新按鈕"
              height="2rem" // 固定高度
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
            <InputRightElement width="5rem" display="flex" alignItems="center">
              {/* 送出按鈕 */}
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
                height="2rem" // 固定高度
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
      <Drawer isOpen={isDrawerOpen} placement="left" onClose={() => setIsDrawerOpen(false)}>
        <DrawerOverlay />
        <DrawerContent>
          <DrawerHeader borderBottomWidth="1px">人員列表</DrawerHeader>
          <DrawerBody>
            {Object.entries(data.current).map(([index, person]) => (
              <HStack
                key={index}
                borderTop="1px solid gray"
                p={2}
                _hover={{ cursor: "pointer", bg: "gray.100" }}
                onClick={() => {
                  setTalkTo(index);
                  setIsDrawerOpen(false);
                  console.log(`與 ${person.name} 開始聊天`);
                }}
                justifyContent="flex-start"
              >
                            <Avatar 
                  name={person.name} 
                  size="lg" 
                  key={person.img} 
                  src={`data:image/png;base64,${person.img}?t=${new Date().getTime()}`} 
                  alt="avatar" 
                />

                <Text fontSize="lg">{person.name}</Text>
              </HStack>
            ))}
          </DrawerBody>
        </DrawerContent>
      </Drawer>

      {/* 右側：聊天室名稱與人員列表（桌面版顯示） */}
      {!isMobile && (
        <Box width="300px" display="flex" flexDirection="column" p={4}>
          <Box px={3} py={3} position="relative" mb={4} textAlign="center">
            <Text fontSize="2xl">{roomName}</Text>
          </Box>
          <Box flex="1" width="300px" overflowY="scroll" paddingRight="23px" ref={ref}>
            {Object.entries(data.current).map(([index, person]) => (
              <HStack
                key={index}
                borderTop="1px solid gray"
                p={2}
                _hover={{ cursor: "pointer", bg: "gray.100" }}
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
