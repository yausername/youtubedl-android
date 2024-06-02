package com.yausername.aria2c

import android.content.Context
import com.yausername.youtubedl_common.Constants
import java.io.File

object Aria2c: Aria2cCore() {
    override fun initAria2c(appContext: Context, aria2cDir: File) {
        val aria2cLib = File(binDir, Constants.LibrariesName.ARIA2C)
        // using size of lib as version
        val aria2cSize = aria2cLib.length().toString()
        if (!aria2cDir.exists() || shouldUpdateAria2c(appContext, aria2cSize)) {
            //TODO: REMAKE THE UPDATE SYSTEM FOR THE ONLINE DOWNLOADING
            updateAria2c(appContext, aria2cSize)
        }
    }

    @JvmStatic
    fun getInstance() = this
}