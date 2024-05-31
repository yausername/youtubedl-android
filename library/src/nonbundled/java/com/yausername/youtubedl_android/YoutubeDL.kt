package com.yausername.youtubedl_android

import android.content.Context
import android.util.Log
import com.yausername.youtubedl_android.util.exceptions.YoutubeDLException
import com.yausername.youtubedl_common.Constants
import com.yausername.youtubedl_common.domain.Dependency
import com.yausername.youtubedl_common.domain.model.DownloadedDependencies
import com.yausername.youtubedl_common.utils.ZipUtils.unzip
import com.yausername.youtubedl_common.utils.dependencies.DependenciesUtil
import com.yausername.youtubedl_common.utils.dependencies.dependencyDownloadCallback
import org.apache.commons.io.FileUtils
import java.io.File

object YoutubeDL: YoutubeDLCore() {
    /**
     * Initializes Python.
     * @param appContext the application context
     * @param pythonDir the directory where Python is located
     */
    @Throws(YoutubeDLException::class)
    override fun initPython(appContext: Context, pythonDir: File) {
        val pythonLibrary = File(
            binariesDirectory, Constants.LibrariesName.PYTHON
        )
        // using size of lib as version
        val pythonSize = pythonLibrary.length().toString()
        if (!pythonDir.exists() || shouldUpdatePython(appContext, pythonSize)) {
            //TODO: REMAKE THE UPDATE SYSTEM FOR THE ONLINE DOWNLOADING
            Log.i("YoutubeDL", "Update Python - TEST")
            updatePython(appContext, pythonSize)
        }
    }

    @JvmName("ensureDependenciesBridge")
    @Throws(IllegalStateException::class)
    fun ensureDependencies(
        appContext: Context,
        skipDependencies: List<Dependency> = emptyList(),
        callback: dependencyDownloadCallback? = null
    ): DownloadedDependencies =
        DependenciesUtil.ensureDependencies(appContext, skipDependencies, callback)

    @JvmStatic
    fun getInstance() = this
}