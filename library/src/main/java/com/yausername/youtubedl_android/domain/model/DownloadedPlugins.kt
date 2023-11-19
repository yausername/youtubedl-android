package com.yausername.youtubedl_android.domain.model

data class DownloadedPlugins(
    val python: Boolean = false,
    val ffmpeg: Boolean = false,
    val aria2c: Boolean = false
)
