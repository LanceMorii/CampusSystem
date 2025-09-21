package com.example.campussystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户登录请求DTO
 */
public class UserLoginRequest {

    @NotBlank(message = "学号不能为空")
    @Size(min = 8, max = 12, message = "学号长度应为8-12位")
    private String studentId;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应为6-20位")
    private String password;

    public UserLoginRequest() {}

    public UserLoginRequest(String studentId, String password) {
        this.studentId = studentId;
        this.password = password;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserLoginRequest{" +
                "studentId='" + studentId + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}