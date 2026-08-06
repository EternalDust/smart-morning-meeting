package com.huadi.smm.controller;

import com.huadi.smm.ai.FaceClient;
import com.huadi.smm.ai.dto.FaceResult;
import com.huadi.smm.common.Result;
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

    @PostMapping("/recognize")
    public Result<?> recognize(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail(400, "请上传人脸照片");
        }
        try {
            FaceResult result = faceClient.recognize(file.getBytes(), file.getOriginalFilename());
            return Result.ok(result);
        } catch (IOException e) {
            return Result.fail(400, "图片解析失败");
        }
    }
}
