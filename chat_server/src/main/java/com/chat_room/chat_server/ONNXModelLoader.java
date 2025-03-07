package com.chat_room.chat_server;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;

@Component
public class ONNXModelLoader {
    private final OrtEnvironment env;
    private final Map<String, OrtSession> sessionMap;
    private final Random random;

    public ONNXModelLoader() throws Exception {
        this.env = OrtEnvironment.getEnvironment();
        this.sessionMap = new HashMap<>();
        this.random = new Random();

        // 設定模型對應的 map
        Map<String, String> roomMap = new HashMap<>();
        roomMap.put("1", "tower_gan_model");
        roomMap.put("0", "cat_gan_model");

        // 遍歷 roomMap 載入所有模型
        for (Map.Entry<String, String> entry : roomMap.entrySet()) {
            String modelKey = entry.getKey();
            String modelName = entry.getValue();
            String modelPath = "/models/" + modelName + ".onnx";

            // 嘗試從 classpath 讀取模型
            try (InputStream modelStream = getClass().getResourceAsStream(modelPath)) {
                if (modelStream == null) {
                    throw new FileNotFoundException("Cannot find model file in classpath: " + modelPath);
                }

                // 將模型寫入臨時檔案
                File tempFile = File.createTempFile(modelName, ".onnx");
                tempFile.deleteOnExit();
                Files.copy(modelStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // 建立 ONNX Session
                OrtSession.SessionOptions options = new OrtSession.SessionOptions();
                OrtSession session = env.createSession(tempFile.getAbsolutePath(), options);

                // 存入 sessionMap
                sessionMap.put(modelKey, session);
            } catch (IOException | OrtException e) {
                System.err.println("Error loading model " + modelPath + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public OrtSession getSession(String key) {
        return sessionMap.get(key);
    }

    public Map<String, OrtSession> getSessionMap() {
        return sessionMap;
    }

    @PreDestroy
    public void close() {
        // 關閉所有 ONNX Sessions
        try {
            for (OrtSession session : sessionMap.values()) {
                session.close();
            }
            env.close();
        } catch (OrtException e) {
            e.printStackTrace();
        }
    }
}
