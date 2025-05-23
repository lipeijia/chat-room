# Stage 1: 使用 Node.js 建置 React 應用
FROM node:18-alpine AS builder
WORKDIR /app

# 複製 package.json 和 lock 檔案並安裝相依套件
COPY package.json package-lock.json ./
RUN npm install

# 複製所有檔案到容器中
COPY . .

# 設定 build 參數與環境變數
ARG REACT_APP_ENV=GCP
ENV REACT_APP_ENV=$REACT_APP_ENV

# 執行 React 應用建置
RUN npm run build

# Stage 2: 使用 Nginx 提供服務
FROM nginx:alpine
# 從前一個階段複製建置好的檔案到 Nginx 預設目錄
COPY --from=builder /app/build /usr/share/nginx/html

# 暴露 Nginx 服務的 80 端口
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
