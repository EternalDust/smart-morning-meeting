package com.huadi.smm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huadi.smm.common.ApiResponse;
import com.huadi.smm.entity.SmGmMember;
import com.huadi.smm.service.SmGmMemberService;
import com.huadi.smm.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录接口（账号体系统一后对接共享表 sm_gm_members）
 *
 * 常规入口是统一壳登录（前端 5000 端口），此处保留 /api/auth/login 供
 * 大屏独立联调使用：按工号 + 密码校验共享表，签发 sub=工号 的统一 JWT。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SmGmMemberService smGmMemberService;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {
        QueryWrapper<SmGmMember> query = new QueryWrapper<>();
        query.eq("user_id", request.getUserId()).eq("password", request.getPassword());
        SmGmMember member = smGmMemberService.getOne(query);

        if (member == null) {
            return ApiResponse.error(401, "工号或密码错误");
        }
        if (member.getStatus() != null && member.getStatus() == 0) {
            return ApiResponse.error(403, "账号已禁用");
        }

        // 签发统一 JWT：subject = 工号，密钥/有效期与全平台一致
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", member.getName());
        String token = JwtUtils.generateToken(claims, member.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", Map.of(
            "userId", member.getUserId(),
            "name", member.getName(),
            "role", member.getRole(),
            "dept", member.getDept() == null ? "" : member.getDept()
        ));
        return ApiResponse.success(result);
    }

    /** 当前登录用户信息（供大屏展示登录身份与数据范围） */
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(HttpServletRequest request) {
        SmGmMember user = smGmMemberService.getOne(
            new QueryWrapper<SmGmMember>().eq("user_id", resolveSubject(request)).last("LIMIT 1"));
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserId());
        result.put("name", user.getName());
        result.put("role", user.getRole());
        result.put("dept", user.getDept());
        result.put("scope", user.getRole() != null && user.getRole() == 1 ? "全院数据" : "本科室数据");
        return ApiResponse.success(result);
    }

    private String resolveSubject(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                return JwtUtils.parseToken(token.substring(7)).getSubject();
            } catch (Exception ignored) {
            }
        }
        return "";
    }
}

@Data
class LoginRequest {
    private String userId;
    private String password;
}
