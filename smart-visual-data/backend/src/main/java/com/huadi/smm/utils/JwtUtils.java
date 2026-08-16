package com.huadi.smm.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

public class JwtUtils {

    // 密钥及过期时间（与统一壳、汇报、审批、督办共用同一密钥，HS256 保证跨子系统可解析）
    private static final String SECRET = "smart-morning-meeting-2026";
    private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24; // 24小时

    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    /**
     * 生成带 subject（工号）的统一 JWT，与全平台（统一壳、汇报、审批）保持一致：
     * sub = 工号，密钥 = smart-morning-meeting-2026，有效期 24 小时
     */
    public static String generateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}
