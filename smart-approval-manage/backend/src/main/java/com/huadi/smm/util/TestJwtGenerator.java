package com.huadi.smm.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestJwtGenerator {
    public static void main(String[] args) {
        // 注意：如果 application.yml 中通过环境变量修改了 jwt.secret，请同步修改下面的 secret 值
        String secret = "medical-meeting-2026-secret-2026";

        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", 1L);
        claims.put("role", "admin");

        Date now = new Date();
        Date expiry = new Date(now.getTime() + 86400000L);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();

        System.out.println("============================================");
        System.out.println("请在浏览器控制台执行以下命令：");
        System.out.println();
        System.out.println("localStorage.setItem('token', '" + token + "');");
        System.out.println();
        System.out.println("============================================");
    }
}
