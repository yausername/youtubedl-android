package com.yausername.youtubedl_android_example;

/** Expected login envelope: {"code":0,"message":"ok","data":{"token":"..."}} */
public class LoginResponse {
    public int code;
    public String message;
    public LoginData data;

    public static class LoginData {
        public String token;
    }
}
