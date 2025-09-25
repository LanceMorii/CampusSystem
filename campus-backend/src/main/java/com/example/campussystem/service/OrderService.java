package com.example.campussystem.service;

import com.example.campussystem.dto.OrderRequest;
import com.example.campussystem.dto.OrderResponse;
import com.example.campussystem.entity.Order;
import com.example.campussystem.entity.Product;
import com.example.campussystem.entity.User;
import com.example.campussystem.exception.BusinessException;
import com.example.campussystem.repository.OrderRepository;
import com.example.campussystem.repository.ProductRepository;
import com.example.campussystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务类
 */
@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 创建订单
     */
    public OrderResponse createOrder(OrderRequest request, Long buyerId) {
        // 验证商品是否存在
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));

        // 验证商品状态
        if (product.getStatus() != 1) {
            throw new BusinessException("商品不可购买");
        }

        // 验证买家不能购买自己的商品
        if (product.getUserId().equals(buyerId)) {
            throw new BusinessException("不能购买自己发布的商品");
        }

        // 验证买家是否存在
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new BusinessException("买家不存在"));

        // 生成订单号
        String orderNo = generateOrderNo();

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setBuyerId(buyerId);
        order.setSellerId(product.getUserId());
        order.setProductId(request.getProductId());
        order.setAmount(request.getAmount());
        order.setRemark(request.getRemark());
        order.setStatus(1); // 待确认
        order.setBuyerConfirm(0);
        order.setSellerConfirm(0);

        Order savedOrder = orderRepository.save(order);

        // 更新商品状态为已预订
        product.setStatus(2);
        productRepository.save(product);

        return convertToResponse(savedOrder);
    }

    /**
     * 获取订单详情
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 验证权限：只有买家或卖家可以查看订单详情
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限查看此订单");
        }

        return convertToResponse(order);
    }

    /**
     * 根据订单号获取订单详情
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNo(String orderNo, Long userId) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 验证权限
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限查看此订单");
        }

        return convertToResponse(order);
    }

    /**
     * 取消订单
     */
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 验证权限：只有买家或卖家可以取消订单
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权限操作此订单");
        }

        // 验证订单状态：只有待确认或进行中的订单可以取消
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BusinessException("订单状态不允许取消");
        }

        // 更新订单状态
        order.setStatus(4); // 已取消
        orderRepository.save(order);

        // 恢复商品状态
        Product product = productRepository.findById(order.getProductId()).orElse(null);
        if (product != null) {
            product.setStatus(1); // 可售
            productRepository.save(product);
        }
    }

    /**
     * 确认订单（买家确认）
     */
    public void confirmOrderByBuyer(Long orderId, Long buyerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 验证权限
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("无权限操作此订单");
        }

        // 验证订单状态
        if (order.getStatus() == 4) {
            throw new BusinessException("订单已取消，无法确认");
        }

        // 买家确认
        order.setBuyerConfirm(1);
        
        // 检查是否双方都已确认
        if (order.getSellerConfirm() == 1) {
            order.setStatus(3); // 已完成
            
            // 更新商品状态为已售出
            Product product = productRepository.findById(order.getProductId()).orElse(null);
            if (product != null) {
                product.setStatus(3); // 已售出
                productRepository.save(product);
            }
        } else {
            order.setStatus(2); // 进行中
        }

        orderRepository.save(order);
    }

    /**
     * 确认订单（卖家确认）
     */
    public void confirmOrderBySeller(Long orderId, Long sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 验证权限
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权限操作此订单");
        }

        // 验证订单状态
        if (order.getStatus() == 4) {
            throw new BusinessException("订单已取消，无法确认");
        }

        // 卖家确认
        order.setSellerConfirm(1);
        
        // 检查是否双方都已确认
        if (order.getBuyerConfirm() == 1) {
            order.setStatus(3); // 已完成
            
            // 更新商品状态为已售出
            Product product = productRepository.findById(order.getProductId()).orElse(null);
            if (product != null) {
                product.setStatus(3); // 已售出
                productRepository.save(product);
            }
        } else {
            order.setStatus(2); // 进行中
        }

        orderRepository.save(order);
    }

    /**
     * 获取用户的买家订单列表
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getBuyerOrders(Long buyerId) {
        List<Order> orders = orderRepository.findByBuyerIdOrderByCreateTimeDesc(buyerId);
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 获取用户的卖家订单列表
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getSellerOrders(Long sellerId) {
        List<Order> orders = orderRepository.findBySellerIdOrderByCreateTimeDesc(sellerId);
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 获取用户的所有订单列表
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findUserOrders(userId);
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 根据状态获取用户订单列表
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrdersByStatus(Long userId, Integer status) {
        List<Order> orders = orderRepository.findUserOrdersByStatus(userId, status);
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 分页获取用户订单列表
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrdersPage(Long userId, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Order> orderPage = orderRepository.findUserOrders(userId, pageable);
        return orderPage.map(this::convertToResponse);
    }

    /**
     * 获取买家待确认订单
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getBuyerPendingOrders(Long buyerId) {
        List<Order> orders = orderRepository.findBuyerPendingConfirmOrders(buyerId);
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 获取卖家待确认订单
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getSellerPendingOrders(Long sellerId) {
        List<Order> orders = orderRepository.findSellerPendingConfirmOrders(sellerId);
        return orders.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 统计用户订单数量
     */
    @Transactional(readOnly = true)
    public long countUserOrders(Long userId, String type) {
        switch (type.toLowerCase()) {
            case "buyer":
                return orderRepository.countByBuyerId(userId);
            case "seller":
                return orderRepository.countBySellerId(userId);
            default:
                return orderRepository.countByBuyerId(userId) + orderRepository.countBySellerId(userId);
        }
    }

    /**
     * 计算用户交易金额
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateUserTotalAmount(Long userId, String type) {
        switch (type.toLowerCase()) {
            case "buyer":
                return orderRepository.calculateBuyerTotalAmount(userId);
            case "seller":
                return orderRepository.calculateSellerTotalAmount(userId);
            default:
                BigDecimal buyerAmount = orderRepository.calculateBuyerTotalAmount(userId);
                BigDecimal sellerAmount = orderRepository.calculateSellerTotalAmount(userId);
                return buyerAmount.add(sellerAmount);
        }
    }

    /**
     * 获取用户订单统计
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getUserOrderStats(Long userId) {
        Map<String, Long> stats = new HashMap<>();
        
        // 统计各种状态的订单数量
        stats.put("pending", orderRepository.countUserOrdersByStatus(userId, 1)); // 待确认
        stats.put("processing", orderRepository.countUserOrdersByStatus(userId, 2)); // 进行中
        stats.put("completed", orderRepository.countUserOrdersByStatus(userId, 3)); // 已完成
        stats.put("cancelled", orderRepository.countUserOrdersByStatus(userId, 4)); // 已取消
        
        // 统计买家和卖家订单数量
        stats.put("asBuyer", orderRepository.countByBuyerId(userId));
        stats.put("asSeller", orderRepository.countBySellerId(userId));
        
        // 总订单数
        stats.put("total", stats.get("asBuyer") + stats.get("asSeller"));
        
        return stats;
    }

    /**
     * 获取用户总交易金额
     */
    @Transactional(readOnly = true)
    public Double getUserTotalTradeAmount(Long userId) {
        try {
            // 只计算已完成订单的金额
            BigDecimal buyerAmount = orderRepository.calculateCompletedBuyerAmount(userId);
            BigDecimal sellerAmount = orderRepository.calculateCompletedSellerAmount(userId);
            
            if (buyerAmount == null) buyerAmount = BigDecimal.ZERO;
            if (sellerAmount == null) sellerAmount = BigDecimal.ZERO;
            
            return buyerAmount.add(sellerAmount).doubleValue();
        } catch (Exception e) {
            System.err.println("计算用户交易金额失败: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int) (Math.random() * 10000));
        return "ORD" + timestamp + String.format("%04d", Integer.parseInt(random));
    }

    /**
     * 转换为响应DTO
     */
    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setBuyerId(order.getBuyerId());
        response.setSellerId(order.getSellerId());
        response.setProductId(order.getProductId());
        response.setAmount(order.getAmount());
        response.setStatus(order.getStatus());
        response.setStatusText(getStatusText(order.getStatus()));
        response.setBuyerConfirm(order.getBuyerConfirm());
        response.setSellerConfirm(order.getSellerConfirm());
        response.setRemark(order.getRemark());
        response.setCreateTime(order.getCreateTime());
        response.setUpdateTime(order.getUpdateTime());

        // 设置买家信息
        if (order.getBuyer() != null) {
            response.setBuyerUsername(order.getBuyer().getUsername());
            response.setBuyerNickname(order.getBuyer().getRealName());
        } else {
            // 如果关联对象为空，手动查询
            userRepository.findById(order.getBuyerId()).ifPresent(buyer -> {
                response.setBuyerUsername(buyer.getUsername());
                response.setBuyerNickname(buyer.getRealName());
            });
        }

        // 设置卖家信息
        if (order.getSeller() != null) {
            response.setSellerUsername(order.getSeller().getUsername());
            response.setSellerNickname(order.getSeller().getRealName());
        } else {
            userRepository.findById(order.getSellerId()).ifPresent(seller -> {
                response.setSellerUsername(seller.getUsername());
                response.setSellerNickname(seller.getRealName());
            });
        }

        // 设置商品信息
        if (order.getProduct() != null) {
            response.setProductTitle(order.getProduct().getTitle());
            response.setProductImage(order.getProduct().getImages());
        } else {
            productRepository.findById(order.getProductId()).ifPresent(product -> {
                response.setProductTitle(product.getTitle());
                response.setProductImage(product.getImages());
            });
        }

        return response;
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 1:
                return "待确认";
            case 2:
                return "进行中";
            case 3:
                return "已完成";
            case 4:
                return "已取消";
            default:
                return "未知状态";
        }
    }
}