package com.yausername.aria2c

import android.content.Context
import com.yausername.youtubedl_android.util.exceptions.YoutubeDLException
import com.yausername.youtubedl_common.Constants
import com.yausername.youtubedl_common.Constants.LIBRARY_NAME
import com.yausername.youtubedl_common.Constants.PACKAGES_ROOT_NAME
import com.yausername.youtubedl_common.SharedPrefsHelper
import com.yausername.youtubedl_common.utils.ZipUtils
import org.apache.commons.io.FileUtils
import java.io.File

abstract class Aria2cCore {
    private var initialized = false
    var binDir: File? = null

    @Synchronized
    fun init(appContext: Context) {
        if (initialized) return
        val baseDir = File(appContext.noBackupFilesDir, LIBRARY_NAME)
        if (!baseDir.exists()) baseDir.mkdir()
        binDir = File(appContext.applicationInfo.nativeLibraryDir)
        val packagesDir = File(baseDir, PACKAGES_ROOT_NAME)
        val aria2cDir = File(packagesDir, Constants.DirectoriesName.ARIA2C)
        initAria2c(appContext, aria2cDir)
        initialized = true
    }

    abstract fun initAria2c(appContext: Context, aria2cDir: File)

    internal fun shouldUpdateAria2c(appContext: Context, version: String): Boolean {
        return version != SharedPrefsHelper[appContext, aria2cLibVersion]
    }

    internal fun updateAria2c(appContext: Context, version: String) {
        SharedPrefsHelper.update(appContext, aria2cLibVersion, version)
    }

    private val aria2cLibVersion = "aria2cLibVersion"

}