package com.yausername.youtubedl_android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.yausername.youtubedl_android.utils.YoutubeDLUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class YoutubeDLUpdater {

    private static final String releasesUrl = "https://api.github.com/repos/yausername/youtubedl-lazy/releases/latest";
    private static final String sharedPrefsName = "youtubedl-android";
    private static final String youtubeDLVersionKey = "youtubeDLVersion";

    public enum UpdateStatus {
        DONE, ALREADY_UP_TO_DATE;
    }

    protected static UpdateStatus update(Application application) throws IOException, YoutubeDLException {
        JsonNode json = checkForUpdate(application);
        if(null == json) return UpdateStatus.ALREADY_UP_TO_DATE;

        String downloadUrl = getDownloadUrl(json);
        File file = download(application, downloadUrl);

        File youtubeDLDir = null;
        try {
            youtubeDLDir = getYoutubeDLDir(application);
            //purge older version
            YoutubeDLUtils.delete(youtubeDLDir);
            //install newer version
            youtubeDLDir.mkdirs();
            YoutubeDLUtils.unzip(file, youtubeDLDir);
        } catch (Exception e) {
            //if something went wrong restore default version
            YoutubeDLUtils.delete(youtubeDLDir);
            YoutubeDL.getInstance().initYoutubeDL(application, youtubeDLDir);
            throw e;
        } finally {
            file.delete();
        }

        updateSharedPrefs(application, getTag(json));
        return UpdateStatus.DONE;
    }

    private static void updateSharedPrefs(Application application, String tag) {
        SharedPreferences pref = application.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(youtubeDLVersionKey, tag);
        editor.apply();
    }

    private static JsonNode checkForUpdate(Application application) throws IOException {
        URL url = new URL(releasesUrl);
        JsonNode json = YoutubeDL.objectMapper.readTree(url);
        String newVersion = getTag(json);
        SharedPreferences pref = application.getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE);
        String oldVersion = pref.getString(youtubeDLVersionKey, null);
        if(newVersion.equals(oldVersion)){
            return null;
        }
        return json;
    }

    private static String getTag(JsonNode json){
        return json.get("tag_name").asText();
    }

    @NonNull
    private static String getDownloadUrl(@NonNull JsonNode json) throws IOException, YoutubeDLException {
        ArrayNode assets = (ArrayNode) json.get("assets");
        String downloadUrl = "";
        for (JsonNode asset : assets) {
            if (YoutubeDL.youtubeDLFile.equals(asset.get("name").asText())) {
                downloadUrl = asset.get("browser_download_url").asText();
                break;
            }
        }
        if (downloadUrl.isEmpty()) throw new YoutubeDLException("unable to get download url");
        return downloadUrl;
    }

    @NonNull
    private static File download(Application application, String url) throws YoutubeDLException, IOException {
        File file = null;
        InputStream in = null;
        FileOutputStream out = null;
        ReadableByteChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            URL downloadUrl = new URL(url);
            in = downloadUrl.openStream();
            inChannel = Channels.newChannel(in);
            file = File.createTempFile("youtube_dl", "zip", application.getCacheDir());
            out = new FileOutputStream(file);
            outChannel = out.getChannel();
            long bytesRead=0;
            long transferPosition=0;
            while ((bytesRead=outChannel.transferFrom(inChannel,transferPosition,1 << 24)) > 0) {
                transferPosition+=bytesRead;
            }
        } catch (Exception e) {
            // delete temp file if something went wrong
            if (null != file && file.exists()) {
                file.delete();
            }
            throw e;
        } finally {
            if (null != in) in.close();
            if (null != inChannel) inChannel.close();
            if (null != out) out.close();
            if (null != outChannel) outChannel.close();
        }
        return file;
    }

    @NonNull
    private static File getYoutubeDLDir(Application application) {
        File baseDir = new File(application.getNoBackupFilesDir(), YoutubeDL.baseName);
        return new File(baseDir, YoutubeDL.youtubeDLDirName);
    }

}
