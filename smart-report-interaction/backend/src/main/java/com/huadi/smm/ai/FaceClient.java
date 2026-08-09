package com.huadi.smm.ai;

import com.huadi.smm.ai.dto.FaceResult;

public interface FaceClient {

    FaceResult recognize(byte[] image, String fileName, String expectedUserId);
}
