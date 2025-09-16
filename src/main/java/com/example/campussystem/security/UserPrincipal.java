package com.example.campussystem.security;

import java.security.Principal;

/**
 * 用户主体类
 * 用于在安全上下文中存储用户信息
 */
public class UserPrincipal implements Principal {
    
    private final Long userId;
    private final String studentId;
    private final String username;

    public UserPrincipal(Long userId, String studentId, String username) {
        this.userId = userId;
        this.studentId = studentId;
        this.username = username;
    }

    @Override
    public String getName() {
        return studentId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "userId=" + userId +
                ", studentId='" + studentId + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}