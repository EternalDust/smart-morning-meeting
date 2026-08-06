package com.huadi.smm.ai.impl;

import com.huadi.smm.ai.AsrClient;
import com.huadi.smm.ai.dto.AsrResult;

public class MockAsrClient implements AsrClient {

    @Override
    public AsrResult transcribe(byte[] audio, String fileName) {
        AsrResult result = new AsrResult();
        result.setDuration(18.6);
        result.setText("各位早好，我先把内科上周的工作情况汇报一下。上周门诊量较前一周增长约百分之八，其中慢病复诊患者明显增多。住院部床位使用率保持在百分之九十二左右，周转比较顺利。本周重点关注高血压患者的用药调整和随访安排，计划周四前完成所有出院患者的复查预约。另外检验科反馈血常规报告平均出具时间已压缩到四十分钟以内，请各科室配合做好标本送检登记，确保危急值及时电话通知。以上就是我的汇报，谢谢。");
        return result;
    }
}
