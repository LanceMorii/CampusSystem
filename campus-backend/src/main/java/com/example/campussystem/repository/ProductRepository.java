package com.example.campussystem.repository;

import com.example.campussystem.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 商品数据访问接口
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 根据用户ID查询商品列表
     */
    List<Product> findByUserIdAndStatusNot(Long userId, Integer status);

    /**
     * 根据用户ID和状态查询商品列表（分页）
     */
    Page<Product> findByUserIdAndStatusNot(Long userId, Integer status, Pageable pageable);

    /**
     * 根据用户ID和状态查询商品列表（分页）
     */
    Page<Product> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);

    /**
     * 根据用户ID、状态和标题查询商品列表（分页）
     */
    Page<Product> findByUserIdAndStatusAndTitleContainingIgnoreCase(Long userId, Integer status, String title, Pageable pageable);

    /**
     * 根据用户ID、标题查询商品列表，排除指定状态（分页）
     */
    Page<Product> findByUserIdAndTitleContainingIgnoreCaseAndStatusNot(Long userId, String title, Integer status, Pageable pageable);

    /**
     * 根据分类ID查询商品列表（分页）
     */
    Page<Product> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable);

    /**
     * 根据状态查询商品列表（分页）
     */
    Page<Product> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据标题关键词搜索商品（分页）
     */
    Page<Product> findByTitleContainingIgnoreCaseAndStatus(String keyword, Integer status, Pageable pageable);

    /**
     * 根据价格区间查询商品（分页）
     */
    Page<Product> findByPriceBetweenAndStatus(BigDecimal minPrice, BigDecimal maxPrice, Integer status, Pageable pageable);

    /**
     * 综合搜索：关键词 + 分类 + 价格区间
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "p.status = :status")
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                @Param("categoryId") Long categoryId,
                                @Param("minPrice") BigDecimal minPrice,
                                @Param("maxPrice") BigDecimal maxPrice,
                                @Param("status") Integer status,
                                Pageable pageable);

    /**
     * 根据用户ID和商品ID查询商品（用于权限验证）
     */
    Optional<Product> findByIdAndUserId(Long id, Long userId);

    /**
     * 查询热门商品（按浏览量排序）
     */
    @Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY p.viewCount DESC")
    Page<Product> findPopularProducts(@Param("status") Integer status, Pageable pageable);

    /**
     * 查询最新商品（按创建时间排序）
     */
    @Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY p.createTime DESC")
    Page<Product> findLatestProducts(@Param("status") Integer status, Pageable pageable);

    /**
     * 统计用户发布的商品数量
     */
    long countByUserIdAndStatusNot(Long userId, Integer status);

    /**
     * 统计分类下的商品数量
     */
    long countByCategoryIdAndStatus(Long categoryId, Integer status);

    /**
     * 查询推荐商品（同分类的其他商品）
     */
    @Query("SELECT p FROM Product p WHERE p.categoryId = :categoryId AND p.id != :excludeId AND p.status = :status ORDER BY p.viewCount DESC")
    List<Product> findRecommendedProducts(@Param("categoryId") Long categoryId, 
                                         @Param("excludeId") Long excludeId, 
                                         @Param("status") Integer status, 
                                         Pageable pageable);

    /**
     * 根据状态统计商品数量
     */
    long countByStatus(Integer status);

    /**
     * 统计用户指定状态的商品数量
     */
    long countByUserIdAndStatus(Long userId, Integer status);

    /**
     * 根据标题关键词搜索商品（不限状态）
     */
    Page<Product> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    /**
     * 根据状态和更新时间查询商品
     */
    List<Product> findByStatusAndUpdateTimeBefore(Integer status, LocalDateTime updateTime);
}