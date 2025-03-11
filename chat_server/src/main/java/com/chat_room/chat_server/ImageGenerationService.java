package com.chat_room.chat_server;

import ai.onnxruntime.*;
import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Service;

import org.apache.commons.math3.distribution.NormalDistribution;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
@Service
public class ImageGenerationService {
    
    // 根據你的模型修改這些參數
    private static final int NOISE_DIM = 256;
    private static final String[] INPUT_NAME = new String[]{"onnx::Gemm_0", "onnx::Reshape_0"};
    private static final String MODEL_PATH = "models/tower_gan_model.onnx"; // 修改為你的 ONNX 模型路徑
    
    private final OrtEnvironment env;


    private final ONNXModelLoader modelLoader;
    private final Random random;


    public ImageGenerationService(ONNXModelLoader modelLoader) {
        this.env = OrtEnvironment.getEnvironment();
        this.modelLoader = modelLoader;
        this.random = new Random();
    }

    /**
     * 產生隨機 noise，做模型推論，並轉換輸出圖片成 Base64 字串。
     */
    public String generateImage(String modelKey) throws OrtException, IOException {
        OrtSession session = modelLoader.getSession(modelKey);
        if (session == null) {
            throw new IllegalArgumentException("Model session not found for key: " + modelKey);
        }
        // 產生隨機 noise（使用正態分佈）
        float[] noiseData = new float[NOISE_DIM];
        NormalDistribution normalDist = new NormalDistribution(0, 1);
        for (int i = 0; i < NOISE_DIM; i++) {
            noiseData[i] = (float) normalDist.sample();
        }
        long[] inputShape = new long[]{1, NOISE_DIM};
        
        // 將 float[] 轉成 FloatBuffer
        FloatBuffer fb = FloatBuffer.wrap(noiseData);
        OnnxTensor inputTensor = OnnxTensor.createTensor(env, fb, inputShape);
        
        // 放入 Map，鍵名稱要與模型相符
        Map<String, OnnxTensor> inputs = Collections.singletonMap(INPUT_NAME[Integer.parseInt(modelKey)], inputTensor);
        
        // 推論
        OrtSession.Result results = session.run(inputs);
        OnnxValue outputValue = results.get(0);
        
        // 假設模型輸出 shape 為 [1, 3, H, W]
        float[][][][] outputArray = (float[][][][]) outputValue.getValue();
        float[][][] imageTensor = outputArray[0];
    
        // 確保 shape 為 (3, H, W)
        if (imageTensor.length == imageTensor[0].length) { // 如果 shape 是 (H, W, 3)
            float[][][] transposedTensor = new float[3][imageTensor.length][imageTensor[0].length];
            for (int i = 0; i < imageTensor.length; i++) {
                for (int j = 0; j < imageTensor[0].length; j++) {
                    for (int k = 0; k < imageTensor[0][0].length; k++) {
                        transposedTensor[k][i][j] = imageTensor[i][j][k];
                    }
                }
            }
            imageTensor = transposedTensor;
        }
    
        // 轉換 tensor 成 BufferedImage
        BufferedImage image = tensorToImage(imageTensor);
        
        // 轉成 Base64 字串
        String base64Image = imageToBase64(image);
        
        inputTensor.close();
        return base64Image;
    }
    
    /**
     * 將 shape 為 [channels, height, width] 的 tensor 轉換成 BufferedImage  
     * 假設 channels = 3 (RGB) 且數值範圍在 [0,1]
     */
    private BufferedImage tensorToImage(float[][][] tensor) {
        int channels = tensor.length; // 預期 3
        int height = tensor[0].length;
        int width = tensor[0][0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 將 [0,1] 轉換到 [0,255]
                int r = Math.min(255, Math.max(0, (int) ((tensor[0][y][x]+1)/2 * 255)));
                int g = Math.min(255, Math.max(0, (int) ((tensor[1][y][x]+1)/2 * 255)));
                int b = Math.min(255, Math.max(0, (int) ((tensor[2][y][x]+1)/2 * 255)));
                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
        
    }
    
    /**
     * 將 BufferedImage 轉換成 Base64 編碼的字串
     */
    private String imageToBase64(BufferedImage image) throws IOException {
        // ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // // 儲存成 PNG 格式
        // ImageIO.write(image, "png", baos);
        // byte[] imageBytes = baos.toByteArray();
        // baos.close();
        // return Base64.getEncoder().encodeToString(imageBytes);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 儲存成 JPEG 格式，若有需要可以設定 JPEG 壓縮品質
        ImageIO.write(image, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();
        baos.close();
        var k = Base64.getEncoder().encodeToString(imageBytes);
        return k;
    }
}
