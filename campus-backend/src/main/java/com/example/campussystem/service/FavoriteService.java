package com.example.campussystem.service;

import com.example.campussystem.dto.ProductResponse;
import com.example.campussystem.entity.Favorite;
import com.example.campussystem.entity.Product;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.repository.FavoriteRepository;
import com.example.campussystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务类
 */
@Service
@Transactional
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * 添加收藏
     */
    public void addFavorite(Long userId, Long productId) {
        // 检查商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 检查是否已经收藏
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BusinessException("商品已在收藏列表中");
        }

        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setCreateTime(LocalDateTime.now());

        favoriteRepository.save(favorite);
    }

    /**
     * 取消收藏
     */
    public void removeFavorite(Long userId, Long productId) {
        Favorite favorite = favoriteRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new BusinessException("收藏记录不存在"));

        favoriteRepository.delete(favorite);
    }

    /**
     * 检查是否已收藏
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    /**
     * 获取用户收藏列表
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getUserFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreateTimeDesc(userId);
        
        return favorites.stream()
                .map(favorite -> {
                    Product product = productRepository.findById(favorite.getProductId()).orElse(null);
                    if (product != null && product.getStatus() == 1) { // 只返回上架的商品
                        return new ProductResponse(product);
                    }
                    return null;
                })
                .filter(productResponse -> productResponse != null)
                .collect(Collectors.toList());
    }

    /**
     * 统计用户收藏数量
     */
    @Transactional(readOnly = true)
    public long countUserFavorites(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }

    /**
     * 统计商品被收藏次数
     */
    @Transactional(readOnly = true)
    public long countProductFavorites(Long productId) {
        return favoriteRepository.countByProductId(productId);
    }
}