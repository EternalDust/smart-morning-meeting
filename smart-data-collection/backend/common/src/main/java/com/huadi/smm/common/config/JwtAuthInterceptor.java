package com.huadi.smm.common.config;

import com.huadi.smm.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
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
            return reject(response, 401, "未提供有效的认证令牌");
        }

        String token = authHeader.substring(7);
        if (!JwtUtil.validateToken(token)) {
            return reject(response, 401, "Token无效或已过期");
        }

        // 解析角色，实现接口级权限隔离
        // 统一角色模型（README 约定）：工号 2 开头=管理员、1 开头=参会人，从 JWT 的 sub 读。
        // 兼容历史 token 的 role claim（admin/manager）。
        Claims claims = JwtUtil.parseToken(token);
        request.setAttribute("claims", claims);
        String userId = claims.getSubject() == null
                ? (claims.get("username") == null ? "" : claims.get("username").toString())
                : claims.getSubject();
        String role = claims.get("role") == null ? "" : claims.get("role").toString();
        boolean admin = userId.startsWith("2")
                || "admin".equals(role) || "manager".equals(role);

        if (isAdminOnly(request) && !admin) {
            return reject(response, 403, "无权限执行该操作，仅管理员可访问");
        }

        return true;
    }

    /**
     * 判定当前请求是否仅管理员可操作。
     * 规则：所有 GET（查询类）对任意已认证用户开放；
     * 管理类写操作（数据源增删改、清洗触发、规则管理、采集上报、异常处理、标签生成）仅 admin/manager。
     */
    private boolean isAdminOnly(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("GET".equals(method)) {
            return false;
        }
        if (path.startsWith("/api/v1/datasource") && !path.endsWith("/test")) {
            return true;
        }
        if (path.startsWith("/api/v1/cleaning/trigger")) {
            return true;
        }
        if (path.startsWith("/api/v1/cleaning-rule")) {
            return true;
        }
        if (path.startsWith("/api/v1/collect/report") || path.startsWith("/api/v1/collect/manual")) {
            return true;
        }
        if (path.startsWith("/api/v1/anomaly")) {
            return true;
        }
        if (path.startsWith("/api/v1/label/generate") || path.startsWith("/api/v1/label/nlp")) {
            return true;
        }
        return false;
    }

    private boolean reject(HttpServletResponse response, int code, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code);
        response.getWriter().write("{\"success\":false,\"code\":" + code
                + ",\"msg\":\"" + msg + "\"}");
        return false;
    }
}
