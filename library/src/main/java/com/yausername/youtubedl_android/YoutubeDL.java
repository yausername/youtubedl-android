package com.yausername.youtubedl_android;

import android.app.Application;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yausername.youtubedl_android.mapper.VideoInfo;
import com.yausername.youtubedl_android.utils.StreamGobbler;
import com.yausername.youtubedl_android.utils.StreamProcessExtractor;
import com.yausername.youtubedl_android.utils.YoutubeDLUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class YoutubeDL {

    private static final YoutubeDL INSTANCE = new YoutubeDL();
    protected static final String baseName = "youtubedl-android";
    private static final String packagesRoot = "packages";
    private static final String pythonBin = "libpython.bin.so";
    private static final String pythonLib = "libpython.zip.so";
    private static final String pythonDirName = "python";
    private static final String ffmpegDirName = "ffmpeg";
    protected static final String youtubeDLDirName = "youtube-dl";
    private static final String youtubeDLBin = "__main__.py";
    protected static final String youtubeDLFile = "youtube_dl.zip";

    private boolean initialized = false;
    private File pythonPath;
    private File youtubeDLPath;
    private File binDir;

    private String ENV_LD_LIBRARY_PATH;
    private String ENV_SSL_CERT_FILE;
    private String ENV_PYTHONHOME;

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    private YoutubeDL(){
    }

    public static YoutubeDL getInstance() {
        return INSTANCE;
    }

    synchronized public void init(Application application) throws YoutubeDLException {
        if (initialized) return;

        File baseDir = new File(application.getNoBackupFilesDir(), baseName);
        if(!baseDir.exists()) baseDir.mkdir();

        File packagesDir = new File(baseDir, packagesRoot);
        binDir = new File(application.getApplicationInfo().nativeLibraryDir);
        pythonPath = new File(binDir, pythonBin);
        File pythonDir = new File(packagesDir, pythonDirName);
        File ffmpegDir = new File(packagesDir, ffmpegDirName);

        File youtubeDLDir = new File(baseDir, youtubeDLDirName);
        youtubeDLPath = new File(youtubeDLDir, youtubeDLBin);

        ENV_LD_LIBRARY_PATH = pythonDir.getAbsolutePath() + "/usr/lib" + ":" + ffmpegDir.getAbsolutePath() + "/usr/lib";
        ENV_SSL_CERT_FILE = pythonDir.getAbsolutePath() + "/usr/etc/tls/cert.pem";
        ENV_PYTHONHOME = pythonDir.getAbsolutePath() + "/usr";

        initPython(application, pythonDir);
        initYoutubeDL(application, youtubeDLDir);

        initialized = true;
    }

    protected void initYoutubeDL(Application application, File youtubeDLDir) throws YoutubeDLException {
        if (!youtubeDLDir.exists()) {
            youtubeDLDir.mkdirs();
            try {
                YoutubeDLUtils.unzip(application.getResources().openRawResource(R.raw.youtube_dl), youtubeDLDir);
            } catch (Exception e) {
                FileUtils.deleteQuietly(youtubeDLDir);
                throw new YoutubeDLException("failed to initialize", e);
            }
        }
    }

    protected void initPython(Application application, File pythonDir) throws YoutubeDLException {
        if (!pythonDir.exists()) {
            pythonDir.mkdirs();
            try {
                YoutubeDLUtils.unzip(new File(binDir, pythonLib), pythonDir);
            } catch (Exception e) {
                FileUtils.deleteQuietly(pythonDir);
                throw new YoutubeDLException("failed to initialize", e);
            }
        }
    }

    private void assertInit() {
        if (!initialized) throw new IllegalStateException("instance not initialized");
    }

    public VideoInfo getInfo(String url) throws YoutubeDLException, InterruptedException {
        YoutubeDLRequest request = new YoutubeDLRequest(url);
        return getInfo(request);
   }

    public VideoInfo getInfo(YoutubeDLRequest request) throws YoutubeDLException, InterruptedException {
        request.addOption("--dump-json");
        YoutubeDLResponse response = execute(request, null);

        VideoInfo videoInfo;
        try {
            videoInfo = objectMapper.readValue(response.getOut(), VideoInfo.class);
        } catch (IOException e) {
            throw new YoutubeDLException("Unable to parse video information", e);
        }

        return videoInfo;
    }

    public YoutubeDLResponse execute(YoutubeDLRequest request) throws YoutubeDLException, InterruptedException {
        return execute(request, null);
    }

    public YoutubeDLResponse execute(YoutubeDLRequest request, @Nullable DownloadProgressCallback callback) throws YoutubeDLException, InterruptedException {
        assertInit();

        // disable caching unless explicitly requested
        if(request.getOption("--cache-dir") == null){
            request.addOption("--no-cache-dir");
        }

        YoutubeDLResponse youtubeDLResponse;
        Process process;
        int exitCode;
        StringBuffer outBuffer = new StringBuffer(); //stdout
        StringBuffer errBuffer = new StringBuffer(); //stderr
        long startTime = System.currentTimeMillis();

        List<String> args = request.buildCommand();
        List<String> command = new ArrayList<>();
        command.addAll(Arrays.asList(pythonPath.getAbsolutePath(), youtubeDLPath.getAbsolutePath()));
        command.addAll(args);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Map<String, String> env = processBuilder.environment();
        env.put("LD_LIBRARY_PATH", ENV_LD_LIBRARY_PATH);
        env.put("SSL_CERT_FILE", ENV_SSL_CERT_FILE);
        env.put("PATH",  System.getenv("PATH") + ":" + binDir.getAbsolutePath());
        env.put("PYTHONHOME", ENV_PYTHONHOME);

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
            process.destroy();
            throw e;
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

    synchronized public YoutubeDLUpdater.UpdateStatus updateYoutubeDL(Application application) throws YoutubeDLException {
        assertInit();
        try {
            return YoutubeDLUpdater.update(application);
        } catch (IOException e) {
            throw new YoutubeDLException("failed to update youtube-dl", e);
        }
    }
}
