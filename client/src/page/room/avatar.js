import { useState, useEffect } from 'react';
import { Avatar } from '@chakra-ui/react';

const BlobAvatar = ({ person }) => {
  const [blobUrl, setBlobUrl] = useState('');

  useEffect(() => {
    if (person.img) {
      // 如果 person.img 已經包含 "data:image/..."，就移除前綴
      const base64Data = person.img.includes('data:image')
        ? person.img.split(',')[1]
        : person.img;

      // 轉換 Base64 為二進位資料
      const byteCharacters = atob(base64Data);
      const byteNumbers = new Array(byteCharacters.length).fill(0)
        .map((_, i) => byteCharacters.charCodeAt(i));
      const byteArray = new Uint8Array(byteNumbers);

      // 產生 Blob，並建立 URL
      const blob = new Blob([byteArray], { type: 'image/png' });
      const url = URL.createObjectURL(blob);
      setBlobUrl(url);

      // 當元件卸載或圖片更新時，釋放 Blob URL
      return () => {
        URL.revokeObjectURL(url);
      };
    }
  }, [person.img]);

  return (
    <Avatar 
      name={person.name} 
      size="lg" 
      src={blobUrl} 
      alt="avatar" 
    />
  );
};

export default BlobAvatar;
