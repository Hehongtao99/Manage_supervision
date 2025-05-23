package com.example.auth.util;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * 人脸识别工具类
 */
public class FaceRecognitionUtil {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionUtil.class);
    
    // 确保OpenCV库加载
    static {
        try {
            // 加载JavaCV必要的类和本地库
            // JavaCV会自动处理平台相关的本地库加载
            Loader.load(org.bytedeco.opencv.opencv_java.class);
            logger.info("JavaCV - OpenCV 库加载成功");
        } catch (Exception e) {
            logger.error("JavaCV - OpenCV 库加载失败", e);
        }
    }
    
    private static final String CASCADE_FILE = "haarcascade_frontalface_alt.xml";
    private static final String CASCADE_PATH = "./data/haarcascade/";
    // 使用延迟初始化，不在静态区块中初始化
    private static CascadeClassifier faceDetector;
    
    /**
     * 获取人脸检测器
     */
    private static synchronized CascadeClassifier getFaceDetector() {
        if (faceDetector == null) {
            faceDetector = loadFaceDetector();
        }
        return faceDetector;
    }
    
    /**
     * 加载人脸检测器
     */
    private static CascadeClassifier loadFaceDetector() {
        logger.info("正在加载人脸检测器...");
        CascadeClassifier detector = new CascadeClassifier();
        
        try {
            // 1. 从资源目录加载
            // 检查多种可能的资源路径
            String[] resourcePaths = {
                "./backend/src/main/resources/haarcascade/" + CASCADE_FILE,
                "./src/main/resources/haarcascade/" + CASCADE_FILE,
                "../backend/src/main/resources/haarcascade/" + CASCADE_FILE,
                "./Manage_supervision/backend/src/main/resources/haarcascade/" + CASCADE_FILE,
                "./resources/haarcascade/" + CASCADE_FILE
            };
            
            // 尝试加载项目中的资源文件
            InputStream is = FaceRecognitionUtil.class.getClassLoader()
                .getResourceAsStream("haarcascade/" + CASCADE_FILE);
            
            if (is != null) {
                logger.info("从类路径资源加载级联分类器");
                // 将输入流复制到临时文件
                Path tempFile = Files.createTempFile("cascade_", ".xml");
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
                is.close();
                
                if (detector.load(tempFile.toString())) {
                    logger.info("从类路径资源成功加载级联分类器: {}", tempFile);
                    
                    // 如果本地目录不存在，复制到本地目录
                    File cascadeFile = new File(CASCADE_PATH + CASCADE_FILE);
                    if (!cascadeFile.exists()) {
                        try {
                            Files.createDirectories(Paths.get(CASCADE_PATH));
                            Files.copy(tempFile, cascadeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            logger.info("级联分类器已复制到数据目录: {}", cascadeFile.getAbsolutePath());
                        } catch (Exception e) {
                            logger.warn("复制级联分类器到数据目录失败", e);
                        }
                    }
                    
                    return detector;
                }
            }
            
            // 2. 尝试从各种可能的位置加载文件
            for (String path : resourcePaths) {
                File file = new File(path);
                if (file.exists() && file.length() > 0) {
                    logger.info("尝试从路径加载级联分类器: {}", file.getAbsolutePath());
                    if (detector.load(file.getAbsolutePath())) {
                        logger.info("级联分类器成功加载: {}", file.getAbsolutePath());
                        return detector;
                    }
                }
            }
            
            // 3. 尝试使用JavaCV内置的文件
            try {
                // 使用JavaCV的内置级联分类器
                File javaCVFile = Loader.extractResource(
                    "org/bytedeco/opencv/opencv_objdetect/cascades/" + CASCADE_FILE, 
                    null, "classifier", null);
                
                if (javaCVFile != null && javaCVFile.exists()) {
                    logger.info("从JavaCV资源加载级联分类器: {}", javaCVFile.getAbsolutePath());
                    if (detector.load(javaCVFile.getAbsolutePath())) {
                        logger.info("JavaCV资源级联分类器加载成功");
                        return detector;
                    }
                }
            } catch (Exception e) {
                logger.warn("访问JavaCV资源文件失败", e);
            }
            
            // 4. 所有尝试都失败，输出详细错误信息
            logger.error("无法加载人脸检测器，请确保级联分类器文件 {} 存在", CASCADE_FILE);
            logger.error("已尝试的路径:");
            for (String path : resourcePaths) {
                logger.error("- {}", path);
            }
            logger.error("请确保正确安装OpenCV及级联分类器文件");
            
            return null; // 返回null表示加载失败
        } catch (Exception e) {
            logger.error("加载人脸检测器失败", e);
            return null;
        }
    }
    
    /**
     * 从Base64编码的图像数据中检测人脸
     * @param base64Image Base64编码的图像数据（不含前缀）
     * @return 如果检测到人脸，返回处理后的人脸图像的Base64编码；否则返回null
     */
    public static String detectFace(String base64Image) {
        Map<String, Object> result = detectFaceWithPosition(base64Image);
        if (result != null) {
            return (String) result.get("faceData");
        }
        return null;
    }
    
    /**
     * 从Base64编码的图像数据中检测人脸，并返回人脸位置信息
     * @param base64Image Base64编码的图像数据（不含前缀）
     * @return 包含人脸图像和位置信息的Map，如果未检测到人脸返回null
     */
    public static Map<String, Object> detectFaceWithPosition(String base64Image) {
        try {
            // 确保人脸检测器已加载
            CascadeClassifier detector = getFaceDetector();
            if (detector == null) {
                logger.error("人脸检测器未加载，无法进行人脸检测");
                return null;
            }
            
            // 解码Base64字符串
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            
            // 将字节数组转换为Mat对象
            Mat buffer = new Mat(1, imageBytes.length, CV_8UC1);
            BytePointer bytePointer = new BytePointer(imageBytes);
            buffer.data(bytePointer);
            
            Mat image = imdecode(buffer, IMREAD_COLOR);
            bytePointer.close(); // 释放内存
            
            if (image.empty()) {
                logger.error("图像解码失败");
                return null;
            }
            
            // 获取原始图像尺寸
            int originalWidth = image.cols();
            int originalHeight = image.rows();
            
            // 检测人脸
            RectVector faceDetections = new RectVector();
            // 使用多尺度检测参数，提高检测准确度
            detector.detectMultiScale(image, faceDetections, 1.1, 3, 0, new Size(80, 80), new Size(500, 500));
            
            if (faceDetections.empty() || faceDetections.size() <= 0) {
                logger.warn("未检测到人脸");
                return null;
            }
            
            // 提取最大的人脸
            Rect maxFace = getMaxFace(faceDetections);
            
            // 计算人脸在原图中的相对位置（百分比）
            double relativeX = (double) maxFace.x() / originalWidth;
            double relativeY = (double) maxFace.y() / originalHeight;
            double relativeWidth = (double) maxFace.width() / originalWidth;
            double relativeHeight = (double) maxFace.height() / originalHeight;
            
            // 裁剪并调整人脸区域
            Mat faceROI = new Mat(image, maxFace);
            Mat resizedFace = new Mat();
            Size size = new Size(200, 200);
            resize(faceROI, resizedFace, size);
            
            // 转换为灰度图像
            Mat grayFace = new Mat();
            cvtColor(resizedFace, grayFace, COLOR_BGR2GRAY);
            
            // 应用直方图均衡化以提高图像质量
            Mat equalizedFace = new Mat();
            equalizeHist(grayFace, equalizedFace);
            
            // 将灰度均衡化图像编码为JPG格式
            String tempFileName = Loader.getTempDir() + "/face_" + System.currentTimeMillis() + ".jpg";
            
            // 将矩阵保存为JPG格式的图像文件
            imwrite(tempFileName, equalizedFace);
            
            // 读取临时文件到字节数组
            byte[] resultArray = Files.readAllBytes(Paths.get(tempFileName));
            
            // 删除临时文件
            new File(tempFileName).delete();
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("faceData", Base64.getEncoder().encodeToString(resultArray));
            result.put("position", Map.of(
                "x", relativeX,
                "y", relativeY,
                "width", relativeWidth,
                "height", relativeHeight,
                "absoluteX", maxFace.x(),
                "absoluteY", maxFace.y(),
                "absoluteWidth", maxFace.width(),
                "absoluteHeight", maxFace.height()
            ));
            result.put("detectedFaces", (long) faceDetections.size());
            
            return result;
        } catch (Exception e) {
            logger.error("人脸检测过程中发生异常", e);
            return null;
        }
    }
    
    /**
     * 多次比较人脸并取平均值，提高识别准确性
     * @param storedFaceBase64 存储的人脸数据（Base64编码）
     * @param capturedFaceBase64 当前捕获的人脸数据（Base64编码）
     * @param verificationTimes 验证次数（建议3-5次）
     * @return 包含验证结果的Map，包括平均相似度、各次相似度、是否通过验证等
     */
    public static Map<String, Object> compareMultipleFaces(String storedFaceBase64, String capturedFaceBase64, int verificationTimes) {
        Map<String, Object> result = new HashMap<>();
        List<Double> similarities = new ArrayList<>();
        List<Double> structuralSimilarities = new ArrayList<>(); // 添加结构相似度列表
        List<Boolean> verificationResults = new ArrayList<>();
        
        logger.info("开始进行{}次人脸识别验证", verificationTimes);
        
        try {
            // 解码Base64字符串
            byte[] storedImageBytes = Base64.getDecoder().decode(storedFaceBase64);
            byte[] capturedImageBytes = Base64.getDecoder().decode(capturedFaceBase64);
            
            // 将字节数组转换为Mat对象
            Mat storedBuffer = new Mat(1, storedImageBytes.length, CV_8UC1);
            BytePointer storedPointer = new BytePointer(storedImageBytes);
            storedBuffer.data(storedPointer);
            
            Mat capturedBuffer = new Mat(1, capturedImageBytes.length, CV_8UC1);
            BytePointer capturedPointer = new BytePointer(capturedImageBytes);
            capturedBuffer.data(capturedPointer);
            
            Mat storedImage = imdecode(storedBuffer, IMREAD_GRAYSCALE);
            Mat capturedImage = imdecode(capturedBuffer, IMREAD_GRAYSCALE);
            
            if (storedImage.empty() || capturedImage.empty()) {
                logger.error("图像解码失败");
                result.put("success", false);
                result.put("message", "图像解码失败");
                return result;
            }
            
            // 确保两个图像大小相同
            if (storedImage.cols() != capturedImage.cols() || storedImage.rows() != capturedImage.rows()) {
                resize(capturedImage, capturedImage, new Size(storedImage.cols(), storedImage.rows()));
            }
            
            // 进行多次验证
            for (int i = 0; i < verificationTimes; i++) {
                logger.info("执行第{}次人脸识别", i + 1);
                
                try {
                    // 对捕获的图像进行轻微的随机变换，增加验证的鲁棒性
                    Mat transformedCaptured = applySafeTransformation(capturedImage, i);
                    
                    // 转换为浮点型Mat以便计算
                    Mat storedImageFloat = new Mat();
                    Mat capturedImageFloat = new Mat();
                    storedImage.convertTo(storedImageFloat, CV_32F);
                    transformedCaptured.convertTo(capturedImageFloat, CV_32F);
                    
                    // 使用LBPH人脸识别器
                    LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
                    
                    // 准备训练数据和标签
                    MatVector images = new MatVector(1);
                    images.put(0, storedImage);
                    
                    Mat labelsMat = new Mat(1, 1, CV_32SC1);
                    IntBuffer intBuf = labelsMat.createBuffer();
                    intBuf.put(0, 1);
                    
                    // 训练模型
                    recognizer.train(images, labelsMat);
                    
                    // 预测
                    int[] label = new int[1];
                    double[] confidence = new double[1];
                    recognizer.predict(transformedCaptured, label, confidence);
                    
                    // 计算本次相似度
                    double lbphSimilarity = Math.max(0.0, 1.0 - confidence[0] / 100.0);
                    double structuralSimilarity = calculateStructuralSimilarity(storedImageFloat, capturedImageFloat);
                    double combinedSimilarity = 0.6 * lbphSimilarity + 0.4 * structuralSimilarity;
                    
                    similarities.add(combinedSimilarity);
                    structuralSimilarities.add(structuralSimilarity); // 记录结构相似度
                    
                    // 本次验证是否通过（调整为更合理的单次验证阈值）
                    double singleVerificationThreshold = 0.65; // 单次验证阈值降低到65%
                    boolean singleResult = combinedSimilarity >= singleVerificationThreshold;
                    verificationResults.add(singleResult);
                    
                    logger.info("第{}次验证 - LBPH相似度: {}, 结构相似度: {}, 综合相似度: {}, 单次结果: {}", 
                               i + 1, lbphSimilarity, structuralSimilarity, combinedSimilarity, singleResult);
                    
                    // 清理资源
                    storedImageFloat.release();
                    capturedImageFloat.release();
                    transformedCaptured.release();
                    
                    // 添加小延迟，避免过快连续处理
                    Thread.sleep(50);
                    
                } catch (Exception e) {
                    logger.warn("第{}次验证失败: {}", i + 1, e.getMessage());
                    similarities.add(0.0);
                    structuralSimilarities.add(0.0); // 失败时添加0
                    verificationResults.add(false);
                }
            }
            
            // 清理资源
            storedPointer.close();
            capturedPointer.close();
            
            // 计算统计结果
            double averageSimilarity = similarities.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double maxSimilarity = similarities.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double minSimilarity = similarities.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            
            // 计算结构相似度统计
            double averageStructuralSimilarity = structuralSimilarities.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double maxStructuralSimilarity = structuralSimilarities.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            double minStructuralSimilarity = structuralSimilarities.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            
            long successCount = verificationResults.stream().mapToLong(b -> b ? 1 : 0).sum();
            double successRate = (double) successCount / verificationTimes;
            
            // 最终验证结果判定（调整为更合理的阈值）
            double finalThreshold = 0.60; // 平均相似度阈值降低到60%
            double minSuccessRate = 0.4; // 最低成功率降低到40%（5次中至少2次通过）
            
            boolean finalResult = averageSimilarity >= finalThreshold && successRate >= minSuccessRate;
            
            // 构建结果
            result.put("success", true);
            result.put("verified", finalResult);
            result.put("averageSimilarity", Math.round(averageSimilarity * 10000.0) / 10000.0);
            result.put("maxSimilarity", Math.round(maxSimilarity * 10000.0) / 10000.0);
            result.put("minSimilarity", Math.round(minSimilarity * 10000.0) / 10000.0);
            
            // 添加结构相似度统计
            result.put("averageStructuralSimilarity", Math.round(averageStructuralSimilarity * 10000.0) / 10000.0);
            result.put("maxStructuralSimilarity", Math.round(maxStructuralSimilarity * 10000.0) / 10000.0);
            result.put("minStructuralSimilarity", Math.round(minStructuralSimilarity * 10000.0) / 10000.0);
            
            result.put("successCount", successCount);
            result.put("totalAttempts", verificationTimes);
            result.put("successRate", Math.round(successRate * 10000.0) / 10000.0);
            result.put("similarities", similarities);
            result.put("structuralSimilarities", structuralSimilarities); // 添加结构相似度列表
            result.put("threshold", finalThreshold);
            result.put("minSuccessRate", minSuccessRate);
            
            logger.info("多次验证完成 - 平均相似度: {}, 平均结构相似度: {}, 成功率: {}, 最终结果: {}", 
                       averageSimilarity, averageStructuralSimilarity, successRate, finalResult);
            
            return result;
            
        } catch (Exception e) {
            logger.error("多次人脸比较过程中发生异常", e);
            result.put("success", false);
            result.put("message", "人脸验证失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 对图像应用安全的轻微变换，增加验证的鲁棒性
     */
    private static Mat applySafeTransformation(Mat image, int transformIndex) {
        Mat result = image.clone();
        
        try {
            switch (transformIndex % 4) {
                case 0:
                    // 不做变换
                    break;
                case 1:
                    // 轻微的亮度调整
                    result.convertTo(result, -1, 1.0, 2.0);
                    break;
                case 2:
                    // 轻微的对比度调整
                    result.convertTo(result, -1, 1.05, 0.0);
                    break;
                case 3:
                    // 轻微的高斯模糊
                    Mat temp = new Mat();
                    GaussianBlur(result, temp, new Size(3, 3), 0.5);
                    temp.copyTo(result);
                    temp.release();
                    break;
            }
        } catch (Exception e) {
            logger.warn("图像变换失败，使用原图: {}", e.getMessage());
            return image.clone();
        }
        
        return result;
    }
    
    /**
     * 比较两个人脸是否匹配（保持向后兼容，内部调用多次验证）
     * @param storedFaceBase64 存储的人脸数据（Base64编码）
     * @param capturedFaceBase64 当前捕获的人脸数据（Base64编码）
     * @return 如果匹配返回true，否则返回false
     */
    public static boolean compareFaces(String storedFaceBase64, String capturedFaceBase64) {
        // 使用3次验证确保准确性
        Map<String, Object> result = compareMultipleFaces(storedFaceBase64, capturedFaceBase64, 3);
        return (Boolean) result.getOrDefault("verified", false);
    }
    
    /**
     * 计算两个图像的结构相似度
     * 结构相似度是评估两个图像相似程度的指标，值范围[0,1]，越接近1表示越相似
     */
    private static double calculateStructuralSimilarity(Mat img1, Mat img2) {
        try {
            // 确保两张图片大小一致
            if (img1.size().width() != img2.size().width() || img1.size().height() != img2.size().height()) {
                resize(img2, img2, new Size(img1.size().width(), img1.size().height()));
            }
            
            // 使用适合JavaCV的简化算法计算相似度
            // 1. 计算均值
            Scalar mean1 = mean(img1);
            Scalar mean2 = mean(img2);
            
            // 2. 创建差异矩阵
            Mat diff = new Mat();
            absdiff(img1, img2, diff);
            
            // 3. 计算平均差异
            Scalar meanDiff = mean(diff);
            
            // 4. 计算相似度 (1 - 归一化差异)
            double maxValue = 255.0; // 灰度图像的最大值
            double similarity = 1.0 - (meanDiff.get(0) / maxValue);
            
            // 5. 计算均值相似性
            double meanSimilarity = 1.0 - Math.abs(mean1.get(0) - mean2.get(0)) / maxValue;
            
            // 6. 综合相似度
            double finalSimilarity = 0.5 * similarity + 0.5 * meanSimilarity;
            
            // 释放资源
            diff.release();
            
            logger.info("计算的相似度为: {}", finalSimilarity);
            return finalSimilarity;
        } catch (Exception e) {
            logger.error("计算结构相似度失败", e);
            return 0.0;
        }
    }
    
    /**
     * 从检测到的多个人脸中获取最大的人脸
     */
    private static Rect getMaxFace(RectVector faces) {
        if (faces.size() == 1) {
            return faces.get(0);
        }
        
        Rect maxFace = faces.get(0);
        for (long i = 1; i < faces.size(); i++) {
            Rect face = faces.get(i);
            if ((face.width() * face.height()) > (maxFace.width() * maxFace.height())) {
                maxFace = face;
            }
        }
        return maxFace;
    }
    
    /**
     * 将Base64图像数据转换为BufferedImage
     */
    private static BufferedImage base64ToBufferedImage(String base64Image) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        return ImageIO.read(bis);
    }
    
    /**
     * 将BufferedImage转换为Base64字符串
     */
    private static String bufferedImageToBase64(BufferedImage image, String formatName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, formatName, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * 获取两个人脸的相似度
     * @param storedFaceBase64 存储的人脸数据（Base64编码）
     * @param capturedFaceBase64 当前捕获的人脸数据（Base64编码）
     * @return 相似度（0-1之间的值，1表示完全匹配）
     */
    public static double getFaceSimilarity(String storedFaceBase64, String capturedFaceBase64) {
        try {
            // 解码Base64字符串
            byte[] storedImageBytes = Base64.getDecoder().decode(storedFaceBase64);
            byte[] capturedImageBytes = Base64.getDecoder().decode(capturedFaceBase64);
            
            // 将字节数组转换为Mat对象
            Mat storedBuffer = new Mat(1, storedImageBytes.length, CV_8UC1);
            BytePointer storedPointer = new BytePointer(storedImageBytes);
            storedBuffer.data(storedPointer);
            
            Mat capturedBuffer = new Mat(1, capturedImageBytes.length, CV_8UC1);
            BytePointer capturedPointer = new BytePointer(capturedImageBytes);
            capturedBuffer.data(capturedPointer);
            
            Mat storedImage = imdecode(storedBuffer, IMREAD_GRAYSCALE);
            Mat capturedImage = imdecode(capturedBuffer, IMREAD_GRAYSCALE);
            
            if (storedImage.empty() || capturedImage.empty()) {
                logger.error("图像解码失败");
                return 0.0;
            }
            
            // 确保两个图像大小相同
            if (storedImage.cols() != capturedImage.cols() || storedImage.rows() != capturedImage.rows()) {
                resize(capturedImage, capturedImage, new Size(storedImage.cols(), storedImage.rows()));
            }
            
            // 转换为浮点型Mat以便计算
            Mat storedImageFloat = new Mat();
            Mat capturedImageFloat = new Mat();
            storedImage.convertTo(storedImageFloat, CV_32F);
            capturedImage.convertTo(capturedImageFloat, CV_32F);
            
            // 使用LBPH人脸识别器
            LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
            
            // 准备训练数据和标签
            MatVector images = new MatVector(1);
            images.put(0, storedImage);
            
            // 创建标签矩阵并设置值
            Mat labelsMat = new Mat(1, 1, CV_32SC1);
            IntBuffer intBuf = labelsMat.createBuffer();
            intBuf.put(0, 1);
            
            // 训练模型
            recognizer.train(images, labelsMat);
            
            // 预测
            int[] label = new int[1];
            double[] confidence = new double[1];
            recognizer.predict(capturedImage, label, confidence);
            
            // LBPH置信度是反向的，值越低表示匹配度越高
            // 将其转换为0-1的相似度值
            double lbphSimilarity = Math.max(0.0, 1.0 - confidence[0] / 100.0);
            
            // 增加结构相似度计算
            double structuralSimilarity = calculateStructuralSimilarity(storedImageFloat, capturedImageFloat);
            
            // 清理本地资源
            storedPointer.close();
            capturedPointer.close();
            storedImageFloat.release();
            capturedImageFloat.release();
            
            // 综合相似度（LBPH和结构相似度的加权平均）
            double combinedSimilarity = 0.6 * lbphSimilarity + 0.4 * structuralSimilarity;
            
            logger.info("LBPH相似度: {}, 结构相似度: {}, 综合相似度: {}", 
                       lbphSimilarity, structuralSimilarity, combinedSimilarity);
            
            return combinedSimilarity;
        } catch (Exception e) {
            logger.error("计算人脸相似度过程中发生异常", e);
            return 0.0;
        }
    }
} 