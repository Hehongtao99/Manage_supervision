package com.example.auth.util;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.IntIndexer;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_face.StandardCollector;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_face.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_objdetect.*;

/**
 * 人脸识别工具类，封装JavaCV/OpenCV的人脸检测和识别功能
 */
@Component
public class FaceRecognitionUtil {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionUtil.class);

    // 人脸检测器
    private CascadeClassifier faceDetector;
    
    // 人脸识别器
    private FaceRecognizer faceRecognizer;
    
    // 阈值 - 低于此值认为是同一个人 (LBPH距离，越小越相似)
    private static final double LBPH_RECOGNITION_THRESHOLD = 70.0;
    
    // 确保资源文件被正确加载
    private static boolean resourcesLoaded = false;
    
    public FaceRecognitionUtil() {
        // 预加载 - 在构造函数中只记录日志，不做初始化
        logger.info("FaceRecognitionUtil构造函数被调用");
    }
    
    @PostConstruct
    public void initialize() {
        logger.info("开始初始化FaceRecognitionUtil...");
        
        try {
            // 确保OpenCV库被加载
            if (!resourcesLoaded) {
                // 这会触发javacpp加载OpenCV的本地库
                try {
                    // 预加载 OpenCV core
                    logger.info("尝试加载OpenCV本地库...");
                    Class<?> clsCore = org.bytedeco.opencv.global.opencv_core.class;
                    logger.info("成功加载OpenCV core本地库");
                    
                    resourcesLoaded = true;
                } catch (Throwable e) {
                    logger.error("加载本地库失败", e);
                    throw new RuntimeException("加载本地库失败", e);
                }
            }
            
            // 初始化人脸检测器 (使用Haar级联分类器)
            logger.info("开始加载Haar级联分类器模型文件");
            String cascadePath = extractHaarCascadeToTempFile();
            logger.info("Haar级联分类器模型文件提取完成: {}", cascadePath);
            
            faceDetector = new CascadeClassifier();
            boolean loaded = faceDetector.load(cascadePath);
            if (!loaded) {
                logger.error("无法加载Haar级联分类器: {}", cascadePath);
                throw new IOException("无法加载Haar级联分类器: " + cascadePath);
            }
            logger.info("Haar级联分类器加载成功");
            
            // 初始化人脸识别器 (使用LBPH算法)
            logger.info("开始创建LBPH人脸识别器");
            faceRecognizer = LBPHFaceRecognizer.create();
            logger.info("LBPH人脸识别器创建成功");
            
            logger.info("FaceRecognitionUtil初始化完成");
        } catch (Exception e) {
            logger.error("初始化人脸识别组件失败", e);
            throw new RuntimeException("初始化人脸识别组件失败", e);
        }
    }
    
    /**
     * 将内置的Haar级联分类器文件提取到临时文件
     */
    private String extractHaarCascadeToTempFile() throws IOException {
        // 尝试多个可能的路径
        String[] possiblePaths = {
            "/models/haarcascade_frontalface_alt.xml",
            "/haarcascade_frontalface_alt.xml", 
            "/models/haarcascade_frontalface_default.xml",
            "/haarcascade_frontalface_default.xml"
        };
        
        InputStream is = null;
        String usedPath = null;
        
        // 尝试从不同路径加载
        for (String path : possiblePaths) {
            logger.info("尝试从路径加载人脸检测模型: {}", path);
            is = getClass().getResourceAsStream(path);
            if (is != null) {
                usedPath = path;
                logger.info("成功从路径加载模型: {}", path);
                break;
            }
        }
        
        // 如果所有路径都失败，抛出异常
        if (is == null) {
            logger.error("在classpath中找不到Haar级联分类器文件");
            throw new IOException("在classpath中找不到Haar级联分类器文件");
        }
        
        try (InputStream inputStream = is) {
            // 创建临时文件
            Path tempFile = Files.createTempFile("haarcascade_", ".xml");
            logger.info("从临时文件加载Haar级联分类器: {}", tempFile.toString());
            
            // 写入临时文件
            try (OutputStream os = Files.newOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
            
            // 确保临时文件在JVM退出时被删除
            tempFile.toFile().deleteOnExit();
            
            return tempFile.toString();
        }
    }
    
    /**
     * 从MultipartFile中读取Mat图像
     */
    private Mat readMatFromMultipartFile(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            byte[] imageBytes = is.readAllBytes();
            
            // 使用OpenCV解码图像
            MatVector mat = new MatVector(1);
            mat.put(0, imdecode(new Mat(imageBytes), IMREAD_COLOR));
            return mat.get(0);
        }
    }
    
    /**
     * 从Base64字符串中读取Mat图像
     */
    private Mat readMatFromBase64(String base64Image) throws IOException {
        // 从Base64字符串中去除前缀，如 "data:image/jpeg;base64,"
        if (base64Image.contains(",")) {
            base64Image = base64Image.split(",")[1];
        }
        
        // 解码Base64
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        
        // 使用OpenCV解码图像
        return imdecode(new Mat(imageBytes), IMREAD_COLOR);
    }
    
    /**
     * 检测人脸并提取特征
     * 
     * @param image 输入图像
     * @return 人脸特征向量
     */
    public Mat extractFaceFeatures(Mat image) {
        // 转换为灰度图
        Mat grayImage = new Mat();
        cvtColor(image, grayImage, COLOR_BGR2GRAY);
        
        // 检测人脸
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(grayImage, faces, 1.1, 3, 0, new Size(30, 30), new Size());
        
        // 如果没有检测到人脸或检测到多个人脸，返回null
        if (faces.empty() || faces.size() > 1) {
            return null;
        }
        
        // 获取人脸区域
        Rect faceRect = faces.get(0);
        Mat faceROI = new Mat(grayImage, faceRect);
        
        // 调整大小为标准尺寸 (100x100)
        Mat resizedFace = new Mat();
        resize(faceROI, resizedFace, new Size(100, 100));
        
        // 执行直方图均衡化以提高对光照变化的鲁棒性
        equalizeHist(resizedFace, resizedFace);
        
        return resizedFace;
    }
    
    /**
     * 从MultipartFile提取人脸特征
     */
    public byte[] extractFaceFeaturesFromFile(MultipartFile file) throws IOException {
        Mat image = readMatFromMultipartFile(file);
        Mat faceFeatures = extractFaceFeatures(image);
        
        if (faceFeatures == null) {
            return null;
        }
        
        // 将Mat转为byte数组
        int size = (int) (faceFeatures.total() * faceFeatures.elemSize());
        byte[] featuresBytes = new byte[size];
        BytePointer bytePointer = faceFeatures.data();
        bytePointer.get(featuresBytes);
        
        return featuresBytes;
    }
    
    /**
     * 从Base64字符串提取人脸特征
     */
    public byte[] extractFaceFeaturesFromBase64(String base64Image) throws IOException {
        Mat image = readMatFromBase64(base64Image);
        Mat faceFeatures = extractFaceFeatures(image);
        
        if (faceFeatures == null) {
            return null;
        }
        
        // 将Mat转为byte数组
        int size = (int) (faceFeatures.total() * faceFeatures.elemSize());
        byte[] featuresBytes = new byte[size];
        BytePointer bytePointer = faceFeatures.data();
        bytePointer.get(featuresBytes);
        
        return featuresBytes;
    }
    
    /**
     * 对比两个人脸特征，返回相似度
     * 
     * @param feature1 特征1
     * @param feature2 特征2
     * @return 距离值（越小表示越相似）
     */
    public double compareFaceFeatures(byte[] feature1, byte[] feature2) {
        // 检查特征大小是否一致
        if (feature1 == null || feature2 == null || feature1.length != feature2.length) {
            logger.warn("Input features are null or sizes do not match.");
            // 返回一个表示非常不相似的距离值
            return Double.MAX_VALUE; 
        }
        
        try {
            // 将byte数组转为Mat (假设特征是 100x100 灰度图)
            Mat mat1 = new Mat(100, 100, CV_8UC1);
            Mat mat2 = new Mat(100, 100, CV_8UC1);
            
            BytePointer bp1 = new BytePointer(feature1);
            BytePointer bp2 = new BytePointer(feature2);
            
            mat1.data(bp1);
            mat2.data(bp2);

            // ---- 使用 LBPHFaceRecognizer 进行比较 ----
            // 1. 准备训练数据 (仅包含存储的特征)
            MatVector images = new MatVector(1);
            images.put(0, mat1);
            Mat labels = new Mat(1, 1, CV_32SC1);
            // 使用 IntIndexer 设置标签值
            try (IntIndexer indexer = labels.createIndexer()) {
                indexer.put(0, 0, 1); // 给存储的特征一个虚拟标签 1
            }

            // 2. 临时"训练"识别器 (会覆盖之前的训练状态)
            faceRecognizer.train(images, labels);

            // 3. 对新特征进行预测，获取距离
            // 使用 StandardCollector 来收集预测结果
            StandardCollector collector = StandardCollector.create();
            faceRecognizer.predict_collect(mat2, collector);

            // 释放临时的 Mat 和 Pointer (除了 collector)
            bp1.close();
            bp2.close();
            mat1.close();
            mat2.close();
            images.close();
            labels.close();
            
            // 获取最小距离 (LBPH 中 confidence 即距离)
            double distance = collector.getMinDist();
            
            // 释放 collector
            collector.close(); 
            
            // LBPH 返回的 confidence/distance，值越小越相似
            return distance;

        } catch (Exception e) {
            logger.error("Error comparing face features using LBPH: {}", e.getMessage(), e);
            // 发生错误时，返回一个表示非常不相似的距离值
            return Double.MAX_VALUE;
        }
    }
    
    /**
     * 验证人脸匹配
     * 
     * @param storedFeatures 存储的特征
     * @param newFeatures 新提取的特征
     * @return 是否匹配
     */
    public boolean isFaceMatched(byte[] storedFeatures, byte[] newFeatures) {
        double distance = compareFaceFeatures(storedFeatures, newFeatures);
        logger.info("Face match distance: " + distance);
        return distance < LBPH_RECOGNITION_THRESHOLD;
    }
    
    /**
     * 从MultipartFile中提取人脸图像
     */
    public byte[] extractFaceImageFromFile(MultipartFile file) throws IOException {
        // 读取图像
        Mat image = readMatFromMultipartFile(file);
        
        // 转换为灰度图
        Mat grayImage = new Mat();
        cvtColor(image, grayImage, COLOR_BGR2GRAY);
        
        // 检测人脸
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(grayImage, faces);
        
        // 如果没有检测到人脸或检测到多个人脸，返回null
        if (faces.empty() || faces.size() > 1) {
            return null;
        }
        
        // 获取人脸区域
        Rect faceRect = faces.get(0);
        Mat faceROI = new Mat(image, faceRect);
        
        // 调整大小为标准尺寸 (200x200)
        Mat resizedFace = new Mat();
        resize(faceROI, resizedFace, new Size(200, 200));
        
        // 将Mat转为JPEG图像字节
        MatVector vector = new MatVector(1);
        vector.put(0, resizedFace);
        
        BytePointer buffer = new BytePointer();
        imencode(".jpg", vector.get(0), buffer);
        
        // 转换为byte数组
        byte[] imageBytes = new byte[(int)buffer.limit()];
        buffer.get(imageBytes);
        
        return imageBytes;
    }
    
    /**
     * 从Base64字符串中提取人脸图像
     */
    public byte[] extractFaceImageFromBase64(String base64Image) throws IOException {
        // 读取图像
        Mat image = readMatFromBase64(base64Image);
        
        // 转换为灰度图
        Mat grayImage = new Mat();
        cvtColor(image, grayImage, COLOR_BGR2GRAY);
        
        // 检测人脸
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(grayImage, faces);
        
        // 如果没有检测到人脸或检测到多个人脸，返回null
        if (faces.empty() || faces.size() > 1) {
            return null;
        }
        
        // 获取人脸区域
        Rect faceRect = faces.get(0);
        Mat faceROI = new Mat(image, faceRect);
        
        // 调整大小为标准尺寸 (200x200)
        Mat resizedFace = new Mat();
        resize(faceROI, resizedFace, new Size(200, 200));
        
        // 将Mat转为JPEG图像字节
        MatVector vector = new MatVector(1);
        vector.put(0, resizedFace);
        
        BytePointer buffer = new BytePointer();
        imencode(".jpg", vector.get(0), buffer);
        
        // 转换为byte数组
        byte[] imageBytes = new byte[(int)buffer.limit()];
        buffer.get(imageBytes);
        
        return imageBytes;
    }
} 