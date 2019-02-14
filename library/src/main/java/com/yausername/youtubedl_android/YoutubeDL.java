package com.yausername.youtubedl_android;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.yausername.youtubedl_android.mapper.VideoInfo;
import com.yausername.youtubedl_android.utils.StreamGobbler;
import com.yausername.youtubedl_android.utils.StreamProcessExtractor;
import com.yausername.youtubedl_android.utils.YoutubeDLUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class YoutubeDL {

    private static final YoutubeDL INSTANCE = new YoutubeDL();
    private static final String baseName = "youtubedl-android";
    private static final String pythonName = "python";
    private static final String pythonBin = "usr/bin/python";
    private static final String youtubeDLName = "youtube-dl";

    private boolean initialized = false;
    private File pythonPath;
    private File youtubeDLPath;
    private String ENV_LD_LIBRARY_PATH;
    private String ENV_SSL_CERT_FILE;
    private YoutubeDLOptions globalOptions = new YoutubeDLOptions();

    private ObjectMapper objectMapper = new ObjectMapper();

    public static YoutubeDL getInstance() {
        return INSTANCE;
    }

    synchronized public void init(Application application) throws YoutubeDLException {
        if (initialized) return;

        initLogger();

        File baseDir = new File(application.getFilesDir(), baseName);
        File pythonDir = new File(baseDir, pythonName);
        pythonPath = new File(pythonDir, pythonBin);
        youtubeDLPath = new File(baseDir, youtubeDLName);
        ENV_LD_LIBRARY_PATH = pythonDir.getAbsolutePath() + "/usr/lib";
        ENV_SSL_CERT_FILE = pythonDir.getAbsolutePath() + "/usr/etc/tls/cert.pem";

        if (!pythonDir.exists()) {
            pythonDir.mkdir();
            try {
                YoutubeDLUtils.unzip(application.getResources().openRawResource(R.raw.python3_7_aarch64), pythonDir);
            } catch (IOException e) {
                throw new YoutubeDLException("failed to initialize", e);
            }
            pythonPath.setExecutable(true);
        }
        if (!youtubeDLPath.exists()) {
            try {
                YoutubeDLUtils.unzip(application.getResources().openRawResource(R.raw.youtube_dl), baseDir);
            } catch (IOException e) {
                throw new YoutubeDLException("failed to initialize", e);
            }
        }
        initialized = true;
    }

    private void initLogger() {
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, @Nullable String tag) {
                return BuildConfig.DEBUG;
            }
        });
    }

    private void assertInit() {
        if (!initialized) throw new IllegalStateException("instance not initialized");
    }

    public YoutubeDL setOption(@NonNull String key, @NonNull String value) {
        globalOptions.setOption(key, value);
        return this;
    }

    public YoutubeDL setOption(@NonNull String key, @NonNull Number value) {
        globalOptions.setOption(key, value);
        return this;
    }

    public YoutubeDL setOption(String key) {
        globalOptions.setOption(key);
        return this;
    }

    public VideoInfo getInfo(String url) throws YoutubeDLException {
        YoutubeDLRequest request = new YoutubeDLRequest(url);
        request.setOption("--dump-json");
        YoutubeDLResponse response = execute(request, null, true);

        VideoInfo videoInfo;

        try {
            videoInfo = objectMapper.readValue(response.getOut(), VideoInfo.class);
        } catch (IOException e) {
            throw new YoutubeDLException("Unable to parse video information", e);
        }

        return videoInfo;
    }

    public YoutubeDLResponse execute(YoutubeDLRequest request) throws YoutubeDLException {
        return execute(request, null, false);
    }

    public YoutubeDLResponse execute(YoutubeDLRequest request, @Nullable DownloadProgressCallback callback) throws YoutubeDLException {
        return execute(request, callback, false);
    }

    public YoutubeDLResponse execute(YoutubeDLRequest request, @Nullable DownloadProgressCallback callback, boolean ignoreGlobalOptions) throws YoutubeDLException {
        assertInit();

        YoutubeDLResponse youtubeDLResponse;
        Process process;
        int exitCode;
        StringBuffer outBuffer = new StringBuffer(); //stdout
        StringBuffer errBuffer = new StringBuffer(); //stderr
        long startTime = System.currentTimeMillis();

        List<String> args = ignoreGlobalOptions ? request.buildCommand() : request.buildCommand(globalOptions);
        List<String> command = new ArrayList<>();
        command.addAll(Arrays.asList(pythonPath.getAbsolutePath(), youtubeDLPath.getAbsolutePath()));
        command.addAll(args);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Map<String, String> env = processBuilder.environment();
        env.put("LD_LIBRARY_PATH", ENV_LD_LIBRARY_PATH);
        env.put("SSL_CERT_FILE", ENV_SSL_CERT_FILE);

        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new YoutubeDLException(e);
        }

        InputStream outStream = process.getInputStream();
        InputStream errStream = process.getErrorStream();

        StreamProcessExtractor stdOutProcessor = new StreamProcessExtractor(outBuffer, outStream, callback);
        StreamGobbler stdErrProcessor = new StreamGobbler(errBuffer, errStream);

        try {
            stdOutProcessor.join();
            stdErrProcessor.join();
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new YoutubeDLException(e);
        }

        String out = outBuffer.toString();
        String err = errBuffer.toString();

        if (exitCode > 0) {
            throw new YoutubeDLException(err);
        }

        long elapsedTime = System.currentTimeMillis() - startTime;

        youtubeDLResponse = new YoutubeDLResponse(command, exitCode, elapsedTime, out, err);

        return youtubeDLResponse;
    }
}
