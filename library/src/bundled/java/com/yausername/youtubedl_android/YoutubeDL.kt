package com.yausername.youtubedl_android

import android.content.Context
import com.yausername.youtubedl_android.util.exceptions.YoutubeDLException
import com.yausername.youtubedl_common.Constants
import com.yausername.youtubedl_common.utils.ZipUtils.unzip
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
            FileUtils.deleteQuietly(pythonDir)
            pythonDir.mkdirs()
            try {
                unzip(pythonLibrary, pythonDir)
            } catch (e: Exception) {
                FileUtils.deleteQuietly(pythonDir)
                throw YoutubeDLException("Failed to initialize Python", e)
            }
            updatePython(appContext, pythonSize)
        }
    }

    @JvmStatic
    fun getInstance() = this
}