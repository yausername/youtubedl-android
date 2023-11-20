package com.yausername.youtubedl_android.domain

import com.yausername.youtubedl_android.Constants

enum class Plugin {
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

    fun fromString(string: String): Plugin {
        return when (string) {
            "python" -> PYTHON
            "ffmpeg" -> FFMPEG
            "aria2c" -> ARIA2C
            else -> throw IllegalArgumentException("Unknown plugin: $string")
        }
    }
    companion object {
        fun Plugin.toLibraryName(): String {
            return when (this) {
                PYTHON -> Constants.Libraries.PYTHON_LIBRARY_NAME
                FFMPEG -> Constants.Libraries.FFMPEG_LIBRARY_NAME
                ARIA2C -> Constants.Libraries.ARIA2C_LIBRARY_NAME
            }
        }

        fun Plugin.toDirectoryName(): String {
            return when (this) {
                PYTHON -> Constants.Directories.PYTHON_DIRECTORY_NAME
                FFMPEG -> Constants.Directories.FFMPEG_DIRECTORY_NAME
                ARIA2C -> Constants.Directories.ARIA2C_DIRECTORY_NAME
            }
        }
    }
}