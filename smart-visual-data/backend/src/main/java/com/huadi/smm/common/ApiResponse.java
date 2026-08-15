package com.huadi.smm.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体，格式与团队 README 保持一致：
 * { "success": true, "code": 200, "msg": "success", "data": {} }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, "success", data);
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(false, code, msg, null);
    }
}
