package com.yausername.youtubedl_android.domain

import com.yausername.youtubedl_android.Constants

enum class Dependency {
    PYTHON,
    FFMPEG,
    ARIA2C;

    override fun toString(): String {
        return when (this) {
            PYTHON -> "python"
            FFMPEG -> "ffmpeg"
            ARIA2C -> "aria2c"
        }
    }

    fun fromString(string: String): Dependency {
        return when (string) {
            "python" -> PYTHON
            "ffmpeg" -> FFMPEG
            "aria2c" -> ARIA2C
            else -> throw IllegalArgumentException("Unknown plugin: $string")
        }
    }
    companion object {
        fun Dependency.toLibraryName(): String {
            return when (this) {
                PYTHON -> Constants.LibrariesName.PYTHON
                FFMPEG -> Constants.LibrariesName.FFMPEG
                ARIA2C -> Constants.LibrariesName.ARIA2C
            }
        }

        fun Dependency.toDirectoryName(): String {
            return when (this) {
                PYTHON -> Constants.DirectoriesName.PYTHON
                FFMPEG -> Constants.DirectoriesName.FFMPEG
                ARIA2C -> Constants.DirectoriesName.ARIA2C
            }
        }
    }
}