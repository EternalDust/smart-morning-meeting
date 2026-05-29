package com.huadi.smm.supervise.dto;

import lombok.Data;

@Data
public class Result<T> {
    private boolean success;
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> ok(String msg, T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> fail(String msg) {
        return fail(500, msg);
    }
}