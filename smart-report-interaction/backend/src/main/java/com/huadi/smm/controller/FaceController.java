package com.huadi.smm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.ai.FaceClient;
import com.huadi.smm.ai.dto.FaceResult;
import com.huadi.smm.common.Result;
import com.huadi.smm.dao.MemberMapper;
import com.huadi.smm.entity.Member;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping("/api/meeting/face")
public class FaceController {

    @Resource
    private FaceClient faceClient;

    @Resource
    private MemberMapper memberMapper;

    @PostMapping("/recognize")
    public Result<?> recognize(@RequestParam("file") MultipartFile file,
                               @RequestParam(required = false) String userId) {
        if (file == null || file.isEmpty()) {
            return Result.fail(400, "请上传人脸照片");
        }
        try {
            FaceResult result = faceClient.recognize(file.getBytes(), file.getOriginalFilename(), userId);
            if (result.isMatched() && result.getUserId() != null) {
                Member member = memberMapper.selectOne(new LambdaQueryWrapper<Member>()
                        .eq(Member::getUserId, result.getUserId()));
                if (member != null) {
                    result.setName(member.getName());
                }
                result.setRole(result.getUserId().startsWith("2") ? "管理员" : "参会人员");
            }
            return Result.ok(result);
        } catch (IOException e) {
            return Result.fail(400, "图片解析失败");
        }
    }
}
