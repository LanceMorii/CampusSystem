package com.example.campussystem.controller;

import com.example.campussystem.common.ApiResponse;
import com.example.campussystem.dto.OrderRequest;
import com.example.campussystem.dto.OrderResponse;
import com.example.campussystem.security.UserPrincipal;
import com.example.campussystem.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        OrderResponse response = orderService.createOrder(request, userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("订单创建成功", response));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        OrderResponse response = orderService.getOrderDetail(id, userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("获取订单详情成功", response));
    }

    /**
     * 根据订单号获取订单详情
     */
    @GetMapping("/no/{orderNo}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNo(
            @PathVariable String orderNo,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        OrderResponse response = orderService.getOrderByOrderNo(orderNo, userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("获取订单详情成功", response));
    }

    /**
     * 取消订单
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        orderService.cancelOrder(id, userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("订单取消成功"));
    }

    /**
     * 买家确认订单
     */
    @PatchMapping("/{id}/confirm/buyer")
    public ResponseEntity<ApiResponse<String>> confirmOrderByBuyer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        orderService.confirmOrderByBuyer(id, userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("订单确认成功"));
    }

    /**
     * 卖家确认订单
     */
    @PatchMapping("/{id}/confirm/seller")
    public ResponseEntity<ApiResponse<String>> confirmOrderBySeller(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        orderService.confirmOrderBySeller(id, userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("订单确认成功"));
    }

    /**
     * 获取我的买家订单列表
     */
    @GetMapping("/buyer")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getBuyerOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        List<OrderResponse> response = orderService.getBuyerOrders(userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("获取买家订单成功", response));
    }

    /**
     * 获取我的卖家订单列表
     */
    @GetMapping("/seller")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getSellerOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        List<OrderResponse> response = orderService.getSellerOrders(userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("获取卖家订单成功", response));
    }

    /**
     * 获取我的所有订单列表
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        List<OrderResponse> response = orderService.getUserOrders(userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("获取我的订单成功", response));
    }

    /**
     * 根据状态获取我的订单列表
     */
    @GetMapping("/my/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrdersByStatus(
            @PathVariable Integer status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        List<OrderResponse> response = orderService.getUserOrdersByStatus(userPrincipal.getUserId(), status);
        return ResponseEntity.ok(ApiResponse.success("获取订单列表成功", response));
    }

    /**
     * 分页获取我的订单列表
     */
    @GetMapping("/my/page")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getUserOrdersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Page<OrderResponse> response = orderService.getUserOrdersPage(
                userPrincipal.getUserId(), page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("获取订单列表成功", response));
    }

    /**
     * 获取买家待确认订单
     */
    @GetMapping("/buyer/pending")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getBuyerPendingOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        List<OrderResponse> response = orderService.getBuyerPendingOrders(userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("获取待确认订单成功", response));
    }

    /**
     * 获取卖家待确认订单
     */
    @GetMapping("/seller/pending")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getSellerPendingOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        List<OrderResponse> response = orderService.getSellerPendingOrders(userPrincipal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("获取待确认订单成功", response));
    }

    /**
     * 统计我的订单数量
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countUserOrders(
            @RequestParam(defaultValue = "all") String type,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        long count = orderService.countUserOrders(userPrincipal.getUserId(), type);
        return ResponseEntity.ok(ApiResponse.success("获取订单数量成功", count));
    }

    /**
     * 计算我的交易金额
     */
    @GetMapping("/amount")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateUserTotalAmount(
            @RequestParam(defaultValue = "all") String type,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        BigDecimal amount = orderService.calculateUserTotalAmount(userPrincipal.getUserId(), type);
        return ResponseEntity.ok(ApiResponse.success("获取交易金额成功", amount));
    }

    /**
     * 获取指定用户的订单数量（管理员功能）
     */
    @GetMapping("/count/user/{userId}")
    public ResponseEntity<ApiResponse<Long>> countUserOrdersById(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "all") String type) {
        
        long count = orderService.countUserOrders(userId, type);
        return ResponseEntity.ok(ApiResponse.success("获取用户订单数量成功", count));
    }

    /**
     * 获取指定用户的交易金额（管理员功能）
     */
    @GetMapping("/amount/user/{userId}")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateUserTotalAmountById(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "all") String type) {
        
        BigDecimal amount = orderService.calculateUserTotalAmount(userId, type);
        return ResponseEntity.ok(ApiResponse.success("获取用户交易金额成功", amount));
    }
}