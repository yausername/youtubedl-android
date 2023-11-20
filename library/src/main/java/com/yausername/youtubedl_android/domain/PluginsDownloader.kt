package com.yausername.youtubedl_android.domain

import android.content.Context

interface PluginsDownloader {
    suspend fun downloadPython(context: Context, progressCallback: (progress: Int) -> Unit)
    suspend fun downloadFFmpeg(context: Context, progressCallback: (progress: Int) -> Unit)
    suspend fun downloadAria2c(context: Context, progressCallback: (progress: Int) -> Unit)
}