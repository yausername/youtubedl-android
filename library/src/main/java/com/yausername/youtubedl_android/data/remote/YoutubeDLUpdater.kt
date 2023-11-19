package com.yausername.youtubedl_android.data.remote

import android.content.Context
import com.yausername.youtubedl_android.Constants.Binaries.YTDLP_BINARY_NAME
import com.yausername.youtubedl_android.Constants.Directories.YTDLP_DIRECTORY_NAME
import com.yausername.youtubedl_android.Constants.LIBRARY_NAME
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDL.UpdateChannel
import com.yausername.youtubedl_android.YoutubeDL.UpdateStatus
import com.yausername.youtubedl_android.domain.model.updates.Release
import com.yausername.youtubedl_android.util.exceptions.YoutubeDLException
import com.yausername.youtubedl_common.SharedPrefsHelper
import com.yausername.youtubedl_common.SharedPrefsHelper.update
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.net.URL

internal object YoutubeDLUpdater {

    private const val dlpBinaryName = "yt-dlp"
    private const val dlpVersionKey = "dlpVersion"
    private const val dlpVersionNameKey = "dlpVersionName"

    @Throws(IOException::class, YoutubeDLException::class)
    internal fun update(
        appContext: Context?,
        youtubeDLChannel: UpdateChannel = UpdateChannel.STABLE
    ): UpdateStatus {
        val json = checkForUpdate(appContext!!, youtubeDLChannel)
            ?: return UpdateStatus.ALREADY_UP_TO_DATE
        val downloadUrl = getDownloadUrl(json)
        val file = download(appContext, downloadUrl)
        val ytdlpDir = getYoutubeDLDir(
            appContext
        )
        val binary = File(ytdlpDir, dlpBinaryName)
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
        updateSharedPrefs(appContext, json.tag_name, json.name)
        return UpdateStatus.DONE
    }

    private fun updateSharedPrefs(appContext: Context, tag: String, name: String) {
        update(appContext, dlpVersionKey, tag)
        update(appContext, dlpVersionNameKey, name)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Throws(IOException::class)
    private fun checkForUpdate(appContext: Context, youtubeDLChannel: UpdateChannel): Release? {
        val url = URL(youtubeDLChannel.apiUrl)
        val json = YoutubeDL.json.decodeFromStream<Release>(url.openStream())
        val newVersion = json.tag_name
        val oldVersion = SharedPrefsHelper[appContext, dlpVersionKey]
        return if (newVersion == oldVersion) {
            null
        } else json
    }

    @Throws(YoutubeDLException::class)
    private fun getDownloadUrl(json: Release): String {
        val assets = json.assets
        var downloadUrl = ""
        for (asset in assets) {
            if (YTDLP_BINARY_NAME == asset.name) {
                downloadUrl = asset.browser_download_url
                break
            }
        }
        if (downloadUrl.isEmpty()) throw YoutubeDLException("Unable to get download url")
        return downloadUrl
    }

    @Throws(IOException::class)
    private fun download(appContext: Context, url: String): File {
        val downloadUrl = URL(url)
        val file = File.createTempFile(dlpBinaryName, null, appContext.cacheDir)
        FileUtils.copyURLToFile(downloadUrl, file, 5000, 10000)
        return file
    }

    private fun getYoutubeDLDir(appContext: Context): File {
        val baseDir = File(appContext.noBackupFilesDir, LIBRARY_NAME)
        return File(baseDir, YTDLP_DIRECTORY_NAME)
    }

    fun version(appContext: Context?): String? {
        return SharedPrefsHelper[appContext!!, dlpVersionKey]
    }

    fun versionName(appContext: Context?): String? {
        return SharedPrefsHelper[appContext!!, dlpVersionNameKey]
    }
}