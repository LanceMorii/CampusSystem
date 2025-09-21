package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.dto.UserLoginRequest;
import com.example.campussystem.dto.UserLoginResponse;
import com.example.campussystem.dto.UserProfileResponse;
import com.example.campussystem.dto.UserRegisterRequest;
import com.example.campussystem.dto.UserRegisterResponse;
import com.example.campussystem.dto.UserUpdateRequest;
import com.example.campussystem.service.UserService;
import com.example.campussystem.util.JwtUtil;
import com.example.campussystem.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 提供用户认证相关的API接口
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRegisterResponse>> register(@Valid @RequestBody UserRegisterRequest request) {
        UserRegisterResponse response = userService.register(request);
        return ResponseEntity.ok(ApiResponse.success("注册成功", response));
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("AuthController is working"));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(@Valid @RequestBody UserLoginRequest request) {
        UserLoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }

    /**
     * 测试JWT生成（临时接口，用于验证JWT功能）
     */
    @PostMapping("/test-jwt")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testJwt() {
        // 生成测试token
        String token = jwtUtil.generateToken(1L, "20240001", "testuser");
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", jwtUtil.getUserIdFromToken(token));
        result.put("studentId", jwtUtil.getStudentIdFromToken(token));
        result.put("username", jwtUtil.getUsernameFromToken(token));
        result.put("role", jwtUtil.getRoleFromToken(token));
        result.put("isValid", jwtUtil.validateToken(token));
        
        return ResponseEntity.ok(ApiResponse.success("JWT测试成功", result));
    }

    /**
     * 获取当前用户详细信息（需要认证）
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("用户未登录"));
        }
        
        UserProfileResponse profile = userService.getUserProfile(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", profile));
    }

    /**
     * 更新当前用户信息（需要认证）
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("用户未登录"));
        }
        
        UserProfileResponse updatedProfile = userService.updateUserProfile(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", updatedProfile));
    }

    /**
     * 管理员测试接口
     */
    @GetMapping("/admin-test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adminTest() {
        return ResponseEntity.ok(ApiResponse.success("管理员权限验证成功"));
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("无效的token"));
        }
        
        String newToken = jwtUtil.refreshToken(token);
        if (newToken == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.badRequest("token刷新失败"));
        }
        
        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);
        
        return ResponseEntity.ok(ApiResponse.success("token刷新成功", result));
    }
}