package com.huadi.smm.supervise.utils;

import com.huadi.smm.supervise.entity.User;
import com.huadi.smm.supervise.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 当前登录身份解析：优先取 JWT 的 sub（工号），其次取 X-Account 请求头，
 * 最后取请求参数兜底。演示模式用 X-Account 模拟登录态。
 */
@Component
public class CurrentUser {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    public User resolve(HttpServletRequest request, String fallbackAccount) {
        String account = null;
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                account = jwtUtils.getClaimsFromToken(auth.substring(7)).getSubject();
            } catch (Exception ignored) {
            }
        }
        if (!StringUtils.hasText(account)) {
            String xa = request.getHeader("X-Account");
            if (StringUtils.hasText(xa)) {
                account = xa;
            }
        }
        if (!StringUtils.hasText(account)) {
            account = fallbackAccount;
        }
        if (!StringUtils.hasText(account)) {
            return null;
        }
        return userMapper.findByUserId(account);
    }

    /**
     * 管理员（工号 2 开头）承担督办专员职责
     */
    public boolean isAdmin(HttpServletRequest request, String fallbackAccount) {
        User user = resolve(request, fallbackAccount);
        return user != null && user.getUserId() != null && user.getUserId().startsWith("2");
    }
}
