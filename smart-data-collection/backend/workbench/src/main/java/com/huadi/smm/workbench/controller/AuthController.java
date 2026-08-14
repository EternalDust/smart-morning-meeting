package com.huadi.smm.workbench.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.common.entity.Member;
import com.huadi.smm.common.entity.R;
import com.huadi.smm.common.utils.JwtUtil;
import com.huadi.smm.workbench.dao.MemberMapper;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private MemberMapper memberMapper;

    /**
     * 登录——统一账号体系，查询共享人员表 sm_gm_members
     */
    @PostMapping("/login")
    public R login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isEmpty()) {
            return R.error(400, "工号不能为空");
        }
        if (password == null || password.isEmpty()) {
            return R.error(400, "密码不能为空");
        }

        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>().eq(Member::getUserId, username));
        if (member == null || !password.equals(member.getPassword())) {
            return R.error(401, "工号或密码错误");
        }
        if (member.getStatus() != null && member.getStatus() == 0) {
            return R.error(403, "账号已禁用");
        }

        String role = mapRole(member.getRole());
        String token = JwtUtil.generateToken(member.getId(), member.getUserId(), role);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", member.getId());
        userInfo.put("username", member.getUserId());
        userInfo.put("name", member.getName());
        userInfo.put("role", role);
        userInfo.put("dept", member.getDept());
        userInfo.put("avatar", "");

        return R.ok(userInfo).message("登录成功").token(token);
    }

    @GetMapping("/info")
    public R getUserInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!JwtUtil.validateToken(token)) {
            return R.error(401, "Token无效或已过期");
        }
        Claims claims = JwtUtil.parseToken(token);
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

    /**
     * 共享表角色映射：1 管理层 → admin，其余（2 科室人员）→ operator
     */
    private String mapRole(Integer role) {
        return role != null && role == 1 ? "admin" : "operator";
    }
}
