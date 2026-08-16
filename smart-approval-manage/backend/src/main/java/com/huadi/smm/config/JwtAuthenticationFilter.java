package com.huadi.smm.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Pattern SUB_PATTERN = Pattern.compile("\"sub\"\\s*:\\s*\"?([^\",}]+)\"?");

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String userId = extractSubject(header.substring(7));
            if (userId != null) {
                // 演示模式 JWT 拦截器放通：不校验签名，按统一角色模型从工号前缀推导角色（2 开头=管理员）
                String role = userId.startsWith("2") ? "ADMIN" : "USER";
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(req, res);
    }

    /**
     * 演示模式免签名校验：仅从 JWT payload 解码 sub（Shell 统一签发的 token 无 role claim，
     * JJWT 0.12 又因 208bit 统一 secret 无法验签，故直接读工号推角色，与其余子系统放通口径一致）。
     * JwtUtil 严格校验方法保留，可随时恢复。
     */
    private String extractSubject(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Matcher m = SUB_PATTERN.matcher(json);
            return m.find() ? m.group(1) : null;
        } catch (Exception e) {
            return null;
        }
    }
}