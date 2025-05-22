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
    private static final CascadeClassifier faceDetector = loadFaceDetector();
    
    /**
     * 加载人脸检测器
     */
    private static CascadeClassifier loadFaceDetector() {
        logger.info("正在加载人脸检测器...");
        CascadeClassifier detector = new CascadeClassifier();
        
        try {
            // 1. 首先检查本地文件是否存在
            File cascadeFile = new File(CASCADE_PATH + CASCADE_FILE);
            if (cascadeFile.exists() && cascadeFile.length() > 0) {
                logger.info("从本地加载级联分类器: {}", cascadeFile.getAbsolutePath());
                if (detector.load(cascadeFile.getAbsolutePath())) {
                    logger.info("级联分类器加载成功");
                    return detector;
                } else {
                    logger.warn("从本地加载级联分类器失败");
                }
            } else {
                logger.info("本地级联分类器文件不存在: {}", cascadeFile.getAbsolutePath());
            }
            
            // 2. 尝试从资源目录加载
            File resourceDir = new File("./backend/src/main/resources/haarcascade");
            File resourceFile = new File(resourceDir, CASCADE_FILE);
            if (resourceFile.exists() && resourceFile.length() > 0) {
                logger.info("从资源目录加载级联分类器: {}", resourceFile.getAbsolutePath());
                if (detector.load(resourceFile.getAbsolutePath())) {
                    logger.info("资源目录级联分类器加载成功");
                    
                    // 如果本地目录不存在，复制到本地目录
                    if (!cascadeFile.exists()) {
                        try {
                            Files.createDirectories(Paths.get(CASCADE_PATH));
                            Files.copy(resourceFile.toPath(), cascadeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            logger.info("级联分类器已复制到数据目录: {}", cascadeFile.getAbsolutePath());
                        } catch (Exception e) {
                            logger.warn("复制级联分类器到数据目录失败", e);
                        }
                    }
                    
                    return detector;
                } else {
                    logger.warn("从资源目录加载级联分类器失败");
                }
            } else {
                logger.info("资源目录级联分类器文件不存在: {}", resourceFile.getAbsolutePath());
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
                        
                        // 如果本地目录不存在，复制到本地目录
                        if (!cascadeFile.exists()) {
                            try {
                                Files.createDirectories(Paths.get(CASCADE_PATH));
                                Files.copy(javaCVFile.toPath(), cascadeFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                logger.info("级联分类器已复制到数据目录: {}", cascadeFile.getAbsolutePath());
                            } catch (Exception e) {
                                logger.warn("复制级联分类器到数据目录失败", e);
                            }
                        }
                        
                        return detector;
                    } else {
                        logger.warn("从JavaCV资源加载级联分类器失败");
                    }
                } else {
                    logger.warn("JavaCV资源中找不到级联分类器文件");
                }
            } catch (Exception e) {
                logger.warn("访问JavaCV资源文件失败", e);
            }
            
            // 4. 所有尝试都失败，使用默认文件名（可能会在当前工作目录或系统目录查找）
            logger.warn("尝试使用默认文件名加载级联分类器");
            if (detector.load(CASCADE_FILE)) {
                logger.info("使用默认文件名加载成功");
                return detector;
            }
            
            // 5. 所有尝试都失败
            logger.error("无法加载人脸检测器，请确保级联分类器文件存在于以下位置之一：" +
                     "\n1. " + CASCADE_PATH + CASCADE_FILE +
                     "\n2. ./backend/src/main/resources/haarcascade/" + CASCADE_FILE);
            
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
        try {
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
            
            // 检测人脸
            RectVector faceDetections = new RectVector();
            faceDetector.detectMultiScale(image, faceDetections);
            
            if (faceDetections.empty() || faceDetections.size() <= 0) {
                logger.warn("未检测到人脸");
                return null;
            }
            
            // 提取最大的人脸
            Rect maxFace = getMaxFace(faceDetections);
            
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
            // 使用JavaCV 1.5.9兼容的方法
            String tempFileName = Loader.getTempDir() + "/face_" + System.currentTimeMillis() + ".jpg";
            
            // 将矩阵保存为JPG格式的图像文件
            imwrite(tempFileName, equalizedFace);
            
            // 读取临时文件到字节数组
            byte[] resultArray = Files.readAllBytes(Paths.get(tempFileName));
            
            // 删除临时文件
            new File(tempFileName).delete();
            
            return Base64.getEncoder().encodeToString(resultArray);
        } catch (Exception e) {
            logger.error("人脸检测过程中发生异常", e);
            return null;
        }
    }
    
    /**
     * 比较两个人脸是否匹配
     * @param storedFaceBase64 存储的人脸数据（Base64编码）
     * @param capturedFaceBase64 当前捕获的人脸数据（Base64编码）
     * @return 如果匹配返回true，否则返回false
     */
    public static boolean compareFaces(String storedFaceBase64, String capturedFaceBase64) {
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
                return false;
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
            
            // 创建标签矩阵并设置值 - 创建一个带有单个元素的Mat，值为1
            Mat labelsMat = new Mat(1, 1, CV_32SC1);
            // 使用JavaCV 1.5.9兼容方式设置值
            IntBuffer intBuf = labelsMat.createBuffer();
            intBuf.put(0, 1);
            
            // 训练模型
            recognizer.train(images, labelsMat);
            
            // 预测 - 使用JavaCV 1.5.9版本兼容的方法
            int[] label = new int[1];
            double[] confidence = new double[1];
            recognizer.predict(capturedImage, label, confidence);
            
            // 置信度阈值（较低的值表示更高的匹配度）
            // 阈值越低，匹配要求越严格，值范围通常为0-100
            // 40.0是一个非常严格的阈值，可以大幅提高准确率，确保只有本人人脸才能通过验证
            // 原来的70.0和60.0阈值太宽松，导致误识别率高
            double threshold = 40.0; // 大幅降低阈值，提高匹配严格程度
            double confidenceValue = confidence[0];
            logger.info("人脸匹配置信度: {}", confidenceValue);
            
            // 增加额外的相似度检查来提高安全性
            double imageSimilarity = calculateStructuralSimilarity(storedImageFloat, capturedImageFloat);
            logger.info("人脸结构相似度: {}", imageSimilarity);
            
            // 清理本地资源
            storedPointer.close();
            capturedPointer.close();
            storedImageFloat.release();
            capturedImageFloat.release();
            
            boolean lbphResult = confidenceValue < threshold;
            // 对于简化的相似度计算方法，阈值应该更高
            boolean ssimResult = imageSimilarity > 0.75;
            
            logger.info("LBPH验证结果: {}, SSIM验证结果: {}", lbphResult, ssimResult);
            
            // 必须同时满足LBPH算法的置信度阈值和结构相似度阈值
            return lbphResult && ssimResult;
        } catch (Exception e) {
            logger.error("人脸比较过程中发生异常", e);
            return false;
        }
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
} 