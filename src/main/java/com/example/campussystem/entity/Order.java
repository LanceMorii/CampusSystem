package com.example.campussystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", unique = true, nullable = false, length = 32)
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @Column(name = "buyer_id", nullable = false)
    @NotNull(message = "买家ID不能为空")
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    @NotNull(message = "卖家ID不能为空")
    private Long sellerId;

    @Column(name = "product_id", nullable = false)
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.01", message = "交易金额必须大于0")
    private BigDecimal amount;

    @Column(name = "status")
    private Integer status = 1; // 1待确认,2进行中,3已完成,4已取消

    @Column(name = "buyer_confirm")
    private Integer buyerConfirm = 0; // 0未确认,1已确认

    @Column(name = "seller_confirm")
    private Integer sellerConfirm = 0; // 0未确认,1已确认

    @Column(name = "remark", columnDefinition = "TEXT")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // 多对一关系：订单的买家
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", insertable = false, updatable = false)
    private User buyer;

    // 多对一关系：订单的卖家
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private User seller;

    // 多对一关系：订单的商品
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    // 构造函数
    public Order() {}

    public Order(String orderNo, Long buyerId, Long sellerId, Long productId, BigDecimal amount) {
        this.orderNo = orderNo;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.productId = productId;
        this.amount = amount;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getBuyerConfirm() {
        return buyerConfirm;
    }

    public void setBuyerConfirm(Integer buyerConfirm) {
        this.buyerConfirm = buyerConfirm;
    }

    public Integer getSellerConfirm() {
        return sellerConfirm;
    }

    public void setSellerConfirm(Integer sellerConfirm) {
        this.sellerConfirm = sellerConfirm;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    // 业务方法
    public boolean isPending() {
        return status == 1; // 待确认
    }

    public boolean isInProgress() {
        return status == 2; // 进行中
    }

    public boolean isCompleted() {
        return status == 3; // 已完成
    }

    public boolean isCancelled() {
        return status == 4; // 已取消
    }

    public boolean isBothConfirmed() {
        return buyerConfirm == 1 && sellerConfirm == 1;
    }

    public void confirmByBuyer() {
        this.buyerConfirm = 1;
        if (isBothConfirmed()) {
            this.status = 3; // 双方都确认后，订单完成
        }
    }

    public void confirmBySeller() {
        this.sellerConfirm = 1;
        if (isBothConfirmed()) {
            this.status = 3; // 双方都确认后，订单完成
        }
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderNo='" + orderNo + '\'' +
                ", buyerId=" + buyerId +
                ", sellerId=" + sellerId +
                ", productId=" + productId +
                ", amount=" + amount +
                ", status=" + status +
                ", buyerConfirm=" + buyerConfirm +
                ", sellerConfirm=" + sellerConfirm +
                ", createTime=" + createTime +
                '}';
    }
}