package com.yausername.youtubedl_common.data.remote.dependencies

import android.content.Context
import com.yausername.youtubedl_common.Constants.LibrariesName.TemporalFilesName.TEMPORAL_ARIA2C
import com.yausername.youtubedl_common.Constants.LibrariesName.TemporalFilesName.TEMPORAL_FFMPEG
import com.yausername.youtubedl_common.Constants.LibrariesName.TemporalFilesName.TEMPORAL_PYTHON
import com.yausername.youtubedl_common.data.remote.FileDownloader
import com.yausername.youtubedl_common.domain.DependenciesDownloader
import com.yausername.youtubedl_common.domain.Dependency
import com.yausername.youtubedl_common.utils.dependencies.DependenciesUtil.getDownloadLinkForDependency
import com.yausername.youtubedl_common.utils.dependencies.DependenciesUtil.unzipToDependencyDirectory
import com.yausername.youtubedl_common.utils.device.CpuUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File

class DependenciesDownloaderImpl : DependenciesDownloader {
    private val fileDownloader by lazy { FileDownloader }
    override suspend fun downloadPython(
        context: Context,
        progressCallback: (progress: Int) -> Unit
    ) {
        val tempFile = withContext(Dispatchers.IO) {
            File.createTempFile(TEMPORAL_PYTHON, null)
        }

        val urlDeferred = withContext(Dispatchers.IO) {
            async { getDownloadLinkForDependency(CpuUtils.getPreferredAbi(), Dependency.PYTHON) }
        }

        val downloadUrl = urlDeferred.await()

        fileDownloader.downloadFileWithProgress(
            fileUrl = downloadUrl,
            localFile = tempFile,
            progressCallback = progressCallback,
            overwrite = true
        )

        unzipToDependencyDirectory(context, tempFile, Dependency.PYTHON)
    }

    override suspend fun downloadFFmpeg(
        context: Context,
        progressCallback: (progress: Int) -> Unit
    ) {
        val tempFile = withContext(Dispatchers.IO) {
            File.createTempFile(TEMPORAL_FFMPEG, null)
        }

        val urlDeferred = withContext(Dispatchers.IO) {
            async { getDownloadLinkForDependency(CpuUtils.getPreferredAbi(), Dependency.FFMPEG) }
        }

        val downloadUrl = urlDeferred.await()

        fileDownloader.downloadFileWithProgress(
            fileUrl = downloadUrl,
            localFile = tempFile,
            progressCallback = progressCallback,
            overwrite = true
        )

        unzipToDependencyDirectory(context, tempFile, Dependency.FFMPEG)
    }

    override suspend fun downloadAria2c(
        context: Context,
        progressCallback: (progress: Int) -> Unit
    ) {
        val tempFile = withContext(Dispatchers.IO) {
            File.createTempFile(TEMPORAL_ARIA2C, null)
        }
        val urlDeferred = withContext(Dispatchers.IO) {
            async { getDownloadLinkForDependency(CpuUtils.getPreferredAbi(), Dependency.ARIA2C) }
        }

        val downloadUrl = urlDeferred.await()

        fileDownloader.downloadFileWithProgress(
            fileUrl = downloadUrl,
            localFile = tempFile,
            progressCallback = progressCallback,
            overwrite = true
        )

        unzipToDependencyDirectory(context, tempFile, Dependency.ARIA2C)
    }
}