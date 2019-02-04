package com.yausername.youtubedl_android;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YoutubeDLOptions {

    private Map<String, String> options = new HashMap<>();

    public YoutubeDLOptions setOption(@NonNull String key, @NonNull String value){
        options.put(key, value);
        return this;
    }

    public YoutubeDLOptions setOption(@NonNull String key, @NonNull Number value){
        options.put(key, value.toString());
        return this;
    }

    public YoutubeDLOptions setOption(String key){
        options.put(key, null);
        return this;
    }

    public List<String> buildOptions(){
        List<String> optionsList = new ArrayList<>();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            optionsList.add(name);
            if(null != value) optionsList.add(value);
        }
        return optionsList;
    }

}
