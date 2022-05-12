package com.yausername.youtubedl_android.utils;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {

    private Utils() {

    }

    public static float getProgress(@NonNull final String line) {
        final float progress = 0;
        final Pattern pattern = Pattern.compile("\\[download\\](.*?)%", Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return Float.parseFloat(matcher.group(1));
        }
        return progress;
    }

}
