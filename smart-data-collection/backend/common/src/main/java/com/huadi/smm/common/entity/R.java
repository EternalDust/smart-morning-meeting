package com.huadi.smm.common.entity;

import lombok.Data;

@Data
public class R {

    private boolean success;
    private int code;
    private String msg;
    private Object data;
    private String token;

    private R() {}

    public static R ok() {
        R r = new R();
        r.success = true;
        r.code = 200;
        r.msg = "success";
        return r;
    }

    public static R ok(Object data) {
        R r = ok();
        r.data = data;
        return r;
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.success = false;
        r.code = code;
        r.msg = msg;
        return r;
    }

    public static R error(String msg) {
        return error(500, msg);
    }

    public R message(String msg) {
        this.msg = msg;
        return this;
    }

    public R data(Object data) {
        this.data = data;
        return this;
    }

    public R token(String token) {
        this.token = token;
        return this;
    }
}
