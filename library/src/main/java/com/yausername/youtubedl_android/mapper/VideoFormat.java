package com.yausername.youtubedl_android.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoFormat {

    public int asr;
    public int tbr;
    public int abr;
    public String format;
    @JsonProperty("format_id")
    public String formatId;
    @JsonProperty("format_note")
    public String formatNote;
    public String ext;
    public int preference;
    public String vcodec;
    public String acodec;
    public int width;
    public int height;
    public long filesize;
    public int fps;
    public String url;
    @JsonProperty("manifest_url")
    public String manifestUrl;
}
