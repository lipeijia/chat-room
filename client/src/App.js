import { useEffect, useState } from 'react';
import axios from 'axios';
import config from './config/config';

function App() {
  const [str, setStr] = useState('');
  useEffect(() => {
    const url = `${config.SERVER_POINT}hello`;
    axios
      .get(url)
      .then((res) => {
        console.log(res);
        setStr(res.data);
      })
      .catch((err) => console.log(err));
  }, []);

  return <h1>{str}</h1>;
}

export default App;
