package com.huadi.smm.controller;

import com.huadi.smm.config.JwtUtil;
import com.huadi.smm.config.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> req) {
        String workNo = req.get("workNo");
        String password = req.get("password");

        if (workNo == null || password == null) {
            return Result.fail("工号或密码不能为空", 400);
        }

        boolean isAdmin = workNo.startsWith("2");
        boolean valid = (isAdmin && "admin".equals(password)) || (!isAdmin && "123456".equals(password));
        if (!valid) {
            return Result.fail("工号或密码错误", 401);
        }

        Long userId;
        try {
            userId = Long.valueOf(workNo);
        } catch (NumberFormatException e) {
            return Result.fail("工号格式错误", 400);
        }

        String role = isAdmin ? "ADMIN" : "USER";
        String token = JwtUtil.generateToken(userId, role);

        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("workNo", workNo);
        data.put("role", role);
        return Result.ok(data);
    }
}