package com.example.campussystem.repository;

import com.example.campussystem.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 商品分类数据访问层
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 根据状态查询分类列表
     */
    List<Category> findByStatusOrderBySortOrderAsc(Integer status);

    /**
     * 根据父分类ID查询子分类
     */
    List<Category> findByParentIdAndStatusOrderBySortOrderAsc(Long parentId, Integer status);

    /**
     * 根据分类名称查询（用于检查重复）
     */
    Optional<Category> findByNameAndParentId(String name, Long parentId);

    /**
     * 查询所有启用的顶级分类（父分类ID为0）
     */
    @Query("SELECT c FROM Category c WHERE c.parentId = 0 AND c.status = :status ORDER BY c.sortOrder ASC")
    List<Category> findTopLevelCategories(@Param("status") Integer status);

    /**
     * 根据父分类ID查询子分类（不限状态）
     */
    List<Category> findByParentIdOrderBySortOrderAsc(Long parentId);

    /**
     * 检查是否存在子分类
     */
    boolean existsByParentId(Long parentId);

    /**
     * 检查分类下是否有商品
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.category.id = :categoryId")
    boolean hasProducts(@Param("categoryId") Long categoryId);

    /**
     * 获取指定父分类下的最大排序值
     */
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM Category c WHERE c.parentId = :parentId")
    Integer getMaxSortOrder(@Param("parentId") Long parentId);

    /**
     * 查询分类及其子分类的数量
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.parentId = :categoryId AND c.status = 1")
    Long countSubCategories(@Param("categoryId") Long categoryId);

    /**
     * 检查分类名称是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND c.parentId = :parentId AND c.id != :excludeId")
    boolean existsByNameAndParentIdExcludingId(@Param("name") String name, @Param("parentId") Long parentId, @Param("excludeId") Long excludeId);

    /**
     * 获取分类商品统计
     */
    @Query("SELECT new map(c.id as categoryId, c.name as categoryName, COUNT(p) as productCount) " +
           "FROM Category c LEFT JOIN Product p ON c.id = p.categoryId AND p.status = 1 " +
           "GROUP BY c.id, c.name ORDER BY COUNT(p) DESC")
    List<Map<String, Object>> getCategoryProductCounts();
}