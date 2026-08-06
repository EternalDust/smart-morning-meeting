package com.huadi.smm.ai;

import com.huadi.smm.ai.dto.SpeechLine;
import com.huadi.smm.ai.dto.SummaryResult;

import java.util.List;

public interface AiClient {

    SummaryResult generateSummary(String meetingTitle, List<SpeechLine> speeches);

    String answerQuestion(String question, List<SpeechLine> context);
}
