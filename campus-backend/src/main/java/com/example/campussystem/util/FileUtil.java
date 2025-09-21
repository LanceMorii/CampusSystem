package com.example.campussystem.util;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * 文件工具类
 */
public class FileUtil {

    // 支持的图片格式
    public static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    );

    // 支持的文档格式
    public static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"
    );

    // 支持的视频格式
    public static final List<String> VIDEO_EXTENSIONS = Arrays.asList(
            "mp4", "avi", "mov", "wmv", "flv", "webm"
    );

    // 支持的音频格式
    public static final List<String> AUDIO_EXTENSIONS = Arrays.asList(
            "mp3", "wav", "flac", "aac", "ogg"
    );

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 获取文件名（不包含扩展名）
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    /**
     * 判断是否为图片文件
     */
    public static boolean isImageFile(String fileName) {
        String extension = getFileExtension(fileName);
        return IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为文档文件
     */
    public static boolean isDocumentFile(String fileName) {
        String extension = getFileExtension(fileName);
        return DOCUMENT_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为视频文件
     */
    public static boolean isVideoFile(String fileName) {
        String extension = getFileExtension(fileName);
        return VIDEO_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为音频文件
     */
    public static boolean isAudioFile(String fileName) {
        String extension = getFileExtension(fileName);
        return AUDIO_EXTENSIONS.contains(extension);
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 验证文件名是否安全
     */
    public static boolean isSafeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否包含危险字符
        String[] dangerousChars = {"..", "/", "\\", ":", "*", "?", "\"", "<", ">", "|"};
        for (String dangerousChar : dangerousChars) {
            if (fileName.contains(dangerousChar)) {
                return false;
            }
        }
        
        // 检查是否为系统保留名称
        String[] reservedNames = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", 
                                 "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", 
                                 "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
        String nameWithoutExt = getFileNameWithoutExtension(fileName).toUpperCase();
        for (String reservedName : reservedNames) {
            if (nameWithoutExt.equals(reservedName)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 计算文件MD5值
     */
    public static String calculateMD5(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = file.getBytes();
        byte[] digest = md.digest(bytes);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString();
    }

    /**
     * 计算文件MD5值
     */
    public static String calculateMD5(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] bytes = Files.readAllBytes(file.toPath());
        byte[] digest = md.digest(bytes);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString();
    }

    /**
     * 获取图片尺寸
     */
    public static int[] getImageDimensions(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        if (image != null) {
            return new int[]{image.getWidth(), image.getHeight()};
        }
        return null;
    }

    /**
     * 获取图片尺寸
     */
    public static int[] getImageDimensions(MultipartFile imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile.getInputStream());
        if (image != null) {
            return new int[]{image.getWidth(), image.getHeight()};
        }
        return null;
    }

    /**
     * 创建目录
     */
    public static boolean createDirectories(String path) {
        try {
            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查文件是否存在
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 获取文件大小
     */
    public static long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * 获取MIME类型
     */
    public static String getMimeType(String fileName) {
        String extension = getFileExtension(fileName);
        
        switch (extension) {
            // 图片
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "bmp":
                return "image/bmp";
            case "svg":
                return "image/svg+xml";
            
            // 文档
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";
            
            // 视频
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";
            case "flv":
                return "video/x-flv";
            case "webm":
                return "video/webm";
            
            // 音频
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "flac":
                return "audio/flac";
            case "aac":
                return "audio/aac";
            case "ogg":
                return "audio/ogg";
            
            default:
                return "application/octet-stream";
        }
    }
}