package com.iot.platform.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long tokenValidityInMilliseconds;

    public JwtUtil(@Value("${jwt.expiration}") long tokenValidityInMilliseconds) {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        this.tokenValidityInMilliseconds = tokenValidityInMilliseconds;
    }

    /**
     * 生成JWT令牌
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDetails.getUsername());
        claims.put("authorities", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }

        String username = claims.get("username", String.class);
        Date expiration = claims.getExpiration();
        
        return username.equals(userDetails.getUsername()) 
                && expiration.after(new Date());
    }

    /**
     * 解析令牌
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("JWT令牌解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 创建令牌
     */
    private String createToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        Date validity = new Date(now + tokenValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(validity)
                .signWith(key)
                .compact();
    }

    /**
     * 判断令牌是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        return claims != null && claims.getExpiration().before(new Date());
    }

    /**
     * 刷新令牌
     */
    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        claims.setIssuedAt(new Date());
        claims.setExpiration(new Date(System.currentTimeMillis() + tokenValidityInMilliseconds));
        
        return Jwts.builder()
                .setClaims(claims)
                .signWith(key)
                .compact();
    }
} 