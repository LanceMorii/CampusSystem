package com.example.campussystem.security;

import com.example.campussystem.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 拦截请求，验证JWT token，设置用户认证信息
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                // 从token中获取用户信息
                Long userId = jwtUtil.getUserIdFromToken(jwt);
                String studentId = jwtUtil.getStudentIdFromToken(jwt);
                String username = jwtUtil.getUsernameFromToken(jwt);
                String role = jwtUtil.getRoleFromToken(jwt);
                
                if (userId != null && studentId != null) {
                    // 创建用户认证信息
                    UserPrincipal userPrincipal = new UserPrincipal(userId, studentId, username);
                    
                    // 设置用户权限
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userPrincipal, 
                            null, 
                            Collections.singletonList(authority)
                        );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // 将userId设置到request属性中，供Controller使用
                    request.setAttribute("userId", userId);
                    
                    // 添加调试日志
                    System.out.println("JWT过滤器设置userId: " + userId + ", studentId: " + studentId);
                    
                    logger.debug("设置用户认证信息: userId={}, studentId={}, role={}", userId, studentId, role);
                }
            }
        } catch (Exception ex) {
            logger.error("无法设置用户认证信息", ex);
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取JWT token
     *
     * @param request HTTP请求
     * @return JWT token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtUtil.getHeader());
        return jwtUtil.getTokenFromHeader(bearerToken);
    }
}