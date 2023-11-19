package com.yausername.youtubedl_android.domain

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
}