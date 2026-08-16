package com.huadi.smm.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huadi.smm.entity.SmGmMember;
import com.huadi.smm.mapper.SmGmMemberMapper;
import com.huadi.smm.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 当前登录用户上下文。
 *
 * 账号体系统一后，可视化不再维护独立账号表，而是解析共享 JWT：
 * token subject = 工号（sm_gm_members.user_id），再查共享表得到角色与科室。
 *
 * 演示模式（未携带 token）视为管理层，可看全院数据，便于大屏独立联调。
 */
@Component
@RequiredArgsConstructor
public class UserContext {

    private final SmGmMemberMapper smGmMemberMapper;

    /** 解析当前登录用户；无 token（演示模式）返回 null */
    public SmGmMember currentUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        try {
            Claims claims = JwtUtils.parseToken(token.substring(7));
            String userId = claims.getSubject();
            if (userId == null || userId.isBlank()) {
                return null;
            }
            return smGmMemberMapper.selectOne(
                new QueryWrapper<SmGmMember>().eq("user_id", userId).last("LIMIT 1"));
        } catch (Exception e) {
            return null;
        }
    }

    /** 是否管理层（role=1）；无 token 演示模式视为管理层 */
    public boolean isAdmin(HttpServletRequest request) {
        SmGmMember user = currentUser(request);
        return user == null || (user.getRole() != null && user.getRole() == 1);
    }

    /** 当前用户科室（字符串），管理层返回 null 表示不限制；演示模式返回 null */
    public String currentDept(HttpServletRequest request) {
        SmGmMember user = currentUser(request);
        return user != null && user.getRole() != null && user.getRole() == 2 ? user.getDept() : null;
    }

    /** 当前工号；无 token 演示模式返回 null */
    public String currentUserId(HttpServletRequest request) {
        SmGmMember user = currentUser(request);
        return user != null ? user.getUserId() : null;
    }

    /** 当前用户名；无 token 演示模式返回 admin */
    public String currentName(HttpServletRequest request) {
        SmGmMember user = currentUser(request);
        return user != null && user.getName() != null ? user.getName() : "admin";
    }
}
