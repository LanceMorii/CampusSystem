package com.example.campussystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 */
public class UserProfileResponse {
    
    private Long id;
    private String studentId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;
    private String role;
    private Integer productCount;  // 发布的商品数量
    private Integer buyOrderCount; // 购买订单数量
    private Integer sellOrderCount; // 销售订单数量
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    public UserProfileResponse() {}

    public UserProfileResponse(Long id, String studentId, String username, String realName, 
                             String phone, String email, String avatar, Integer status,
                             LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.studentId = studentId;
        this.username = username;
        this.realName = realName;
        this.phone = phone;
        this.email = email;
        this.avatar = avatar;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }

    public Integer getBuyOrderCount() {
        return buyOrderCount;
    }

    public void setBuyOrderCount(Integer buyOrderCount) {
        this.buyOrderCount = buyOrderCount;
    }

    public Integer getSellOrderCount() {
        return sellOrderCount;
    }

    public void setSellOrderCount(Integer sellOrderCount) {
        this.sellOrderCount = sellOrderCount;
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
}