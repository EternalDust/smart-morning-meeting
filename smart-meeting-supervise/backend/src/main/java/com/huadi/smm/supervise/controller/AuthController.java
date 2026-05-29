package com.huadi.smm.supervise.controller;

import com.huadi.smm.supervise.dto.LoginDto;
import com.huadi.smm.supervise.dto.Result;
import com.huadi.smm.supervise.entity.User;
import com.huadi.smm.supervise.mapper.UserMapper;
import com.huadi.smm.supervise.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDto loginDto) {
        // 从共享表 sm_gm_members 查询用户
        User user = userMapper.findByAccount(loginDto.getAccount());
        if (user == null) {
            return Result.fail(401, "账号不存在");
        }
        if (!loginDto.getPassword().equals(user.getPassword())) {
            return Result.fail(401, "密码错误");
        }

        // 生成 JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getAccount());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("userName", user.getName());
        result.put("role", user.getRole());
        result.put("token", token);

        return Result.ok(result);
    }
}