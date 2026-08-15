package com.huadi.smm.supervise.controller;

import com.huadi.smm.supervise.dto.Result;
import com.huadi.smm.supervise.entity.User;
import com.huadi.smm.supervise.mapper.UserMapper;
import com.huadi.smm.supervise.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supervise/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    /**
     * 按工号查询用户（演示/登录态解析用）
     * GET /api/supervise/user/by-account/{account}
     */
    @GetMapping("/by-account/{account}")
    public Result<UserVo> getByAccount(@PathVariable String account) {
        User user = userMapper.findByUserId(account);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        UserVo vo = new UserVo();
        vo.setId(user.getId());
        vo.setUserId(user.getUserId());
        vo.setName(user.getName());
        vo.setDept(user.getDept());
        vo.setRole(user.getRole());
        return Result.ok(vo);
    }
}
