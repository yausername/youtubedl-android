package com.yausername.youtubedl_common

object Constants {
    const val LIBRARY_NAME = "youtubedl-android"
    const val PACKAGES_ROOT_NAME = "packages"

    object BinariesName {
        const val PYTHON = "libpython.so"
        const val FFMPEG = "libffmpeg.so"
        const val ARIA2C = "libaria2c.so"
        const val YTDLP = "yt-dlp"
    }

    object LibrariesName {
        const val PYTHON = "libpython.zip.so"
        const val FFMPEG = "libffmpeg.zip.so"
        const val ARIA2C = "libaria2c.zip.so"

        object TemporalFilesName {
            const val TEMPORAL_PYTHON = "temp_libpython.zip.so"
            const val TEMPORAL_FFMPEG = "temp_libffmpeg.zip.so"
            const val TEMPORAL_ARIA2C = "temp_libaria2c.zip.so"
        }
    }

    object DirectoriesName {
        const val PYTHON = "python"
        const val FFMPEG = "ffmpeg"
        const val ARIA2C = "aria2c"

        const val YTDLP = "yt-dlp"
    }
}