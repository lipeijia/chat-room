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
  const [data, setData] = useState([]);
  const [text, setText] = useState('');
  const [talkTo, setTalkTo] = useState('');
  const [messageHistory, setMessageHistory] = useState([]);
  const socket = useRef();
  const ref = useRef();
  const navigate = useNavigate();
  const location = useLocation();
  
  const name = useMemo(
    () => new URLSearchParams(location.search).get('name'),
    [location.search]
  );
  
  const selfIdx = useMemo(() => {
    if (!data || !name) return;
    return data.findIndex((p) => p.name === name);
  }, [data, name]);

  useEffect(() => {

    if (!name || !!socket?.current) return;
    const url = `ws://localhost:8080/ws?name=${name}`;
    const _socket = new WebSocket(url);
    _socket.onopen = socket_onopen;
    _socket.onmessage = socket_onmessage;
    _socket.onclose = socket_onclose;
    _socket.onSend = socket_onsend;
    _socket.cusData = data;

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

          setData(JSON.parse(res.data));
          break;
        case 1: //有人加入
          const _newPerson = JSON.parse(res.data);
          setData((prev) => [...prev, _newPerson]);
          this.cusData.push(_newPerson);
          break;
        case 2: //接收密語
          resData = JSON.parse(res.data);
          let _current = {
            sender: data?.[selfIdx]?.name ?? '',
            receiver: data?.[talkTo]?.name ?? '',
            data: resData?.data
          };
          console.log(_current);
          setMessageHistory((prev) => [...prev, _current]);
          break;
        case 3: // 斷線
          resData = JSON.parse(res.data);
          const updateList = () =>
            [...data].filter((_, index) => index !== resData?.leftIdx);
          setData(updateList);
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

    socket.current = _socket;
    // eslint-disable-next-line
  }, [name, socket]);

  useOutsideClick({
    ref: ref,
    handler: () => setTalkTo('')
  });

  const handleChange = (e) => {
    setText(e.target.value);
  };

  const handleTalk = (val) => {
    socket.current.onSend(2, val, selfIdx, talkTo);
    // setMessageHistory((prev) => [...prev, _current]);
    setText('');
  };

  return (
    <>
      <Box px={3} py={3} position='relative'>
        <Text fontSize='2xl'>匿名聊天室</Text>
      </Box>
      <Box mx={2} ref={ref}>
        {data?.map((person, index) => (
          <HStack
            key={`${index}-${person.id}`}
            borderTop='1px solid gray'
            id={person.id}
            index={index}
            p={2}
            _hover={{ cursor: 'pointer' }}
            onClick={
              name !== data[index].name
                ? () => {
                    setTalkTo(index);
                    console.log(index);
                  }
                : null
            }
          >
            <Avatar name={person.name} />
            <Text fontSize='lg'>{person.name}</Text>
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
              <Button
                h='1.75rem'
                size='sm'
                onClick={() => handleTalk(text)}
                type='button'
              >
                送出
              </Button>
            </InputRightElement>
          </InputGroup>
        </Box>
      </Box>
      <Box>
        {messageHistory?.map((m, idx) => (
          <div key={idx}>
            <p>
              對大家説:
            </p>
            <p>{m?.data}</p>
          </div>
        ))}
      </Box>
    </>
  );
}

export default Room;
