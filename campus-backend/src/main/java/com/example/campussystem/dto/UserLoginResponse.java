package com.example.campussystem.dto;

import java.time.LocalDateTime;

/**
 * 用户登录响应DTO
 */
public class UserLoginResponse {

    private Long userId;
    private String studentId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private String token;
    private LocalDateTime loginTime;

    public UserLoginResponse() {}

    public UserLoginResponse(Long userId, String studentId, String username, String realName, 
                           String phone, String email, String avatar, String token, LocalDateTime loginTime) {
        this.userId = userId;
        this.studentId = studentId;
        this.username = username;
        this.realName = realName;
        this.phone = phone;
        this.email = email;
        this.avatar = avatar;
        this.token = token;
        this.loginTime = loginTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    @Override
    public String toString() {
        return "UserLoginResponse{" +
                "userId=" + userId +
                ", studentId='" + studentId + '\'' +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", token='[PROTECTED]'" +
                ", loginTime=" + loginTime +
                '}';
    }
}