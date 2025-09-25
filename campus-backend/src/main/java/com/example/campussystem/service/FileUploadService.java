package com.example.campussystem.service;

import com.example.campussystem.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务
 */
@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    @Value("${file.upload.max-size:20971520}")
    private long maxFileSize;

    @Value("${file.upload.allowed-types:jpg,jpeg,png,gif,webp}")
    private String allowedTypes;

    // 支持的图片格式
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    );

    // 最大图片尺寸
    private static final int MAX_IMAGE_WIDTH = 2048;
    private static final int MAX_IMAGE_HEIGHT = 2048;

    /**
     * 上传图片文件
     */
    public String uploadImage(MultipartFile file, String category) {
        // 验证文件
        validateFile(file);
        
        // 验证图片格式
        validateImageFile(file);
        
        try {
            // 创建上传目录
            String categoryPath = createUploadDirectory(category);
            
            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());
            
            // 完整文件路径
            Path filePath = Paths.get(categoryPath, fileName);
            
            // 处理图片（压缩、调整尺寸）
            BufferedImage processedImage = processImage(file);
            
            // 保存处理后的图片
            String fileExtension = getFileExtension(file.getOriginalFilename());
            ImageIO.write(processedImage, fileExtension, filePath.toFile());
            
            // 返回相对路径
            String relativePath = category + "/" + fileName;
            logger.info("文件上传成功: {}", relativePath);
            
            return relativePath;
            
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传普通文件
     */
    public String uploadFile(MultipartFile file, String category) {
        // 验证文件
        validateFile(file);
        
        try {
            // 创建上传目录
            String categoryPath = createUploadDirectory(category);
            
            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());
            
            // 完整文件路径
            Path filePath = Paths.get(categoryPath, fileName);
            
            // 保存文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 返回相对路径
            String relativePath = category + "/" + fileName;
            logger.info("文件上传成功: {}", relativePath);
            
            return relativePath;
            
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String filePath) {
        try {
            Path fullPath = Paths.get(uploadPath, filePath);
            boolean deleted = Files.deleteIfExists(fullPath);
            
            if (deleted) {
                logger.info("文件删除成功: {}", filePath);
            } else {
                logger.warn("文件不存在或删除失败: {}", filePath);
            }
            
            return deleted;
        } catch (IOException e) {
            logger.error("文件删除失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 获取文件信息
     */
    public FileInfo getFileInfo(String filePath) {
        try {
            Path fullPath = Paths.get(uploadPath, filePath);
            
            if (!Files.exists(fullPath)) {
                return null;
            }
            
            File file = fullPath.toFile();
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(file.getName());
            fileInfo.setFilePath(filePath);
            fileInfo.setFileSize(file.length());
            fileInfo.setLastModified(LocalDateTime.ofEpochSecond(
                file.lastModified() / 1000, 0, 
                java.time.ZoneOffset.systemDefault().getRules().getOffset(LocalDateTime.now())
            ));
            
            // 如果是图片，获取图片尺寸
            if (isImageFile(filePath)) {
                try {
                    BufferedImage image = ImageIO.read(file);
                    if (image != null) {
                        fileInfo.setWidth(image.getWidth());
                        fileInfo.setHeight(image.getHeight());
                    }
                } catch (IOException e) {
                    logger.warn("获取图片尺寸失败: {}", filePath, e);
                }
            }
            
            return fileInfo;
            
        } catch (Exception e) {
            logger.error("获取文件信息失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小超过限制: " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        
        if (!ALLOWED_IMAGE_TYPES.contains(fileExtension.toLowerCase())) {
            throw new BusinessException("不支持的图片格式，支持的格式: " + String.join(", ", ALLOWED_IMAGE_TYPES));
        }
        
        // 验证文件内容是否为图片
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new BusinessException("文件不是有效的图片格式");
            }
        } catch (IOException e) {
            throw new BusinessException("读取图片文件失败");
        }
    }

    /**
     * 处理图片（压缩、调整尺寸）
     */
    private BufferedImage processImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        if (originalImage == null) {
            throw new IOException("无法读取图片文件");
        }
        
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // 如果图片尺寸在限制范围内，直接返回
        if (originalWidth <= MAX_IMAGE_WIDTH && originalHeight <= MAX_IMAGE_HEIGHT) {
            return originalImage;
        }
        
        // 计算缩放比例
        double scaleWidth = (double) MAX_IMAGE_WIDTH / originalWidth;
        double scaleHeight = (double) MAX_IMAGE_HEIGHT / originalHeight;
        double scale = Math.min(scaleWidth, scaleHeight);
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        
        // 创建缩放后的图片
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        
        // 设置高质量渲染
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        logger.info("图片已压缩: {}x{} -> {}x{}", originalWidth, originalHeight, newWidth, newHeight);
        
        return scaledImage;
    }

    /**
     * 创建上传目录
     */
    private String createUploadDirectory(String category) throws IOException {
        // 直接在category目录下创建，不使用日期子目录
        String categoryPath = Paths.get(uploadPath, category).toString();
        
        Path path = Paths.get(categoryPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        
        return categoryPath;
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String originalFilename) {
        String fileExtension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + fileExtension;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String filePath) {
        String extension = getFileExtension(filePath);
        return ALLOWED_IMAGE_TYPES.contains(extension.toLowerCase());
    }

    /**
     * 文件信息类
     */
    public static class FileInfo {
        private String fileName;
        private String filePath;
        private long fileSize;
        private LocalDateTime lastModified;
        private Integer width;
        private Integer height;

        // Getters and Setters
        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public LocalDateTime getLastModified() {
            return lastModified;
        }

        public void setLastModified(LocalDateTime lastModified) {
            this.lastModified = lastModified;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }
    }
}