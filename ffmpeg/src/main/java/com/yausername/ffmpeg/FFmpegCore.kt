package com.yausername.ffmpeg

import android.content.Context
import com.yausername.youtubedl_common.Constants
import com.yausername.youtubedl_common.Constants.LIBRARY_NAME
import com.yausername.youtubedl_common.Constants.PACKAGES_ROOT_NAME
import com.yausername.youtubedl_common.SharedPrefsHelper
import com.yausername.youtubedl_common.SharedPrefsHelper.update
import java.io.File

abstract class FFmpegCore {
    private var initialized = false
    var binDir: File? = null

    @Synchronized
    fun init(appContext: Context) {
        if (initialized) return
        val baseDir = File(appContext.noBackupFilesDir, LIBRARY_NAME)
        if (!baseDir.exists()) baseDir.mkdir()
        binDir = File(appContext.applicationInfo.nativeLibraryDir)
        val packagesDir = File(baseDir, PACKAGES_ROOT_NAME)
        val ffmpegDir = File(packagesDir, Constants.DirectoriesName.FFMPEG)
        initFFmpeg(appContext, ffmpegDir)
        initialized = true
    }

    internal abstract fun initFFmpeg(appContext: Context, ffmpegDir: File)

    internal fun shouldUpdateFFmpeg(appContext: Context, version: String): Boolean {
        return version != SharedPrefsHelper[appContext, ffmpegLibVersion]
    }

    internal fun updateFFmpeg(appContext: Context, version: String) {
        update(appContext, ffmpegLibVersion, version)
    }

    companion object {
        const val ffmpegLibVersion = "ffmpegLibVersion"
    }
}