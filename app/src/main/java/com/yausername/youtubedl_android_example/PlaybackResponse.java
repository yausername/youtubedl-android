package com.yausername.youtubedl_android_example;

/** Expected backend envelope: {"code":0,"message":"ok","data":{"playUrl":"https://...","mimeType":"application/vnd.apple.mpegurl","expiresAt":"..."}} */
public class PlaybackResponse {
    public int code;
    public String message;
    public PlaybackData data;

    public static class PlaybackData {
        public String playUrl;
        public String mimeType;
        public String expiresAt;
    }
}
