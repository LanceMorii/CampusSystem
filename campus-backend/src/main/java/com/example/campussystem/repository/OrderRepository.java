package com.example.campussystem.repository;

import com.example.campussystem.entity.Order;
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
 * 订单数据访问接口
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 根据订单号查找订单
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 根据买家ID查找订单列表
     */
    List<Order> findByBuyerIdOrderByCreateTimeDesc(Long buyerId);

    /**
     * 根据卖家ID查找订单列表
     */
    List<Order> findBySellerIdOrderByCreateTimeDesc(Long sellerId);

    /**
     * 根据商品ID查找订单列表
     */
    List<Order> findByProductIdOrderByCreateTimeDesc(Long productId);

    /**
     * 根据买家ID和状态查找订单列表
     */
    List<Order> findByBuyerIdAndStatusOrderByCreateTimeDesc(Long buyerId, Integer status);

    /**
     * 根据卖家ID和状态查找订单列表
     */
    List<Order> findBySellerIdAndStatusOrderByCreateTimeDesc(Long sellerId, Integer status);

    /**
     * 根据买家ID分页查询订单
     */
    Page<Order> findByBuyerIdOrderByCreateTimeDesc(Long buyerId, Pageable pageable);

    /**
     * 根据卖家ID分页查询订单
     */
    Page<Order> findBySellerIdOrderByCreateTimeDesc(Long sellerId, Pageable pageable);

    /**
     * 根据买家ID和状态分页查询订单
     */
    Page<Order> findByBuyerIdAndStatusOrderByCreateTimeDesc(Long buyerId, Integer status, Pageable pageable);

    /**
     * 根据卖家ID和状态分页查询订单
     */
    Page<Order> findBySellerIdAndStatusOrderByCreateTimeDesc(Long sellerId, Integer status, Pageable pageable);

    /**
     * 查询指定时间范围内的订单
     */
    @Query("SELECT o FROM Order o WHERE o.createTime BETWEEN :startTime AND :endTime ORDER BY o.createTime DESC")
    List<Order> findOrdersByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定金额范围内的订单
     */
    @Query("SELECT o FROM Order o WHERE o.amount BETWEEN :minAmount AND :maxAmount ORDER BY o.createTime DESC")
    List<Order> findOrdersByAmountRange(@Param("minAmount") BigDecimal minAmount, 
                                       @Param("maxAmount") BigDecimal maxAmount);

    /**
     * 统计买家订单数量
     */
    long countByBuyerId(Long buyerId);

    /**
     * 统计卖家订单数量
     */
    long countBySellerId(Long sellerId);

    /**
     * 统计指定状态的订单数量
     */
    long countByStatus(Integer status);

    /**
     * 统计买家指定状态的订单数量
     */
    long countByBuyerIdAndStatus(Long buyerId, Integer status);

    /**
     * 统计卖家指定状态的订单数量
     */
    long countBySellerIdAndStatus(Long sellerId, Integer status);

    /**
     * 查询待确认的订单（双方都未确认）
     */
    @Query("SELECT o FROM Order o WHERE o.buyerConfirm = 0 AND o.sellerConfirm = 0 ORDER BY o.createTime DESC")
    List<Order> findPendingConfirmOrders();

    /**
     * 查询买家待确认的订单
     */
    @Query("SELECT o FROM Order o WHERE o.buyerId = :buyerId AND o.buyerConfirm = 0 ORDER BY o.createTime DESC")
    List<Order> findBuyerPendingConfirmOrders(@Param("buyerId") Long buyerId);

    /**
     * 查询卖家待确认的订单
     */
    @Query("SELECT o FROM Order o WHERE o.sellerId = :sellerId AND o.sellerConfirm = 0 ORDER BY o.createTime DESC")
    List<Order> findSellerPendingConfirmOrders(@Param("sellerId") Long sellerId);

    /**
     * 查询已完成的订单（双方都已确认）
     */
    @Query("SELECT o FROM Order o WHERE o.buyerConfirm = 1 AND o.sellerConfirm = 1 ORDER BY o.createTime DESC")
    List<Order> findCompletedOrders();

    /**
     * 查询用户参与的所有订单（作为买家或卖家）
     */
    @Query("SELECT o FROM Order o WHERE o.buyerId = :userId OR o.sellerId = :userId ORDER BY o.createTime DESC")
    List<Order> findUserOrders(@Param("userId") Long userId);

    /**
     * 查询用户参与的指定状态订单
     */
    @Query("SELECT o FROM Order o WHERE (o.buyerId = :userId OR o.sellerId = :userId) AND o.status = :status ORDER BY o.createTime DESC")
    List<Order> findUserOrdersByStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 分页查询用户参与的所有订单
     */
    @Query("SELECT o FROM Order o WHERE o.buyerId = :userId OR o.sellerId = :userId ORDER BY o.createTime DESC")
    Page<Order> findUserOrders(@Param("userId") Long userId, Pageable pageable);

    /**
     * 统计买家交易金额
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.buyerId = :buyerId AND o.status = 3")
    BigDecimal calculateBuyerTotalAmount(@Param("buyerId") Long buyerId);

    /**
     * 统计卖家交易金额
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.sellerId = :sellerId AND o.status = 3")
    BigDecimal calculateSellerTotalAmount(@Param("sellerId") Long sellerId);

    /**
     * 统计指定时间后创建的订单数量
     */
    long countByCreateTimeAfter(LocalDateTime createTime);

    /**
     * 统计指定时间后创建且指定状态的订单数量
     */
    long countByCreateTimeAfterAndStatus(LocalDateTime createTime, Integer status);

    /**
     * 计算指定时间后创建且指定状态的订单总金额
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.createTime > :createTime AND o.status = :status")
    BigDecimal sumAmountByCreateTimeAfterAndStatus(@Param("createTime") LocalDateTime createTime, @Param("status") Integer status);

    /**
     * 计算指定状态的订单总金额
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") Integer status);

    /**
     * 计算买家指定状态的订单总金额
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.buyerId = :buyerId AND o.status = :status")
    BigDecimal sumAmountByBuyerIdAndStatus(@Param("buyerId") Long buyerId, @Param("status") Integer status);

    /**
     * 计算卖家指定状态的订单总金额
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.sellerId = :sellerId AND o.status = :status")
    BigDecimal sumAmountBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") Integer status);

    /**
     * 统计商品的订单数量
     */
    long countByProductId(Long productId);

    /**
     * 统计商品指定状态的订单数量
     */
    long countByProductIdAndStatusIn(Long productId, List<Integer> statuses);

    /**
     * 根据状态和更新时间查询订单
     */
    List<Order> findByStatusAndUpdateTimeBefore(Integer status, LocalDateTime updateTime);

    /**
     * 查询最近的订单
     */
    @Query("SELECT o FROM Order o ORDER BY o.createTime DESC")
    Page<Order> findRecentOrders(Pageable pageable);

    /**
     * 查询热门商品的订单统计
     */
    @Query("SELECT o.productId, COUNT(o) as orderCount FROM Order o WHERE o.status = 3 GROUP BY o.productId ORDER BY orderCount DESC")
    List<Object[]> findPopularProductOrders();

    /**
     * 统计用户指定状态的订单数量（作为买家或卖家）
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE (o.buyerId = :userId OR o.sellerId = :userId) AND o.status = :status")
    long countUserOrdersByStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 计算买家已完成订单的总金额
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.buyerId = :buyerId AND o.status = 3")
    BigDecimal calculateCompletedBuyerAmount(@Param("buyerId") Long buyerId);

    /**
     * 计算卖家已完成订单的总金额
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.sellerId = :sellerId AND o.status = 3")
    BigDecimal calculateCompletedSellerAmount(@Param("sellerId") Long sellerId);

    /**
     * 统计用户成功交易次数（作为买家或卖家，状态为已完成）
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE (o.buyerId = :userId OR o.sellerId = :userId) AND o.status = 3")
    long countSuccessfulOrdersByUserId(@Param("userId") Long userId);

    /**
     * 计算用户成功交易总金额（作为买家或卖家，状态为已完成）
     */
    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE (o.buyerId = :userId OR o.sellerId = :userId) AND o.status = 3")
    BigDecimal sumSuccessfulOrderAmountByUserId(@Param("userId") Long userId);
}