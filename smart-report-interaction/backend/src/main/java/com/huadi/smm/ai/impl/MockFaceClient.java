package com.huadi.smm.ai.impl;

import com.huadi.smm.ai.FaceClient;
import com.huadi.smm.ai.dto.FaceResult;

public class MockFaceClient implements FaceClient {

    @Override
    public FaceResult recognize(byte[] image, String fileName, String expectedUserId) {
        FaceResult result = new FaceResult();
        result.setMatched(true);
        result.setUserId(expectedUserId != null && !expectedUserId.isEmpty() ? expectedUserId : "2001");
        result.setConfidence(0.97);
        result.setMessage("识别成功");
        return result;
    }
}
