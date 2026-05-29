package com.huadi.smm.common.config;

import com.huadi.smm.common.utils.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final String[] EXCLUDE_PATHS = {
        "/api/v1/auth/login"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        for (String exclude : EXCLUDE_PATHS) {
            if (path.equals(exclude)) {
                return true;
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"success\":false,\"code\":401,\"msg\":\"未提供有效的认证令牌\"}");
            return false;
        }

        String token = authHeader.substring(7);
        if (!JwtUtil.validateToken(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"success\":false,\"code\":401,\"msg\":\"Token无效或已过期\"}");
            return false;
        }

        return true;
    }
}
