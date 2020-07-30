package com.yausername.youtubedl_android.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpHeader {

    @JsonProperty("Accept-Charset")
    private String acceptCharset;
    @JsonProperty("Accept-Language")
    private String acceptLanguage;
    @JsonProperty("Accept-Encoding")
    private String acceptEncoding;
    @JsonProperty("Accept")
    private String accept;
    @JsonProperty("User-Agent")
    private String userAgent;

    public String getAcceptCharset() {
        return acceptCharset;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public String getAcceptEncoding() {
        return acceptEncoding;
    }

    public String getAccept() {
        return accept;
    }

    public String getUserAgent() {
        return userAgent;
    }
}