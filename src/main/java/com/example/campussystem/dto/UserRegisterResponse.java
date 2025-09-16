package com.example.campussystem.dto;

import java.time.LocalDateTime;

/**
 * 用户注册响应DTO
 */
public class UserRegisterResponse {

    private Long id;
    private String studentId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private LocalDateTime createTime;

    // 构造函数
    public UserRegisterResponse() {}

    public UserRegisterResponse(Long id, String studentId, String username, String realName, 
                               String phone, String email, LocalDateTime createTime) {
        this.id = id;
        this.studentId = studentId;
        this.username = username;
        this.realName = realName;
        this.phone = phone;
        this.email = email;
        this.createTime = createTime;
    }

    // Getter和Setter方法
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "UserRegisterResponse{" +
                "id=" + id +
                ", studentId='" + studentId + '\'' +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}