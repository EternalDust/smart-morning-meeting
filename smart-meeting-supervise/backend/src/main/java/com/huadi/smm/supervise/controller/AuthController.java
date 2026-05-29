package com.huadi.smm.supervise.controller;

import com.huadi.smm.supervise.dto.LoginDto;
import com.huadi.smm.supervise.dto.Result;
import com.huadi.smm.supervise.entity.User;
import com.huadi.smm.supervise.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserMapper userMapper;

    public AuthController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDto loginDto) {
        User user = userMapper.findByAccount(loginDto.getAccount());
        if (user == null || !loginDto.getPassword().equals(user.getPassword())) {
            return Result.error(401, "账号或密码错误");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("userName", user.getName());
        result.put("role", user.getRole());
        // TODO: 生成JWT Token
        return Result.success(result);
    }
}