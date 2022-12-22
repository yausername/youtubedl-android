package com.yausername.youtubedl_android

import android.content.Context
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.yausername.youtubedl_android.YoutubeDL.UpdateStatus
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_common.SharedPrefsHelper
import com.yausername.youtubedl_common.SharedPrefsHelper.update
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.net.URL

internal object YoutubeDLUpdater {
    private const val releasesUrl = "https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest"
    private const val youtubeDLVersionKey = "youtubeDLVersion"
    @Throws(IOException::class, YoutubeDLException::class)
    fun update(appContext: Context): UpdateStatus {
        val json = checkForUpdate(appContext) ?: return UpdateStatus.ALREADY_UP_TO_DATE
        val downloadUrl = getDownloadUrl(json)
        val file = download(appContext, downloadUrl)
        val ytdlpDir = getYoutubeDLDir(appContext)
        val binary = File(ytdlpDir, "yt-dlp")
        try {
            /* purge older version */
            if (ytdlpDir.exists()) FileUtils.deleteDirectory(ytdlpDir)
            /* install newer version */ytdlpDir.mkdirs()
            FileUtils.copyFile(file, binary)
        } catch (e: Exception) {
            /* if something went wrong restore default version */
            FileUtils.deleteQuietly(ytdlpDir)
            YoutubeDL.init_ytdlp(appContext, ytdlpDir)
            throw YoutubeDLException(e)
        } finally {
            file.delete()
        }
        updateSharedPrefs(appContext, getTag(json))
        return UpdateStatus.DONE
    }

    private fun updateSharedPrefs(appContext: Context, tag: String) {
        update(appContext, youtubeDLVersionKey, tag)
    }

    @Throws(IOException::class)
    private fun checkForUpdate(appContext: Context): JsonNode? {
        val url = URL(releasesUrl)
        val json: JsonNode = YoutubeDL.objectMapper.readTree(url)
        val newVersion = getTag(json)
        val oldVersion = SharedPrefsHelper[appContext, youtubeDLVersionKey]
        return if (newVersion == oldVersion) {
            null
        } else json
    }

    private fun getTag(json: JsonNode): String {
        return json["tag_name"].asText()
    }

    @Throws(YoutubeDLException::class)
    private fun getDownloadUrl(json: JsonNode): String {
        val assets = json["assets"] as ArrayNode
        var downloadUrl = ""
        for (asset in assets) {
            if (YoutubeDL.ytdlpBin == asset["name"].asText()) {
                downloadUrl = asset["browser_download_url"].asText()
                break
            }
        }
        if (downloadUrl.isEmpty()) throw YoutubeDLException("unable to get download url")
        return downloadUrl
    }

    @Throws(IOException::class)
    private fun download(appContext: Context, url: String): File {
        val downloadUrl = URL(url)
        val file = File.createTempFile("yt-dlp", null, appContext.cacheDir)
        FileUtils.copyURLToFile(downloadUrl, file, 5000, 10000)
        return file
    }

    private fun getYoutubeDLDir(appContext: Context): File {
        val baseDir = File(appContext.noBackupFilesDir, YoutubeDL.baseName)
        return File(baseDir, YoutubeDL.ytdlpDirName)
    }

    fun version(appContext: Context?): String? {
        return SharedPrefsHelper[appContext!!, youtubeDLVersionKey]
    }
}