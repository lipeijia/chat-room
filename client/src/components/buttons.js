import React from "react";
import { Box, Button } from "@chakra-ui/react";
import { gsap } from "gsap";

const FancyButton = ({text, delegate}) => {
  const buttonRef = React.useRef(null);
  const rippleRef = React.useRef(null);

  const handleButtonClick = (e) => {
    const button = buttonRef.current;
    const ripple = rippleRef.current;

    const rect = button.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

       // 初始化 ripple 狀態
       gsap.set(ripple, { top: y, left: x, scale: 0, opacity: 1 });

       // 執行動畫
       gsap.to(ripple, {
         scale: 10,
         opacity: 0,
         duration: 0.6,
         onComplete: () => {
           gsap.set(ripple, { scale: 0, opacity: 1 });
         },
       });
   
       // 按鈕縮放動畫
       gsap.fromTo(
         button,
         { scale: 1 },
         { scale: 0.95, duration: 0.1, yoyo: true, repeat: 1 }
       );
    delegate();
  };

  return (
    <Box position="relative" display="inline-block">
      <Box
        ref={rippleRef}
        position="absolute"
        width="20px"
        height="20px"
        borderRadius="50%"
        backgroundColor="rgba(255, 255, 255, 0.5)"
        pointerEvents="none"
        zIndex={0}
      ></Box>
      <Button
        ref={buttonRef}
        zIndex={1}
        bgGradient="linear(to-r, teal.400, blue.500)"
        color="white"
        _hover={{ bgGradient: "linear(to-r, teal.500, blue.600)" }}
        _active={{ bgGradient: "linear(to-r, teal.300, blue.400)" }}
        borderRadius="full"
        px="8"
        py="4"
        fontSize="lg"
        boxShadow="lg"
        onClick={handleButtonClick}
      >
       {text}
      </Button>
    </Box>
  );
};

export default FancyButton;
