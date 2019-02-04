package com.yausername.youtubedl_android.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpHeader {

    @JsonProperty("Accept-Charset")
    public String acceptCharset;
    @JsonProperty("Accept-Language")
    public String acceptLanguage;
    @JsonProperty("Accept-Encoding")
    public String acceptEncoding;
    @JsonProperty("Accept")
    public String accept;
    @JsonProperty("User-Agent")
    public String userAgent;
}