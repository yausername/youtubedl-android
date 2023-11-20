package com.yausername.youtubedl_android.data.remote.plugins

import android.content.Context
import com.yausername.youtubedl_android.Constants.Libraries.Temporal.TEMPORAL_ARIA2C_LIBRARY_NAME
import com.yausername.youtubedl_android.Constants.Libraries.Temporal.TEMPORAL_FFMPEG_LIBRARY_NAME
import com.yausername.youtubedl_android.Constants.Libraries.Temporal.TEMPORAL_PYTHON_LIBRARY_NAME
import com.yausername.youtubedl_android.data.remote.files.FileDownloader
import com.yausername.youtubedl_android.domain.Plugin
import com.yausername.youtubedl_android.domain.PluginsDownloader
import com.yausername.youtubedl_android.util.device.CpuUtils
import com.yausername.youtubedl_android.util.plugins.PluginsUtil.getDownloadLinkForPlugin
import com.yausername.youtubedl_android.util.plugins.PluginsUtil.unzipToPluginDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File

class PluginsDownloaderImpl : PluginsDownloader {
    private val fileDownloader by lazy { FileDownloader }
    override suspend fun downloadPython(
        context: Context,
        progressCallback: (progress: Int) -> Unit
    ) {
        val tempFile = withContext(Dispatchers.IO) {
            File.createTempFile(TEMPORAL_PYTHON_LIBRARY_NAME, null)
        }

        val urlDeferred = withContext(Dispatchers.IO) {
            async { getDownloadLinkForPlugin(CpuUtils.getPreferredAbi(), Plugin.PYTHON) }
        }

        val downloadUrl = urlDeferred.await()

        fileDownloader.downloadFileWithProgress(
            fileUrl = downloadUrl,
            localFile = tempFile,
            progressCallback = progressCallback,
            overwrite = true
        )

        unzipToPluginDirectory(context, tempFile, Plugin.PYTHON)
    }

    override suspend fun downloadFFmpeg(
        context: Context,
        progressCallback: (progress: Int) -> Unit
    ) {
        val tempFile = withContext(Dispatchers.IO) {
            File.createTempFile(TEMPORAL_FFMPEG_LIBRARY_NAME, null)
        }

        val urlDeferred = withContext(Dispatchers.IO) {
            async { getDownloadLinkForPlugin(CpuUtils.getPreferredAbi(), Plugin.FFMPEG) }
        }

        val downloadUrl = urlDeferred.await()

        fileDownloader.downloadFileWithProgress(
            fileUrl = downloadUrl,
            localFile = tempFile,
            progressCallback = progressCallback,
            overwrite = true
        )

        unzipToPluginDirectory(context, tempFile, Plugin.FFMPEG)
    }

    override suspend fun downloadAria2c(
        context: Context,
        progressCallback: (progress: Int) -> Unit
    ) {
        val tempFile = withContext(Dispatchers.IO) {
            File.createTempFile(TEMPORAL_ARIA2C_LIBRARY_NAME, null)
        }
        val urlDeferred = withContext(Dispatchers.IO) {
            async { getDownloadLinkForPlugin(CpuUtils.getPreferredAbi(), Plugin.ARIA2C) }
        }

        val downloadUrl = urlDeferred.await()

        fileDownloader.downloadFileWithProgress(
            fileUrl = downloadUrl,
            localFile = tempFile,
            progressCallback = progressCallback,
            overwrite = true
        )

        unzipToPluginDirectory(context, tempFile, Plugin.ARIA2C)
    }
}