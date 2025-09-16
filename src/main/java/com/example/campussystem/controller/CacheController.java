package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.service.CacheEvictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 缓存管理控制器
 */
@RestController
@RequestMapping("/api/cache")
@PreAuthorize("hasRole('ADMIN')")
public class CacheController {

    @Autowired
    private CacheEvictionService cacheEvictionService;

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<CacheEvictionService.CacheStatistics>> getCacheStatistics() {
        CacheEvictionService.CacheStatistics statistics = cacheEvictionService.getCacheStatistics();
        return ResponseEntity.ok(ApiResponse.success("获取缓存统计信息成功", statistics));
    }

    /**
     * 清理所有缓存
     */
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<String>> clearAllCache() {
        cacheEvictionService.clearAllCache();
        return ResponseEntity.ok(ApiResponse.success("所有缓存清理成功"));
    }

    /**
     * 清理商品相关缓存
     */
    @DeleteMapping("/products")
    public ResponseEntity<ApiResponse<String>> clearProductCaches() {
        cacheEvictionService.cleanProductCaches();
        return ResponseEntity.ok(ApiResponse.success("商品缓存清理成功"));
    }

    /**
     * 清理用户相关缓存
     */
    @DeleteMapping("/users")
    public ResponseEntity<ApiResponse<String>> clearUserCaches() {
        cacheEvictionService.cleanUserCaches();
        return ResponseEntity.ok(ApiResponse.success("用户缓存清理成功"));
    }

    /**
     * 清理分类相关缓存
     */
    @DeleteMapping("/categories")
    public ResponseEntity<ApiResponse<String>> clearCategoryCaches() {
        cacheEvictionService.cleanCategoryCaches();
        return ResponseEntity.ok(ApiResponse.success("分类缓存清理成功"));
    }

    /**
     * 根据模式清理缓存
     */
    @DeleteMapping("/pattern")
    public ResponseEntity<ApiResponse<String>> clearCacheByPattern(@RequestParam String pattern) {
        cacheEvictionService.clearCacheByPattern(pattern);
        return ResponseEntity.ok(ApiResponse.success("模式缓存清理成功"));
    }

    /**
     * 预热缓存
     */
    @PostMapping("/warmup")
    public ResponseEntity<ApiResponse<String>> warmUpCache() {
        cacheEvictionService.warmUpCache();
        return ResponseEntity.ok(ApiResponse.success("缓存预热成功"));
    }

    /**
     * 手动触发缓存清理任务
     */
    @PostMapping("/clean")
    public ResponseEntity<ApiResponse<String>> manualCleanCache() {
        cacheEvictionService.cleanExpiredCache();
        return ResponseEntity.ok(ApiResponse.success("手动缓存清理完成"));
    }
}