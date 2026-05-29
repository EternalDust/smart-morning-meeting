package com.huadi.smm.controller;

import com.huadi.smm.config.JwtUtil;
import com.huadi.smm.config.Result;
import com.huadi.smm.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agenda/public")
public class LoginController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest req) {
        // TODO: 后续对接 sm_gm_members 共享用户表时，改成查数据库校验
        if ("admin".equals(req.getUsername()) && "admin123".equals(req.getPassword())) {
            String token = jwtUtil.generateToken(1L, "admin");
            return Result.ok(token);
        }
        return Result.fail("用户名或密码错误", 401);
    }
}