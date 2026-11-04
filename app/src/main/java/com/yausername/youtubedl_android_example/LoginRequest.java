package com.yausername.youtubedl_android_example;

/** Login request for the application's own backend. */
public class LoginRequest {
    public final String username;
    public final String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
