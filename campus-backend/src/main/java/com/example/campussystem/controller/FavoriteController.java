package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.dto.ProductResponse;
import com.example.campussystem.security.UserPrincipal;
import com.example.campussystem.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<String>> addFavorite(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        favoriteService.addFavorite(userPrincipal.getUserId(), productId);
        return ResponseEntity.ok(ApiResponse.success("收藏成功"));
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<String>> removeFavorite(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        favoriteService.removeFavorite(userPrincipal.getUserId(), productId);
        return ResponseEntity.ok(ApiResponse.success("取消收藏成功"));
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/{productId}/check")
    public ResponseEntity<ApiResponse<Boolean>> checkFavorite(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        boolean isFavorite = favoriteService.isFavorite(userPrincipal.getUserId(), productId);
        return ResponseEntity.ok(ApiResponse.success("检查收藏状态成功", isFavorite));
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getUserFavorites(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        List<ProductResponse> favorites = favoriteService.getUserFavorites(userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("获取收藏列表成功", favorites));
    }

    /**
     * 统计用户收藏数量
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countUserFavorites(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        long count = favoriteService.countUserFavorites(userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("获取收藏数量成功", count));
    }
}