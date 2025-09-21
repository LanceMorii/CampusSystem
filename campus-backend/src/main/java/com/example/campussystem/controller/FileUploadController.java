package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.security.UserPrincipal;
import com.example.campussystem.service.FileUploadService;
import com.example.campussystem.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/files")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private SystemLogService systemLogService;

    /**
     * 上传单个图片
     */
    @PostMapping("/upload/image")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "products") String category,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        try {
            String filePath = fileUploadService.uploadImage(file, category);
            
            // 记录操作日志
            systemLogService.logOperation(
                userPrincipal.getUserId().toString(),
                "上传图片",
                "分类: " + category + ", 文件: " + file.getOriginalFilename()
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("filePath", filePath);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("url", "/api/files/view/" + filePath);
            
            return ResponseEntity.ok(ApiResponse.success("图片上传成功", result));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("图片上传失败: " + e.getMessage()));
        }
    }

    /**
     * 批量上传图片
     */
    @PostMapping("/upload/images")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> uploadImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(defaultValue = "products") String category,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        List<Map<String, Object>> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String filePath = fileUploadService.uploadImage(file, category);
                
                Map<String, Object> result = new HashMap<>();
                result.put("filePath", filePath);
                result.put("fileName", file.getOriginalFilename());
                result.put("fileSize", file.getSize());
                result.put("url", "/api/files/view/" + filePath);
                result.put("success", true);
                
                results.add(result);
                
            } catch (Exception e) {
                Map<String, Object> result = new HashMap<>();
                result.put("fileName", file.getOriginalFilename());
                result.put("error", e.getMessage());
                result.put("success", false);
                
                results.add(result);
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }
        
        // 记录操作日志
        systemLogService.logOperation(
            userPrincipal.getUserId().toString(),
            "批量上传图片",
            "分类: " + category + ", 文件数量: " + files.length + ", 成功: " + (files.length - errors.size())
        );
        
        if (errors.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("所有图片上传成功", results));
        } else {
            return ResponseEntity.ok(ApiResponse.success("部分图片上传成功", results));
        }
    }

    /**
     * 上传普通文件
     */
    @PostMapping("/upload/file")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "documents") String category,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        
        try {
            String filePath = fileUploadService.uploadFile(file, category);
            
            // 记录操作日志
            systemLogService.logOperation(
                userPrincipal.getUserId().toString(),
                "上传文件",
                "分类: " + category + ", 文件: " + file.getOriginalFilename()
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("filePath", filePath);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("url", "/api/files/download/" + filePath);
            
            return ResponseEntity.ok(ApiResponse.success("文件上传成功", result));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("文件上传失败: " + e.getMessage()));
        }
    }

    /**
     * 查看图片文件
     */
    @GetMapping("/view/**")
    public ResponseEntity<Resource> viewFile(HttpServletRequest request) {
        try {
            // 获取文件路径
            String filePath = extractFilePath(request, "/api/files/view/");
            
            // 获取文件资源
            Path file = Paths.get("uploads").resolve(filePath);
            Resource resource = new UrlResource(file.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            // 确定文件类型
            String contentType = determineContentType(filePath);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(resource);
                    
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        try {
            // 获取文件路径
            String filePath = extractFilePath(request, "/api/files/download/");
            
            // 获取文件资源
            Path file = Paths.get("uploads").resolve(filePath);
            Resource resource = new UrlResource(file.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取原始文件名
            String fileName = file.getFileName().toString();
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
                    
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{category}/{dateDir}/{fileName}")
    public ResponseEntity<ApiResponse<String>> deleteFile(
            @PathVariable String category,
            @PathVariable String dateDir,
            @PathVariable String fileName,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        String filePath = category + "/" + dateDir + "/" + fileName;
        boolean deleted = fileUploadService.deleteFile(filePath);
        
        if (deleted) {
            // 记录操作日志
            systemLogService.logOperation(
                userPrincipal.getUserId().toString(),
                "删除文件",
                "文件路径: " + filePath
            );
            
            return ResponseEntity.ok(ApiResponse.success("文件删除成功"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("文件删除失败"));
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{category}/{dateDir}/{fileName}")
    public ResponseEntity<ApiResponse<FileUploadService.FileInfo>> getFileInfo(
            @PathVariable String category,
            @PathVariable String dateDir,
            @PathVariable String fileName) {
        
        String filePath = category + "/" + dateDir + "/" + fileName;
        FileUploadService.FileInfo fileInfo = fileUploadService.getFileInfo(filePath);
        
        if (fileInfo != null) {
            return ResponseEntity.ok(ApiResponse.success("获取文件信息成功", fileInfo));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 提取文件路径
     */
    private String extractFilePath(HttpServletRequest request, String prefix) {
        String requestURI = request.getRequestURI();
        return requestURI.substring(prefix.length());
    }

    /**
     * 确定文件内容类型
     */
    private String determineContentType(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();
        
        switch (extension) {
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
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default:
                return "application/octet-stream";
        }
    }
}