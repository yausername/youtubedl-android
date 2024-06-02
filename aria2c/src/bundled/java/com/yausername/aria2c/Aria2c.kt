package com.yausername.aria2c

import android.content.Context
import com.yausername.youtubedl_android.util.exceptions.YoutubeDLException
import com.yausername.youtubedl_common.Constants
import com.yausername.youtubedl_common.utils.ZipUtils
import org.apache.commons.io.FileUtils
import java.io.File

object Aria2c: Aria2cCore() {
    override fun initAria2c(appContext: Context, aria2cDir: File) {
        val aria2cLib = File(binDir, Constants.LibrariesName.ARIA2C)
        // using size of lib as version
        val aria2cSize = aria2cLib.length().toString()
        if (!aria2cDir.exists() || shouldUpdateAria2c(appContext, aria2cSize)) {
            FileUtils.deleteQuietly(aria2cDir)
            aria2cDir.mkdirs()
            try {
                ZipUtils.unzip(aria2cLib, aria2cDir)
            } catch (e: Exception) {
                FileUtils.deleteQuietly(aria2cDir)
                throw YoutubeDLException("failed to initialize", e)
            }
            updateAria2c(appContext, aria2cSize)
        }
    }

    @JvmStatic
    fun getInstance() = this
}