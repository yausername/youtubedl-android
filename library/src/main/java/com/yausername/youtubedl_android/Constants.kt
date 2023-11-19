package com.yausername.youtubedl_android

object Constants {
    const val LIBRARY_NAME = "youtubedl-android"
    const val PACKAGES_ROOT_NAME = "packages"

    object Binaries {
        const val PYTHON_BINARY_NAME = "libpython.so"
        const val FFMPEG_BINARY_NAME = "libffmpeg.so"
        const val ARIA2C_BINARY_NAME = "libaria2c.so"
        const val YTDLP_BINARY_NAME = "yt-dlp"
    }

    object Libraries {
        const val PYTHON_LIBRARY_NAME = "libpython.zip.so"
        const val FFMPEG_LIBRARY_NAME = "libffmpeg.zip.so"
        const val ARIA2C_LIBRARY_NAME = "libaria2c.zip.so"

        object Temporal {
            const val TEMPORAL_PYTHON_LIBRARY_NAME = "temp_libpython.zip.so"
            const val TEMPORAL_FFMPEG_LIBRARY_NAME = "temp_libffmpeg.zip.so"
            const val TEMPORAL_ARIA2C_LIBRARY_NAME = "temp_libaria2c.zip.so"
        }
    }

    object Directories {
        const val PYTHON_DIRECTORY_NAME = "python"
        const val FFMPEG_DIRECTORY_NAME = "ffmpeg"
        const val ARIA2C_DIRECTORY_NAME = "aria2c"

        const val YTDLP_DIRECTORY_NAME = "yt-dlp"
    }
}