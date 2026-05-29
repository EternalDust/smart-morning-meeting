package com.huadi.smm.controller;

import com.huadi.smm.config.JwtUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String token = jwtUtil.generateToken(username);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("code", 200);
        result.put("msg", "success");
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("username", username);
        result.put("data", data);
        return result;
    }
}
