package com.yausername.youtubedl_android;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.yausername.youtubedl_android.YoutubeDL.UpdateStatus;
import com.yausername.youtubedl_common.SharedPrefsHelper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

class YoutubeDLUpdater {

    private YoutubeDLUpdater() {
    }

    private static final String releasesUrl = "https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest";
    private static final String youtubeDLVersionKey = "youtubeDLVersion";

    static UpdateStatus update(Context appContext) throws IOException, YoutubeDLException {
        JsonNode json = checkForUpdate(appContext);
        if (null == json) return UpdateStatus.ALREADY_UP_TO_DATE;

        String downloadUrl = getDownloadUrl(json);
        File file = download(appContext, downloadUrl);
        File ytdlpDir = getYoutubeDLDir(appContext);
        File binary = new File(ytdlpDir, "yt-dlp");

        try {
            /* purge older version */
            if (ytdlpDir.exists())
                FileUtils.deleteDirectory(ytdlpDir);
            /* install newer version */
            ytdlpDir.mkdirs();
            FileUtils.copyFile(file, binary);
        } catch (final Exception e) {
            /* if something went wrong restore default version */
            FileUtils.deleteQuietly(ytdlpDir);
            YoutubeDL.getInstance().init_ytdlp(appContext, ytdlpDir);
            throw new YoutubeDLException(e);
        } finally {
            file.delete();
        }

        updateSharedPrefs(appContext, getTag(json));
        return UpdateStatus.DONE;
    }

    private static void updateSharedPrefs(Context appContext, String tag) {
        SharedPrefsHelper.update(appContext, youtubeDLVersionKey, tag);
    }

    private static JsonNode checkForUpdate(Context appContext) throws IOException {
        URL url = new URL(releasesUrl);
        JsonNode json = YoutubeDL.objectMapper.readTree(url);
        String newVersion = getTag(json);
        String oldVersion = SharedPrefsHelper.get(appContext, youtubeDLVersionKey);
        if (newVersion.equals(oldVersion)) {
            return null;
        }
        return json;
    }

    private static String getTag(JsonNode json) {
        return json.get("tag_name").asText();
    }

    @NonNull
    private static String getDownloadUrl(@NonNull JsonNode json) throws YoutubeDLException {
        ArrayNode assets = (ArrayNode) json.get("assets");
        String downloadUrl = "";
        for (JsonNode asset : assets) {
            if (YoutubeDL.ytdlpBin.equals(asset.get("name").asText())) {
                downloadUrl = asset.get("browser_download_url").asText();
                break;
            }
        }
        if (downloadUrl.isEmpty()) throw new YoutubeDLException("unable to get download url");
        return downloadUrl;
    }

    @NonNull
    private static File download(Context appContext, String url) throws IOException {
        URL downloadUrl = new URL(url);
        File file = File.createTempFile("yt-dlp", null, appContext.getCacheDir());
        FileUtils.copyURLToFile(downloadUrl, file, 5000, 10000);
        return file;
    }

    @NonNull
    private static File getYoutubeDLDir(Context appContext) {
        File baseDir = new File(appContext.getNoBackupFilesDir(), YoutubeDL.baseName);
        return new File(baseDir, YoutubeDL.ytdlpDirName);
    }

    @Nullable
    static String version(Context appContext) {
        return SharedPrefsHelper.get(appContext, youtubeDLVersionKey);
    }

}
