import { useState, useMemo, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router';
import {
  Button,
  Input,
  Avatar,
  HStack,
  Text,
  Box,
  InputGroup,
  InputRightElement,
  useOutsideClick
} from '@chakra-ui/react';

function Room() {
  // const [data, setData] = useState([]);
  let data = useRef([])
  const [text, setText] = useState('');
  const [talkTo, setTalkTo] = useState('');
  const [renderTrigger, setRenderTrigger] = useState(0); // 用來觸發渲染
  const [selfIdx, setSelfIdx] = useState(-1);
  const [messageHistory, setMessageHistory] = useState([]);
  const socket = useRef();
  const ref = useRef();
  const navigate = useNavigate();
  const location = useLocation();
  
  const name = useMemo(
    () => new URLSearchParams(location.search).get('name'),
    [location.search]
  );
  // const selfIdx = -1;
  // const selfIdx = useMemo(() => {
  //   // if (!data || !name) return;
  //   // return data.findIndex((p) => p.name === name);
  // }, [data, name]);

  useEffect(() => {
    
    if (!name || socket.current) return;
    const url = `ws://localhost:8080/ws?name=${name}`;
    const _socket = new WebSocket(url);
    _socket.onopen = socket_onopen;
    _socket.onmessage = socket_onmessage;
    _socket.onclose = socket_onclose;
    _socket.onSend = socket_onsend;
    _socket.cusData = data;
    socket.current = _socket;

    function socket_onopen(val) {
      console.log('connect: ', val);
    }

    function socket_onsend(kind = 0, val, senderIdx, receiverIdx) {
      _socket.send(
        JSON.stringify({
          kind: kind,
          data: val,
          senderIdx: senderIdx,
          receiverIdx: receiverIdx
        })
      );
    }

    function socket_onmessage(e) {
      const res = JSON.parse(e.data);
      let resData;
      switch (res.kind) {
        case 0: //新聊天室

          // setData(JSON.parse(res.data));
          var resdata = JSON.parse(res.data);
          data.current = resdata.guys;
          // let idx1 = data.current.map((person, id) => {

          // });
          // Object.entries(data.current).forEach(([key, value]) => {
          //   console.log(key, value);
          // });
          setSelfIdx(resdata.id);
          setRenderTrigger((prev) => prev+1);
          break;
        case 1: //有人加入
          const _newPerson = JSON.parse(res.data);
          // setData((prev) => [...prev, _newPerson]);
          data.current[_newPerson.id] = _newPerson.guy;
          setRenderTrigger((prev) => prev+1);
          // this.cusData.push(_newPerson);
          break;
        case 2: //接收密語
          resData = JSON.parse(res.data);
          let _current = {
            sender: data.current?.[resData.senderIdx]?.name ?? '',
            receiver: data.current?.[resData.receiverIdx]?.name ?? '',
            data: resData?.data
          };
          console.log(_current);
          setMessageHistory((prev) => [...prev, _current]);
          break;
        case 3: // 斷線
          resData = JSON.parse(res.data);
          delete data.current[resData.leftIdx];
          setRenderTrigger((prev) => prev+1);
          // const updateList = () =>
          //   [...data].filter((_, index) => index !== resData?.leftIdx);
          // setData(updateList);
          break;
        default:
          console.log(data);
          return;
      }
    }

    function socket_onclose(val) {
      console.log('disconnect: ', val);
      navigate('/');
    }

    // socket.current = _socket;
    // eslint-disable-next-line
  }, [name, socket]);

  // useOutsideClick({
  //   ref: ref,
  //   handler: () => setTalkTo('')
  // });

  const handleChange = (e) => {
    setText(e.target.value);
  };

  const handleTalk = (val) => {
    socket.current.onSend(2, val, selfIdx, talkTo);
    // setMessageHistory((prev) => [...prev, _current]);
    setText('');
  };

  return (
    <Box display="flex" flexDirection="row" height="100vh">
      {/* 左側：聊天內容區域 */}
      <Box flex="1" display="flex" flexDirection="column" borderRight="1px solid gray">
        <Box flex="1" overflowY="auto" p={4}>
          <Text fontSize="2xl" mb={4}>聊天內容</Text>
          {messageHistory?.map((m, idx) => {
            if (m?.sender && m?.receiver) {
              return (
                <Box key={idx} mb={4}>
                  <Text fontWeight="bold">{`${m.sender} 對 ${m.receiver} 說:`}</Text>
                  <Text>{m.data || "（無內容）"}</Text>
                </Box>
              );
            } else if (m?.sender) {
              return (
                <Box key={idx} mb={4}>
                  <Text fontWeight="bold">{`${m.sender} 對大家說:`}</Text>
                  <Text>{m.data || "（無內容）"}</Text>
                </Box>
              );
            } else if (m?.receiver) {
              return (
                <Box key={idx} mb={4}>
                  <Text fontWeight="bold">{`匿名對 ${m.receiver} 說:`}</Text>
                  <Text>{m.data || "（無內容）"}</Text>
                </Box>
              );
            } else {
              return (
                <Box key={idx} mb={4}>
                  <Text fontWeight="bold">匿名對大家說:</Text>
                  <Text>{m.data || "（無內容）"}</Text>
                </Box>
              );
            }
          })}
        </Box>
        <Box p={4} background="purple.100" borderTop="1px solid gray">
          <Text mb={2}>對大家說：</Text>
          <InputGroup>
            <Input
              name="text"
              value={text}
              background="#fff"
              onChange={handleChange}
            />
            <InputRightElement width="4.5rem">
              <Button
                h="1.75rem"
                size="sm"
                onClick={() => handleTalk(text)}
                type="button"
              >
                送出
              </Button>
            </InputRightElement>
          </InputGroup>
        </Box>
      </Box>
  
      {/* 右側：聊天室名稱與人員列表 */}
      <Box width="300px" display="flex" flexDirection="column" p={4}>
        <Box px={3} py={3} position="relative" mb={4} textAlign="right">
          <Text fontSize="2xl">匿名聊天室</Text>
        </Box>
        <Box flex="1" overflowY="auto" ref={ref}>
          {Object.entries(data.current).map(([index, person]) => (
            <HStack
              key={index}
              borderTop="1px solid gray"
              p={2}
              _hover={{ cursor: "pointer" }}
              onClick={
                name !== data.current[index].name
                  ? () => {
                      setTalkTo(index);
                      console.log(index);
                    }
                  : null
              }
              justifyContent="flex-end"
            >
              <Text fontSize="lg" mr={2}>{person.name}</Text>
              <Avatar name={person.name} />
            </HStack>
          ))}
        </Box>
      </Box>
    </Box>
  );
  
  
}

export default Room;
