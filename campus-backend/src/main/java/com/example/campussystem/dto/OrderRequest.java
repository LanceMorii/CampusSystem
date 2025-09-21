package com.example.campussystem.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 订单请求DTO
 */
public class OrderRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.01", message = "交易金额必须大于0")
    private BigDecimal amount;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    // 构造函数
    public OrderRequest() {}

    public OrderRequest(Long productId, BigDecimal amount, String remark) {
        this.productId = productId;
        this.amount = amount;
        this.remark = remark;
    }

    // Getter和Setter方法
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "OrderRequest{" +
                "productId=" + productId +
                ", amount=" + amount +
                ", remark='" + remark + '\'' +
                '}';
    }
}