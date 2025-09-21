package com.example.campussystem.controller;

import com.example.campussystem.dto.CategoryRequest;
import com.example.campussystem.dto.CategoryResponse;
import com.example.campussystem.entity.Category;
import com.example.campussystem.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品分类控制器
 */
@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有分类列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllActiveCategories();
            List<CategoryResponse> categoryResponses = CategoryResponse.fromList(categories);
            
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "获取分类列表成功",
                    "data", categoryResponses,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "获取分类列表失败: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 获取分类树结构
     */
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getCategoryTree() {
        try {
            List<Category> categoryTree = categoryService.getCategoryTree();
            List<CategoryResponse> categoryResponses = CategoryResponse.fromList(categoryTree);
            
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "获取分类树成功",
                    "data", categoryResponses,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "获取分类树失败: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 获取顶级分类列表
     */
    @GetMapping("/top")
    public ResponseEntity<Map<String, Object>> getTopLevelCategories() {
        try {
            List<Category> categories = categoryService.getTopLevelCategories();
            List<CategoryResponse> categoryResponses = CategoryResponse.fromList(categories);
            
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "获取顶级分类成功",
                    "data", categoryResponses,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "获取顶级分类失败: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 根据父分类ID获取子分类
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<Map<String, Object>> getCategoriesByParentId(@PathVariable Long parentId) {
        try {
            List<Category> categories = categoryService.getCategoriesByParentId(parentId);
            List<CategoryResponse> categoryResponses = CategoryResponse.fromList(categories);
            
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "获取子分类成功",
                    "data", categoryResponses,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "获取子分类失败: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 根据ID获取分类详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id) {
        try {
            Category category = categoryService.getCategoryById(id);
            CategoryResponse categoryResponse = CategoryResponse.from(category);
            
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "获取分类详情成功",
                    "data", categoryResponse,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "获取分类详情失败: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 创建分类（需要管理员权限）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createCategory(@Valid @RequestBody CategoryRequest request) {
        try {
            Category category = categoryService.createCategory(
                    request.getName(),
                    request.getParentId(),
                    request.getSortOrder()
            );
            CategoryResponse categoryResponse = CategoryResponse.from(category);
            
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "创建分类成功",
                    "data", categoryResponse,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "创建分类失败: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 更新分类（需要管理员权限）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        try {
            Category category = categoryService.updateCategory(
                    id,
                    request.getName(),
                    request.getParentId(),
                    request.getSortOrder(),
                    request.getStatus()
            );
            CategoryResponse categoryResponse = CategoryResponse.from(category);
            
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "更新分类成功",
                    "data", categoryResponse,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "更新分类失败: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 删除分类（需要管理员权限）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "删除分类成功",
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "删除分类失败: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 切换分类状态（需要管理员权限）
     */
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleCategoryStatus(@PathVariable Long id) {
        try {
            Category category = categoryService.toggleCategoryStatus(id);
            CategoryResponse categoryResponse = CategoryResponse.from(category);
            
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "切换分类状态成功",
                    "data", categoryResponse,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "切换分类状态失败: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 批量删除分类（需要管理员权限）
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCategoriesBatch(@RequestBody List<Long> ids) {
        try {
            categoryService.deleteCategoriesBatch(ids);
            
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "批量删除分类成功",
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "批量删除分类失败: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * 初始化基础分类数据（需要管理员权限）
     */
    @PostMapping("/init")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> initializeBasicCategories() {
        try {
            categoryService.initializeBasicCategories();
            
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "初始化基础分类数据成功",
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "初始化基础分类数据失败: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }
}