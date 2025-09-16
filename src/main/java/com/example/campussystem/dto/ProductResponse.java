package com.example.campussystem.dto;

import com.example.campussystem.entity.Product;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品响应DTO
 */
public class ProductResponse {

    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private List<String> images;
    private Integer viewCount;
    private Integer status;
    private String statusText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 商品详细信息字段
    private String brand;
    private String model;
    private String color;
    private String size;
    private String purchaseTime;
    private String condition;
    private String purchaseLocation;
    private String tradeNotes;
    private Boolean isNegotiable;
    private Boolean includeInvoice;
    private Boolean includeWarranty;
    private List<String> tags;

    // 构造函数
    public ProductResponse() {}

    public ProductResponse(Product product) {
        this.id = product.getId();
        this.userId = product.getUserId();
        this.categoryId = product.getCategoryId();
        this.title = product.getTitle();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.originalPrice = product.getOriginalPrice();
        this.viewCount = product.getViewCount();
        this.status = product.getStatus();
        this.createTime = product.getCreateTime();
        this.updateTime = product.getUpdateTime();
        
        // 设置状态文本
        this.statusText = getStatusText(product.getStatus());
        
        // 处理图片JSON
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            try {
                // 这里简化处理，实际应该用JSON解析
                String imagesStr = product.getImages();
                if (imagesStr.startsWith("[") && imagesStr.endsWith("]")) {
                    imagesStr = imagesStr.substring(1, imagesStr.length() - 1);
                    this.images = List.of(imagesStr.split(","));
                }
            } catch (Exception e) {
                this.images = List.of();
            }
        }
        
        // 如果有关联的用户和分类信息，设置相关字段
        if (product.getUser() != null) {
            this.username = product.getUser().getUsername();
            this.userAvatar = product.getUser().getAvatar();
        }
        
        if (product.getCategory() != null) {
            this.categoryName = product.getCategory().getName();
        }
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "上架中";
            case 2 -> "已下架";
            case 3 -> "已售出";
            case 0 -> "已删除";
            default -> "未知";
        };
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
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

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}