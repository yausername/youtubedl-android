package com.yausername.youtubedl_android.domain

import android.content.Context

interface PluginsDownloader {
    fun downloadPython(context: Context, progressCallback: (progress: Int) -> Unit)
    fun downloadFFmpeg(context: Context, progressCallback: (progress: Int) -> Unit)
    fun downloadAria2c(context: Context, progressCallback: (progress: Int) -> Unit)
}