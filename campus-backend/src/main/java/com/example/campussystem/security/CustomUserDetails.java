package com.example.campussystem.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 自定义用户详情类
 * 实现Spring Security的UserDetails接口
 */
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String studentId;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long userId, 
                           String studentId, 
                           String username, 
                           String password, 
                           boolean enabled,
                           Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.studentId = studentId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return studentId; // 使用学号作为用户名
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // 自定义getter方法
    public Long getUserId() {
        return userId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getDisplayName() {
        return username;
    }
}