package com.huadi.smm.workbench.controller;

import com.huadi.smm.common.entity.R;
import com.huadi.smm.common.utils.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Set<String> VALID_USERS = Set.of(
        "admin", "manager", "zhangyi", "liuyi", "wangfang", "chenwei", "zhaoxin"
    );

    /**
     * 登录接口——开发阶段简化实现
     * 生产环境应接入数据库用户表进行验证
     */
    @PostMapping("/login")
    public R login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isEmpty()) {
            return R.error(400, "用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            return R.error(400, "密码不能为空");
        }

        if (!VALID_USERS.contains(username)) {
            return R.error(401, "用户不在允许的测试名单中");
        }

        // 开发阶段使用固定账号
        String role = determineRole(username);
        String token = JwtUtil.generateToken(1L, username, role);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", 1L);
        userInfo.put("username", username);
        userInfo.put("role", role);
        userInfo.put("avatar", "");

        return R.ok(userInfo).message("登录成功").token(token);
    }

    @GetMapping("/info")
    public R getUserInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!JwtUtil.validateToken(token)) {
            return R.error(401, "Token无效或已过期");
        }
        var claims = JwtUtil.parseToken(token);
        Map<String, Object> info = new HashMap<>();
        info.put("userId", claims.get("userId"));
        info.put("username", claims.get("username"));
        info.put("role", claims.get("role"));
        return R.ok(info);
    }

    @PostMapping("/logout")
    public R logout() {
        return R.ok().message("已退出登录");
    }

    private String determineRole(String username) {
        if ("admin".equals(username)) return "admin";
        if ("manager".equals(username)) return "manager";
        return "operator";
    }
}
