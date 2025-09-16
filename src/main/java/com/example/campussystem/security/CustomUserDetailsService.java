package com.example.campussystem.security;

import com.example.campussystem.entity.User;
import com.example.campussystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自定义用户详情服务
 * 用于Spring Security加载用户信息
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String studentId) throws UsernameNotFoundException {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + studentId));

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new UsernameNotFoundException("用户已被禁用: " + studentId);
        }

        // 创建用户详情对象
        return new CustomUserDetails(
                user.getId(),
                user.getStudentId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    /**
     * 根据用户ID加载用户信息
     *
     * @param userId 用户ID
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在异常
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + userId));

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new UsernameNotFoundException("用户已被禁用: " + userId);
        }

        // 创建用户详情对象
        return new CustomUserDetails(
                user.getId(),
                user.getStudentId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}