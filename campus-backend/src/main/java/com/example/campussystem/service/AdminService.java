package com.example.campussystem.service;

import com.example.campussystem.dto.ProductResponse;
import com.example.campussystem.dto.UserProfileResponse;
import com.example.campussystem.entity.Product;
import com.example.campussystem.entity.User;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.repository.CategoryRepository;
import com.example.campussystem.repository.OrderRepository;
import com.example.campussystem.repository.ProductRepository;
import com.example.campussystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务类
 */
@Service
@Transactional
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SystemLogService systemLogService;

    /**
     * 获取系统统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 用户统计
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(1);
        long newUsersToday = userRepository.countByCreateTimeAfter(LocalDateTime.now().minusDays(1));
        
        // 商品统计
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByStatus(1);
        long soldProducts = productRepository.countByStatus(3);
        
        // 订单统计
        long totalOrders = orderRepository.count();
        long completedOrders = orderRepository.countByStatus(3);
        BigDecimal totalAmount = orderRepository.sumAmountByStatus(3);
        
        // 分类统计
        long totalCategories = categoryRepository.count();
        
        statistics.put("users", Map.of(
            "total", totalUsers,
            "active", activeUsers,
            "newToday", newUsersToday
        ));
        
        statistics.put("products", Map.of(
            "total", totalProducts,
            "active", activeProducts,
            "sold", soldProducts
        ));
        
        statistics.put("orders", Map.of(
            "total", totalOrders,
            "completed", completedOrders,
            "totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO
        ));
        
        statistics.put("categories", Map.of(
            "total", totalCategories
        ));
        
        return statistics;
    }

    /**
     * 获取用户列表（分页）
     */
    @Transactional(readOnly = true)
    public Page<UserProfileResponse> getUserList(int page, int size, String sortBy, String sortDir, String keyword) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.findByUsernameContainingIgnoreCaseOrRealNameContainingIgnoreCaseOrStudentIdContaining(
                keyword, keyword, keyword, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        return users.map(user -> {
            UserProfileResponse response = new UserProfileResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setStudentId(user.getStudentId());
            response.setRealName(user.getRealName());
            response.setPhone(user.getPhone());
            response.setEmail(user.getEmail());
            response.setAvatar(user.getAvatar());
            response.setStatus(user.getStatus());
            response.setRole(user.getRole());
            response.setCreateTime(user.getCreateTime());
            response.setUpdateTime(user.getUpdateTime());
            return response;
        });
    }

    /**
     * 更新用户状态
     */
    public void updateUserStatus(Long userId, Integer status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (status < 0 || status > 1) {
            throw new BusinessException("用户状态值无效");
        }
        
        user.setStatus(status);
        userRepository.save(user);
    }

    /**
     * 获取商品列表（分页，包含已删除）
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductList(int page, int size, String sortBy, String sortDir, 
                                              String keyword, Integer status) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (status != null) {
                products = productRepository.findByTitleContainingIgnoreCaseAndStatus(keyword, status, pageable);
            } else {
                products = productRepository.findByTitleContainingIgnoreCase(keyword, pageable);
            }
        } else {
            if (status != null) {
                products = productRepository.findByStatus(status, pageable);
            } else {
                products = productRepository.findAll(pageable);
            }
        }
        
        return products.map(ProductResponse::new);
    }

    /**
     * 更新商品状态
     */
    public void updateProductStatus(Long productId, Integer status) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        
        if (status < 0 || status > 3) {
            throw new BusinessException("商品状态值无效");
        }
        
        product.setStatus(status);
        productRepository.save(product);
    }

    /**
     * 强制删除商品
     */
    public void forceDeleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        
        // 检查是否有未完成的订单
        long pendingOrders = orderRepository.countByProductIdAndStatusIn(productId, List.of(1, 2));
        if (pendingOrders > 0) {
            throw new BusinessException("该商品存在未完成的订单，无法删除");
        }
        
        productRepository.delete(product);
    }

    /**
     * 获取用户详细信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("user", user);
        
        // 用户商品统计
        long productCount = productRepository.countByUserIdAndStatusNot(userId, 0);
        long soldProductCount = productRepository.countByUserIdAndStatus(userId, 3);
        
        // 用户订单统计
        long buyOrderCount = orderRepository.countByBuyerId(userId);
        long sellOrderCount = orderRepository.countBySellerId(userId);
        BigDecimal buyAmount = orderRepository.sumAmountByBuyerIdAndStatus(userId, 3);
        BigDecimal sellAmount = orderRepository.sumAmountBySellerIdAndStatus(userId, 3);
        
        detail.put("statistics", Map.of(
            "productCount", productCount,
            "soldProductCount", soldProductCount,
            "buyOrderCount", buyOrderCount,
            "sellOrderCount", sellOrderCount,
            "buyAmount", buyAmount != null ? buyAmount : BigDecimal.ZERO,
            "sellAmount", sellAmount != null ? sellAmount : BigDecimal.ZERO
        ));
        
        return detail;
    }

    /**
     * 获取商品详细信息（管理员视图）
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("product", new ProductResponse(product));
        
        // 商品订单统计
        long orderCount = orderRepository.countByProductId(productId);
        
        detail.put("statistics", Map.of(
            "orderCount", orderCount,
            "viewCount", product.getViewCount()
        ));
        
        return detail;
    }

    /**
     * 获取用户活跃度统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserActivityStatistics(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        long newUsers = userRepository.countByCreateTimeAfter(startTime);
        long activeUsers = userRepository.countByUpdateTimeAfter(startTime);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("newUsers", newUsers);
        statistics.put("activeUsers", activeUsers);
        statistics.put("days", days);
        
        return statistics;
    }

    /**
     * 获取商品分类统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCategoryStatistics() {
        List<Map<String, Object>> categoryStats = categoryRepository.getCategoryProductCounts();
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("categoryStats", categoryStats);
        
        return statistics;
    }

    /**
     * 获取交易统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTradeStatistics(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        long newOrders = orderRepository.countByCreateTimeAfter(startTime);
        long completedOrders = orderRepository.countByCreateTimeAfterAndStatus(startTime, 3);
        BigDecimal totalAmount = orderRepository.sumAmountByCreateTimeAfterAndStatus(startTime, 3);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("newOrders", newOrders);
        statistics.put("completedOrders", completedOrders);
        statistics.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        statistics.put("days", days);
        
        return statistics;
    }

    /**
     * 批量更新用户状态
     */
    public void batchUpdateUserStatus(List<Long> userIds, Integer status) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("用户ID列表不能为空");
        }
        
        if (status < 0 || status > 1) {
            throw new BusinessException("用户状态值无效");
        }
        
        List<User> users = userRepository.findAllById(userIds);
        users.forEach(user -> user.setStatus(status));
        userRepository.saveAll(users);
    }

    /**
     * 批量更新商品状态
     */
    public void batchUpdateProductStatus(List<Long> productIds, Integer status) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessException("商品ID列表不能为空");
        }
        
        if (status < 0 || status > 3) {
            throw new BusinessException("商品状态值无效");
        }
        
        List<Product> products = productRepository.findAllById(productIds);
        products.forEach(product -> product.setStatus(status));
        productRepository.saveAll(products);
    }

    /**
     * 获取系统日志
     */
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getSystemLogs(int page, int size, String level, String keyword) {
        // 使用SystemLogService获取日志
        List<Map<String, Object>> logs = systemLogService.getSystemLogs(page, size, level, keyword);
        
        // 转换为Page对象
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return new PageImpl<>(logs, pageable, logs.size());
    }

    /**
     * 清理系统数据
     */
    public void cleanupSystemData(int days) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(days);
        
        // 清理已删除的商品（软删除超过指定天数）
        List<Product> deletedProducts = productRepository.findByStatusAndUpdateTimeBefore(0, cutoffTime);
        productRepository.deleteAll(deletedProducts);
        
        // 清理已取消的订单（超过指定天数）
        List<com.example.campussystem.entity.Order> cancelledOrders = 
            orderRepository.findByStatusAndUpdateTimeBefore(0, cutoffTime);
        orderRepository.deleteAll(cancelledOrders);
    }
}