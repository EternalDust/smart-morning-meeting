package com.huadi.smm.config;

import lombok.Data;

@Data
public class Result<T> {
    private boolean success;
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setSuccess(true);
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(String msg, int code) {
        Result<T> r = new Result<>();
        r.setSuccess(false);
        r.setCode(code);
        r.setMsg(msg);
        r.setData(null);
        return r;
    }

    public static <T> Result<T> fail(String msg) {
        return fail(msg, 400);
    }
}