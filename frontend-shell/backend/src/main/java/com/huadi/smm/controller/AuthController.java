package com.huadi.smm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.dao.MemberMapper;
import com.huadi.smm.entity.Member;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String SECRET = "smart-morning-meeting-2026";
    private static final long EXPIRATION = 86400000;

    @Resource
    private MemberMapper memberMapper;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String password = body.getOrDefault("password", "");

        LambdaQueryWrapper<Member> qw = new LambdaQueryWrapper<>();
        qw.eq(Member::getUserId, userId);
        Member member = memberMapper.selectOne(qw);

        Map<String, Object> result = new HashMap<>();
        if (member == null || !password.equals(member.getPassword())) {
            result.put("success", false);
            result.put("code", 401);
            result.put("msg", "工号或密码错误");
            return result;
        }

        String token = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();

        result.put("success", true);
        result.put("code", 200);
        result.put("msg", "success");
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("userName", member.getName());
        data.put("userId", userId);
        result.put("data", data);
        return result;
    }
}
