package com.example.campussystem.websocket;

import com.example.campussystem.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

/**
 * WebSocket握手拦截器
 * 用于在WebSocket连接建立前进行JWT认证
 */
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            // 1) 优先从Authorization头中获取token
            String authHeader = request.getHeaders().getFirst("Authorization");
            String token = jwtUtil.getTokenFromHeader(authHeader);

            // 2) 如果请求头没有，尝试从查询参数获取 ?token=
            if (token == null || token.isEmpty()) {
                try {
                    URI uri = request.getURI();
                    String query = uri.getQuery();
                    if (query != null) {
                        for (String pair : query.split("&")) {
                            String[] kv = pair.split("=");
                            if (kv.length == 2 && "token".equalsIgnoreCase(kv[0])) {
                                token = kv[1];
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("解析查询参数获取token失败", e);
                }
            }

            if (token != null && jwtUtil.validateToken(token)) {
                // 获取用户信息
                String username = jwtUtil.getUsernameFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                String studentId = jwtUtil.getStudentIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                
                // 存储到会话属性，供后续使用
                attributes.put("username", username);
                attributes.put("userId", userId);
                attributes.put("studentId", studentId);
                
                // 设置到安全上下文，使后续STOMP能够获取到Principal
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    // 将userId作为principal名称，便于服务端用Principal.getName()获取
                    String.valueOf(userId),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + (role == null ? "USER" : role)))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                logger.info("WebSocket握手认证成功，用户: {}, userId: {}", username, userId);
            } else {
                logger.warn("WebSocket握手：未提供有效token，握手将继续，后续由STOMP CONNECT进行认证");
            }
        } catch (Exception e) {
            logger.error("WebSocket握手认证过程中发生错误", e);
        }
        // 不再阻断握手，允许连接建立，认证在STOMP阶段完成
        return true;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            logger.error("WebSocket握手后发生错误", exception);
        }
    }
}