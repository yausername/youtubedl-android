package com.yausername.ffmpeg;

import android.app.Application;

import androidx.annotation.Nullable;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.utils.YoutubeDLUtils;

import java.io.File;
import java.io.IOException;

public class FFmpeg {

    private static final FFmpeg INSTANCE = new FFmpeg();
    protected static final String baseName = "youtubedl-android";
    private static final String packagesRoot = "packages";
    private static final String ffmpegBin = "libffmpeg.bin.so";
    private static final String ffmpegLib = "libffmpeg.zip.so";

    private boolean initialized = false;
    private File binDir;
    private File ffmpegPath;

    private FFmpeg(){
    }

    public static FFmpeg getInstance(){
        return INSTANCE;
    }

    synchronized public void init(Application application) throws YoutubeDLException {
        if (initialized) return;

        initLogger();

        File baseDir = new File(application.getFilesDir(), baseName);
        if(!baseDir.exists()) baseDir.mkdir();

        File packagesDir = new File(baseDir, packagesRoot);
        binDir = new File(application.getApplicationInfo().nativeLibraryDir);
        ffmpegPath = new File(binDir, ffmpegBin);

        initFFmpeg(application, packagesDir);

        initialized = true;
    }

    private void initFFmpeg(Application application, File packagesDir) throws YoutubeDLException {
       File exists = new File(packagesDir, ".ffmpeg");
        if (!exists.exists()) {
            if (!packagesDir.exists()) {
                packagesDir.mkdirs();
            }
            try {
                YoutubeDLUtils.unzip(new File(binDir, ffmpegLib), packagesDir);
                exists.createNewFile();
            } catch (IOException e) {
                throw new YoutubeDLException("failed to initialize", e);
            }
        }
    }

    private void initLogger() {
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, @Nullable String tag) {
                return BuildConfig.DEBUG;
            }
        });
    }
}
