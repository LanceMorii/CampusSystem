package com.example.campussystem.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 负责JWT token的生成、验证和解析
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    /**
     * 生成JWT token
     *
     * @param userId 用户ID
     * @param studentId 学号
     * @param username 用户名
     * @return JWT token
     */
    public String generateToken(Long userId, String studentId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("studentId", studentId);
        claims.put("username", username);
        claims.put("role", "USER");
        
        return createToken(claims, studentId);
    }

    /**
     * 生成管理员JWT token
     *
     * @param userId 用户ID
     * @param studentId 学号
     * @param username 用户名
     * @return JWT token
     */
    public String generateAdminToken(Long userId, String studentId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("studentId", studentId);
        claims.put("username", username);
        claims.put("role", "ADMIN");
        
        return createToken(claims, studentId);
    }

    /**
     * 创建token
     *
     * @param claims 声明
     * @param subject 主题
     * @return JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从token中获取用户ID
     *
     * @param token JWT token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? Long.valueOf(claims.get("userId").toString()) : null;
    }

    /**
     * 从token中获取学号
     *
     * @param token JWT token
     * @return 学号
     */
    public String getStudentIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从token中获取用户名
     *
     * @param token JWT token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("username") : null;
    }

    /**
     * 从token中获取角色
     *
     * @param token JWT token
     * @return 角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    /**
     * 从token中获取过期时间
     *
     * @param token JWT token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 从token中获取声明
     *
     * @param token JWT token
     * @return 声明
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token已过期: {}", e.getMessage());
            return null;
        } catch (UnsupportedJwtException e) {
            logger.warn("不支持的JWT token: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            logger.warn("JWT token格式错误: {}", e.getMessage());
            return null;
        } catch (SecurityException e) {
            logger.warn("JWT token签名验证失败: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            logger.warn("JWT token参数错误: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证token是否过期
     *
     * @param token JWT token
     * @return true表示已过期，false表示未过期
     */
    public Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }

    /**
     * 验证token是否有效
     *
     * @param token JWT token
     * @param studentId 学号
     * @return true表示有效，false表示无效
     */
    public Boolean validateToken(String token, String studentId) {
        String tokenStudentId = getStudentIdFromToken(token);
        return (studentId.equals(tokenStudentId) && !isTokenExpired(token));
    }

    /**
     * 验证token是否有效（不验证用户）
     *
     * @param token JWT token
     * @return true表示有效，false表示无效
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("JWT token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从请求头中获取token
     *
     * @param authHeader 认证头
     * @return JWT token
     */
    public String getTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(prefix + " ")) {
            return authHeader.substring(prefix.length() + 1);
        }
        return null;
    }

    /**
     * 获取签名密钥
     *
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 刷新token
     *
     * @param token 原token
     * @return 新token
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }

        Long userId = Long.valueOf(claims.get("userId").toString());
        String studentId = claims.getSubject();
        String username = (String) claims.get("username");
        String role = (String) claims.get("role");

        if ("ADMIN".equals(role)) {
            return generateAdminToken(userId, studentId, username);
        } else {
            return generateToken(userId, studentId, username);
        }
    }

    // Getter方法
    public String getHeader() {
        return header;
    }

    public String getPrefix() {
        return prefix;
    }

    public Long getExpiration() {
        return expiration;
    }
}