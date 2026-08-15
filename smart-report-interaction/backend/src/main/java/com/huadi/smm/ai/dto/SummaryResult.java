package com.huadi.smm.ai.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SummaryResult {
    private String summary = "";
    private List<String> keyPoints = new ArrayList<>();
    private List<String> decisions = new ArrayList<>();
    private List<String> medicalEntities = new ArrayList<>();
}
