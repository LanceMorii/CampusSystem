package com.example.campussystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用户信息更新请求DTO
 */
public class UserUpdateRequest {
    
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
    private String username;
    
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    private String avatar;

    public UserUpdateRequest() {}

    public UserUpdateRequest(String username, String realName, String phone, String email, String avatar) {
        this.username = username;
        this.realName = realName;
        this.phone = phone;
        this.email = email;
        this.avatar = avatar;
    }

    // Getters and Setters
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
}