package com.huadi.smm.ai;

import com.huadi.smm.ai.dto.AsrResult;

public interface AsrClient {

    AsrResult transcribe(byte[] audio, String fileName);
}
