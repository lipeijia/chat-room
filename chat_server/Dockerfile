# 使用官方的 OpenJDK 17 影像作為基底（可根據你的 Java 版本調整）
FROM amazoncorretto:21

# 設定工作目錄
WORKDIR /app

# 複製 Spring Boot 應用程式 JAR 到容器內
COPY target/chat_server-0.0.1-SNAPSHOT.jar app.jar

# 設定 Spring Boot 應用的啟動命令
CMD ["java", "-jar", "app.jar"]