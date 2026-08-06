package com.huadi.smm.ai.impl;

import com.huadi.smm.ai.FaceClient;
import com.huadi.smm.ai.dto.FaceResult;

public class MockFaceClient implements FaceClient {

    @Override
    public FaceResult recognize(byte[] image, String fileName) {
        FaceResult result = new FaceResult();
        result.setMatched(true);
        result.setUserId("2001");
        result.setName("杨辉");
        result.setRole("管理员");
        result.setConfidence(0.97);
        result.setMessage("识别成功");
        return result;
    }
}
