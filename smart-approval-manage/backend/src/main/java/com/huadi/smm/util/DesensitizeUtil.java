package com.huadi.smm.util;

public class DesensitizeUtil {

    public static String desensitize(String text) {
        if (text == null || text.isEmpty()) return text;

        // 手机号
        text = text.replaceAll("(1[3-9]\\d)\\d{4}(\\d{4})", "$1****$2");

        // 身份证号 18位
        text = text.replaceAll("(\\d{4})\\d{10}(\\d{4}[\\dXx])", "$1**********$2");

        // 身份证号 15位
        text = text.replaceAll("(\\d{4})\\d{7}(\\d{4})", "$1*******$2");

        // 邮箱
        text = text.replaceAll("([a-zA-Z0-9])\\w+(@\\w+(?:\\.\\w+)+)", "$1****$2");

        // 姓名（2-4位汉字，保留姓）
        text = text.replaceAll("([\\u4e00-\\u9fa5])[\\u4e00-\\u9fa5]{1,2}(?=[\\u4e00-\\u9fa5]*[^\\u4e00-\\u9fa5]|$)",
                "$1*");

        return text;
    }
}