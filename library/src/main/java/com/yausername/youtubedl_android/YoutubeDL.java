package com.yausername.youtubedl_android;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yausername.youtubedl_android.mapper.VideoInfo;
import com.yausername.youtubedl_common.SharedPrefsHelper;
import com.yausername.youtubedl_common.utils.ZipUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YoutubeDL {

    private static final YoutubeDL INSTANCE = new YoutubeDL();
    protected static final String baseName = "youtubedl-android";
    private static final String packagesRoot = "packages";
    private static final String pythonBinName = "libpython.so";
    private static final String pythonLibName = "libpython.zip.so";
    private static final String pythonDirName = "python";
    private static final String ffmpegDirName = "ffmpeg";
    private static final String ffmpegBinName = "libffmpeg.so";
    private static final String aria2cDirName = "aria2c";
    protected static final String ytdlpDirName = "yt-dlp";
    protected static final String ytdlpBin = "yt-dlp";
    private static final String pythonLibVersion = "pythonLibVersion";

    private boolean initialized = false;
    private File pythonPath;
    private File ffmpegPath;
    private File ytdlpPath;
    private File binDir;

    private String ENV_LD_LIBRARY_PATH;
    private String ENV_SSL_CERT_FILE;
    private String ENV_PYTHONHOME;

    private final Map<String, Process> id2Process = Collections.synchronizedMap(new HashMap<String, Process>());

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    private YoutubeDL() {
    }

    public static YoutubeDL getInstance() {
        return INSTANCE;
    }

    synchronized public void init(Context appContext) throws YoutubeDLException {
        if (initialized) return;

        File baseDir = new File(appContext.getNoBackupFilesDir(), baseName);
        if (!baseDir.exists()) baseDir.mkdir();

        File packagesDir = new File(baseDir, packagesRoot);
        binDir = new File(appContext.getApplicationInfo().nativeLibraryDir);
        pythonPath = new File(binDir, pythonBinName);
        ffmpegPath = new File(binDir, ffmpegBinName);
        File pythonDir = new File(packagesDir, pythonDirName);
        File ffmpegDir = new File(packagesDir, ffmpegDirName);
        File aria2cDir = new File(packagesDir, aria2cDirName);

        File ytdlpDir = new File(baseDir, ytdlpDirName);
        ytdlpPath = new File(ytdlpDir, ytdlpBin);

        ENV_LD_LIBRARY_PATH = pythonDir.getAbsolutePath() + "/usr/lib" + ":" +
                ffmpegDir.getAbsolutePath() + "/usr/lib" + ":" +
        aria2cDir.getAbsolutePath() + "/usr/lib";
        ENV_SSL_CERT_FILE = pythonDir.getAbsolutePath() + "/usr/etc/tls/cert.pem";
        ENV_PYTHONHOME = pythonDir.getAbsolutePath() + "/usr";

        initPython(appContext, pythonDir);
        init_ytdlp(appContext, ytdlpDir);

        initialized = true;
    }

    protected void init_ytdlp(@NonNull final Context appContext, @NonNull final File ytdlpDir) throws YoutubeDLException {
        if (!ytdlpDir.exists())
            ytdlpDir.mkdirs();

        final File ytdlpBinary = new File(ytdlpDir, ytdlpBin);
        if (!ytdlpBinary.exists()) {
            try {
                final InputStream inputStream = appContext.getResources().openRawResource(R.raw.ytdlp); /* will be renamed to yt-dlp */
                FileUtils.copyInputStreamToFile(inputStream, ytdlpBinary);
            } catch (final Exception e) {
                FileUtils.deleteQuietly(ytdlpDir);
                throw new YoutubeDLException("failed to initialize", e);
            }
        }
    }

    protected void initPython(Context appContext, File pythonDir) throws YoutubeDLException {
        File pythonLib = new File(binDir, pythonLibName);
        // using size of lib as version
        String pythonSize = String.valueOf(pythonLib.length());
        if (!pythonDir.exists() || shouldUpdatePython(appContext, pythonSize)) {
            FileUtils.deleteQuietly(pythonDir);
            pythonDir.mkdirs();
            try {
                ZipUtils.unzip(pythonLib, pythonDir);
            } catch (Exception e) {
                FileUtils.deleteQuietly(pythonDir);
                throw new YoutubeDLException("failed to initialize", e);
            }
            updatePython(appContext, pythonSize);
        }
    }

    private boolean shouldUpdatePython(@NonNull Context appContext, @NonNull String version) {
        return !version.equals(SharedPrefsHelper.get(appContext, pythonLibVersion));
    }

    private void updatePython(@NonNull Context appContext, @NonNull String version) {
        SharedPrefsHelper.update(appContext, pythonLibVersion, version);
    }

    private void assertInit() {
        if (!initialized) throw new IllegalStateException("instance not initialized");
    }

    public VideoInfo getInfo(String url) throws YoutubeDLException, InterruptedException {
        YoutubeDLRequest request = new YoutubeDLRequest(url);
        return getInfo(request);
    }

    @NonNull
    public VideoInfo getInfo(YoutubeDLRequest request) throws YoutubeDLException, InterruptedException {
        request.addOption("--dump-json");
        YoutubeDLResponse response = execute(request, null, null);

        VideoInfo videoInfo;
        try {
            videoInfo = objectMapper.readValue(response.getOut(), VideoInfo.class);
        } catch (IOException e) {
            throw new YoutubeDLException("Unable to parse video information", e);
        }

        if (videoInfo == null) {
            throw new YoutubeDLException("Failed to fetch video information");
        }

        return videoInfo;
    }

    public YoutubeDLResponse execute(YoutubeDLRequest request) throws YoutubeDLException, InterruptedException {
        return execute(request, null, null);
    }

    private boolean ignoreErrors(YoutubeDLRequest request, String out) {
        return request.hasOption("--dump-json") && !out.isEmpty() && request.hasOption("--ignore-errors");
    }

    public YoutubeDLResponse execute(YoutubeDLRequest request, @Nullable DownloadProgressCallback callback) throws YoutubeDLException, InterruptedException {
        return execute(request, null, callback);
    }

    public boolean destroyProcessById(@NonNull final String id) {
        if (id2Process.containsKey(id)) {
            final Process p = id2Process.get(id);
            boolean alive = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!p.isAlive()) {
                    alive = false;
                }
            }
            if (alive) {
                try {
                    p.destroy();
                    return true;
                } catch (Exception ignored) {
                }
            }
        }
        return false;
    }

    public YoutubeDLResponse execute(YoutubeDLRequest request, @Nullable String processId, @Nullable DownloadProgressCallback callback) throws YoutubeDLException, InterruptedException {
        assertInit();
        if (processId != null && id2Process.containsKey(processId))
            throw new YoutubeDLException("Process ID already exists");
        // disable caching unless explicitly requested
        if (!request.hasOption("--cache-dir") || request.getOption("--cache-dir") == null) {
            request.addOption("--no-cache-dir");
        }

        /* Set ffmpeg location, See https://github.com/xibr/ytdlp-lazy/issues/1 */
        request.addOption("--ffmpeg-location", ffmpegPath.getAbsolutePath());

        YoutubeDLResponse youtubeDLResponse;
        Process process;
        int exitCode;
        StringBuffer outBuffer = new StringBuffer(); //stdout
        StringBuffer errBuffer = new StringBuffer(); //stderr
        long startTime = System.currentTimeMillis();

        List<String> args = request.buildCommand();
        List<String> command = new ArrayList<>();
        command.addAll(Arrays.asList(pythonPath.getAbsolutePath(), ytdlpPath.getAbsolutePath()));
        command.addAll(args);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Map<String, String> env = processBuilder.environment();
        env.put("LD_LIBRARY_PATH", ENV_LD_LIBRARY_PATH);
        env.put("SSL_CERT_FILE", ENV_SSL_CERT_FILE);
        env.put("PATH", System.getenv("PATH") + ":" + binDir.getAbsolutePath());
        env.put("PYTHONHOME", ENV_PYTHONHOME);

        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new YoutubeDLException(e);
        }
        if (processId != null) {
            id2Process.put(processId, process);
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
            try {
                process.destroy();
            } catch (Exception ignored) {

            }
            if (processId != null)
                id2Process.remove(processId);
            throw e;
        }
        if (processId != null)
            id2Process.remove(processId);

        String out = outBuffer.toString();
        String err = errBuffer.toString();

        if (exitCode > 0 && !ignoreErrors(request, out)) {
            throw new YoutubeDLException(err);
        }

        long elapsedTime = System.currentTimeMillis() - startTime;

        youtubeDLResponse = new YoutubeDLResponse(command, exitCode, elapsedTime, out, err);

        return youtubeDLResponse;
    }

    synchronized public UpdateStatus updateYoutubeDL(Context appContext, UpdateChannel updateChannel) throws YoutubeDLException {
        assertInit();
        try {
            return YoutubeDLUpdater.update(appContext, updateChannel);
        } catch (IOException e) {
            throw new YoutubeDLException("failed to update youtube-dl", e);
        }
    }

    @Nullable
    public String version(Context appContext) {
        return YoutubeDLUpdater.version(appContext);
    }

    @Nullable
    public String versionName(Context appContext) {
        return YoutubeDLUpdater.versionName(appContext);
    }

    public enum UpdateStatus {
        DONE, ALREADY_UP_TO_DATE
    }

    public enum UpdateChannel {
        STABLE, NIGHTLY
    }
}
