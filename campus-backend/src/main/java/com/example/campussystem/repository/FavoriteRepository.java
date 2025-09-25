package com.example.campussystem.repository;

import com.example.campussystem.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 收藏数据访问接口
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /**
     * 根据用户ID和商品ID查找收藏记录
     */
    Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * 检查用户是否收藏了某商品
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /**
     * 根据用户ID查找收藏列表（按创建时间倒序）
     */
    List<Favorite> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 根据商品ID查找收藏列表
     */
    List<Favorite> findByProductId(Long productId);

    /**
     * 统计用户收藏数量
     */
    long countByUserId(Long userId);

    /**
     * 统计商品被收藏次数
     */
    long countByProductId(Long productId);

    /**
     * 删除用户的所有收藏
     */
    void deleteByUserId(Long userId);

    /**
     * 删除商品的所有收藏记录
     */
    void deleteByProductId(Long productId);
}