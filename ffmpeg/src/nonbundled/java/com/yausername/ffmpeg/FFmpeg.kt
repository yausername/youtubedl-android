package com.yausername.ffmpeg

import android.content.Context
import com.yausername.youtubedl_android.util.exceptions.YoutubeDLException
import com.yausername.youtubedl_common.Constants
import com.yausername.youtubedl_common.utils.ZipUtils.unzip
import org.apache.commons.io.FileUtils
import java.io.File

object FFmpeg: FFmpegCore() {
    override fun initFFmpeg(appContext: Context, ffmpegDir: File) {
        val ffmpegLib = File(binDir, Constants.LibrariesName.FFMPEG)
        // using size of lib as version
        val ffmpegSize = ffmpegLib.length().toString()
        if (!ffmpegDir.exists() || shouldUpdateFFmpeg(appContext, ffmpegSize)) {
            //TODO: REMAKE THE UPDATE SYSTEM FOR THE ONLINE DOWNLOADING
            updateFFmpeg(appContext, ffmpegSize)
        }
    }

    @JvmStatic
    fun getInstance() = this
}