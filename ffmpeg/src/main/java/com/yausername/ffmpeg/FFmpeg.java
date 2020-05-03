package com.yausername.ffmpeg;

import android.app.Application;

import androidx.annotation.Nullable;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.utils.YoutubeDLUtils;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class FFmpeg {

    private static final FFmpeg INSTANCE = new FFmpeg();
    protected static final String baseName = "youtubedl-android";
    private static final String packagesRoot = "packages";
    private static final String ffmegDirName = "ffmpeg";
    private static final String ffmpegLib = "libffmpeg.zip.so";

    private boolean initialized = false;
    private File binDir;

    private FFmpeg(){
    }

    public static FFmpeg getInstance(){
        return INSTANCE;
    }

    synchronized public void init(Application application) throws YoutubeDLException {
        if (initialized) return;

        initLogger();

        File baseDir = new File(application.getNoBackupFilesDir(), baseName);
        if(!baseDir.exists()) baseDir.mkdir();

        binDir = new File(application.getApplicationInfo().nativeLibraryDir);

        File packagesDir = new File(baseDir, packagesRoot);
        File ffmpegDir = new File(packagesDir, ffmegDirName);
        initFFmpeg(application, ffmpegDir);

        initialized = true;
    }

    private void initFFmpeg(Application application, File ffmpegDir) throws YoutubeDLException {
        if (!ffmpegDir.exists()) {
            ffmpegDir.mkdirs();
            try {
                new ZipFile(new File(binDir, ffmpegLib)).extractAll(ffmpegDir.getAbsolutePath());
            } catch (IOException e) {
                YoutubeDLUtils.delete(ffmpegDir);
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
