package com.yausername.youtubedl_android_example;

/** Expected envelope: {"code":0,"message":"ok","data":{"playUrl":"https://...","mimeType":"application/vnd.apple.mpegurl","expiresAt":"..."}} */
public class PlaybackResponse {
    public int code;
    public String message;
    public PlaybackData data;

    public static class PlaybackData {
        public String playUrl;
        /** Optional MIME type, e.g. application/vnd.apple.mpegurl or application/dash+xml. */
        public String mimeType;
        /** Informational ISO-8601 expiry; the server/CDN must enforce expiry independently. */
        public String expiresAt;
    }
}
