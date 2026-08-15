package com.huadi.smm.controller;

import com.huadi.smm.config.Result;
import com.huadi.smm.entity.GmMember;
import com.huadi.smm.service.GmMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class GmMemberController {

    @Autowired
    private GmMemberService gmMemberService;

    @GetMapping
    public Result<List<GmMember>> list() {
        return Result.ok(gmMemberService.list());
    }
}