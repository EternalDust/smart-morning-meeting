package com.huadi.smm.controller;

import com.huadi.smm.ai.AsrClient;
import com.huadi.smm.ai.dto.AsrResult;
import com.huadi.smm.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping("/api/meeting/asr")
public class AsrController {

    @Resource
    private AsrClient asrClient;

    @PostMapping("/transcribe")
    public Result<?> transcribe(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail(400, "请上传音频文件");
        }
        try {
            AsrResult result = asrClient.transcribe(file.getBytes(), file.getOriginalFilename());
            return Result.ok(result);
        } catch (IOException e) {
            return Result.fail(400, "音频解析失败");
        }
    }
}
