package com.yausername.ffmpeg;

import android.app.Application;
import android.support.annotation.Nullable;

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
    private static final String ffmpegBin = "usr/bin/ffmpeg";

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
        binDir = new File(packagesDir, "usr/bin");
        ffmpegPath = new File(packagesDir, ffmpegBin);

        initFFmpeg(application, packagesDir);

        initialized = true;
    }

    private void initFFmpeg(Application application, File packagesDir) throws YoutubeDLException {
        if (!ffmpegPath.exists()) {
            if (!packagesDir.exists()) {
                packagesDir.mkdirs();
            }
            try {
                YoutubeDLUtils.unzip(application.getResources().openRawResource(R.raw.ffmpeg_arm), packagesDir);
            } catch (IOException e) {
                // delete for recovery later
                YoutubeDLUtils.delete(ffmpegPath);
                throw new YoutubeDLException("failed to initialize", e);
            }
            markExecutable(binDir);
        }
    }

    private void markExecutable(File binDir) {
        File[] directoryListing = binDir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if(!child.isDirectory()) child.setExecutable(true);
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
