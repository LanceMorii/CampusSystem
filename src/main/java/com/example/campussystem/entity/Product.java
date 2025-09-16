package com.example.campussystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品实体类
 */
@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "title", nullable = false, length = 100)
    @NotBlank(message = "商品标题不能为空")
    @Size(max = 100, message = "商品标题长度不能超过100个字符")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 2000, message = "商品描述长度不能超过2000个字符")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    @DecimalMax(value = "99999999.99", message = "商品价格不能超过99999999.99")
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    @DecimalMax(value = "99999999.99", message = "原价不能超过99999999.99")
    private BigDecimal originalPrice;

    @Column(name = "images", columnDefinition = "JSON")
    private String images; // JSON格式存储图片URL数组

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "status")
    private Integer status = 1; // 1上架,2下架,3已售出,0已删除

    // 商品详细信息字段
    @Column(name = "brand", length = 50)
    @Size(max = 50, message = "品牌长度不能超过50个字符")
    private String brand;

    @Column(name = "model", length = 50)
    @Size(max = 50, message = "型号长度不能超过50个字符")
    private String model;

    @Column(name = "color", length = 20)
    @Size(max = 20, message = "颜色长度不能超过20个字符")
    private String color;

    @Column(name = "size", length = 20)
    @Size(max = 20, message = "尺寸长度不能超过20个字符")
    private String size;

    @Column(name = "purchase_time", length = 50)
    @Size(max = 50, message = "购买时间长度不能超过50个字符")
    private String purchaseTime;

    @Column(name = "`condition`", length = 20)
    @Size(max = 20, message = "使用程度长度不能超过20个字符")
    private String condition; // 全新、九成新、八成新等

    @Column(name = "purchase_location", length = 100)
    @Size(max = 100, message = "购买地点长度不能超过100个字符")
    private String purchaseLocation;

    @Column(name = "trade_notes", length = 500)
    @Size(max = 500, message = "交易说明长度不能超过500个字符")
    private String tradeNotes; // 交易说明，如面交地点、注意事项等

    @Column(name = "is_negotiable")
    private Boolean isNegotiable = false; // 是否可议价

    @Column(name = "include_invoice")
    private Boolean includeInvoice = false; // 是否包含发票

    @Column(name = "include_warranty")
    private Boolean includeWarranty = false; // 是否在保修期内

    @Column(name = "tags", length = 100)
    @Size(max = 100, message = "标签长度不能超过100个字符")
    private String tags; // 商品标签，用逗号分隔

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // 多对一关系：商品属于某个用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    // 多对一关系：商品属于某个分类
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    // 一对多关系：商品的订单
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    // 一对多关系：商品相关的消息
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;

    // 构造函数
    public Product() {}

    public Product(Long userId, Long categoryId, String title, String description, BigDecimal price) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.price = price;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    // 新增字段的getter和setter方法
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(String purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getPurchaseLocation() {
        return purchaseLocation;
    }

    public void setPurchaseLocation(String purchaseLocation) {
        this.purchaseLocation = purchaseLocation;
    }

    public String getTradeNotes() {
        return tradeNotes;
    }

    public void setTradeNotes(String tradeNotes) {
        this.tradeNotes = tradeNotes;
    }

    public Boolean getIsNegotiable() {
        return isNegotiable;
    }

    public void setIsNegotiable(Boolean isNegotiable) {
        this.isNegotiable = isNegotiable;
    }

    public Boolean getIncludeInvoice() {
        return includeInvoice;
    }

    public void setIncludeInvoice(Boolean includeInvoice) {
        this.includeInvoice = includeInvoice;
    }

    public Boolean getIncludeWarranty() {
        return includeWarranty;
    }

    public void setIncludeWarranty(Boolean includeWarranty) {
        this.includeWarranty = includeWarranty;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    // 业务方法
    public boolean isAvailable() {
        return status == 1; // 上架状态
    }

    public boolean isSold() {
        return status == 3; // 已售出状态
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", userId=" + userId +
                ", categoryId=" + categoryId +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", originalPrice=" + originalPrice +
                ", viewCount=" + viewCount +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}