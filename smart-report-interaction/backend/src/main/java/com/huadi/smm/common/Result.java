package com.huadi.smm.common;

import lombok.Data;

@Data
public class Result<T> {
    private boolean success;
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.success = true;
        r.code = 200;
        r.msg = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.success = false;
        r.code = code;
        r.msg = msg;
        return r;
    }
}
