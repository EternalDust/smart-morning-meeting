package com.huadi.smm.config;

@SuppressWarnings("unused")
public class JwtRoleUtil {

    public static boolean isAdmin(String token) {
        String workNo = parseWorkNo(token);
        return workNo != null && workNo.startsWith("2");
    }

    public static boolean isAttendee(String token) {
        String workNo = parseWorkNo(token);
        return workNo != null && workNo.startsWith("1");
    }

    public static String parseWorkNo(String token) {
        if (token == null || !token.startsWith("Bearer ")) return null;
        return JwtUtil.parseUserId(token.substring(7));
    }
}