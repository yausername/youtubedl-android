package com.yausername.youtubedl_android.domain.model

import com.yausername.youtubedl_android.domain.Plugin

data class DownloadedPlugins(
    val python: Boolean = false,
    val ffmpeg: Boolean = false,
    val aria2c: Boolean = false
)
fun DownloadedPlugins.getMissingPlugins(): List<Plugin> {
    val missingPlugins = mutableListOf<Plugin>()
    if (!python) missingPlugins.add(Plugin.PYTHON)
    if (!ffmpeg) missingPlugins.add(Plugin.FFMPEG)
    if (!aria2c) missingPlugins.add(Plugin.ARIA2C)
    return missingPlugins
}
