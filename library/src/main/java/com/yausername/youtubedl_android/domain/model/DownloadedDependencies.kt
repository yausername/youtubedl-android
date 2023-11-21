package com.yausername.youtubedl_android.domain.model

import com.yausername.youtubedl_android.domain.Dependency

data class DownloadedDependencies(
    val python: Boolean = false,
    val ffmpeg: Boolean = false,
    val aria2c: Boolean = false
)
fun DownloadedDependencies.getMissingDependencies(): List<Dependency> {
    val missingDependencies = mutableListOf<Dependency>()
    if (!python) missingDependencies.add(Dependency.PYTHON)
    if (!ffmpeg) missingDependencies.add(Dependency.FFMPEG)
    if (!aria2c) missingDependencies.add(Dependency.ARIA2C)
    return missingDependencies
}
