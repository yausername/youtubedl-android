package com.yausername.aria2c;

import android.content.Context;

import androidx.annotation.NonNull;

import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_common.SharedPrefsHelper;
import com.yausername.youtubedl_common.utils.ZipUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class Aria2c {

    private static final Aria2c INSTANCE = new Aria2c();
    protected static final String baseName = "youtubedl-android";
    private static final String packagesRoot = "packages";
    private static final String aria2cDirName = "aria2c";
    private static final String aria2cLibName = "libaria2c.zip.so";
    private static final String aria2cLibVersion = "aria2cLibVersion";

    private boolean initialized = false;
    private File binDir;

    private Aria2c() {
    }

    public static Aria2c getInstance() {
        return INSTANCE;
    }

    synchronized public void init(Context appContext) throws YoutubeDLException {
        if (initialized) return;

        final File baseDir = new File(appContext.getNoBackupFilesDir(), baseName);
        if (!baseDir.exists()) baseDir.mkdir();

        binDir = new File(appContext.getApplicationInfo().nativeLibraryDir);

        final File packagesDir = new File(baseDir, packagesRoot);
        final File aria2cDir = new File(packagesDir, aria2cDirName);
        initAria2c(appContext, aria2cDir);

        initialized = true;
    }

    private void initAria2c(Context appContext, File aria2cDir) throws YoutubeDLException {
        final File aria2cLib = new File(binDir, aria2cLibName);
        // using size of lib as version
        final String aria2cSize = String.valueOf(aria2cLib.length());
        if (!aria2cDir.exists() || shouldUpdateAria2c(appContext, aria2cSize)) {
            FileUtils.deleteQuietly(aria2cDir);
            aria2cDir.mkdirs();
            try {
                ZipUtils.unzip(aria2cLib, aria2cDir);
            } catch (Exception e) {
                FileUtils.deleteQuietly(aria2cDir);
                throw new YoutubeDLException("failed to initialize", e);
            }
            updateAria2c(appContext, aria2cSize);
        }
    }

    private boolean shouldUpdateAria2c(@NonNull Context appContext, @NonNull String version) {
        return !version.equals(SharedPrefsHelper.get(appContext, aria2cLibVersion));
    }

    private void updateAria2c(@NonNull Context appContext, @NonNull String version) {
        SharedPrefsHelper.update(appContext, aria2cLibVersion, version);
    }
}
