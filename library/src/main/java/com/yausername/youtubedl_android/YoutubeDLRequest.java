package com.yausername.youtubedl_android;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YoutubeDLRequest {

    private List<String> urls;
    private YoutubeDLOptions options = new YoutubeDLOptions();

    public YoutubeDLRequest(String url) {
        this.urls = Arrays.asList(url);
    }

    public YoutubeDLRequest(@NonNull  List<String> urls) {
        this.urls = urls;
    }

    public YoutubeDLRequest setOption(@NonNull String key, @NonNull String value){
        options.setOption(key, value);
        return this;
    }

    public YoutubeDLRequest setOption(@NonNull String key, @NonNull Number value){
        options.setOption(key, value);
        return this;
    }

    public YoutubeDLRequest setOption(String key){
        options.setOption(key);
        return this;
    }

    public List<String> buildCommand(){
        List<String> command = new ArrayList<>();
        command.addAll(options.buildOptions());
        command.addAll(urls);
        return command;
    }

}
