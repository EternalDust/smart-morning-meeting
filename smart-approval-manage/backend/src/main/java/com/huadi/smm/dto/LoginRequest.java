package com.huadi.smm.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}