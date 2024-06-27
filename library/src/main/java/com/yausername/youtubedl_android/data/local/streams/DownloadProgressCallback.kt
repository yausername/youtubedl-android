package com.yausername.youtubedl_android.data.local.streams

interface DownloadProgressCallback {
    fun onProgressUpdate(progress: Float, etaInSeconds: Long, line: String?)
}