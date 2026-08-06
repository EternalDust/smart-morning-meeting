package com.huadi.smm.ai.dto;

import lombok.Data;

@Data
public class FaceResult {
    private boolean matched;
    private String userId;
    private String name;
    private String role;
    private double confidence;
    private String message;
}
