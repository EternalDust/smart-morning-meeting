package com.huadi.smm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huadi.smm.common.ApiResponse;
import com.huadi.smm.entity.SysUser;
import com.huadi.smm.service.SysUserService;
import com.huadi.smm.utils.JwtUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {
        QueryWrapper<SysUser> query = new QueryWrapper<>();
        query.eq("username", request.getUsername())
             .eq("password", request.getPassword());
        
        SysUser user = sysUserService.getOne(query);
        if (user == null) {
            return ApiResponse.error(401, "用户名或密码错误");
        }

        // 签发 Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("roleId", user.getRoleId());
        claims.put("deptId", user.getDeptId());
        claims.put("deptName", user.getDeptName());

        String token = JwtUtils.generateToken(claims);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);

        return ApiResponse.success(result);
    }
}

@Data
class LoginRequest {
    private String username;
    private String password;
}