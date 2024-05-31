package com.yausername.youtubedl_android.domain

sealed class UpdateChannel(val apiUrl: String) {
    data object STABLE :
        UpdateChannel("https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest")

    data object NIGHTLY :
        UpdateChannel("https://api.github.com/repos/yt-dlp/yt-dlp-nightly-builds/releases/latest")

    data object MASTER :
        UpdateChannel("https://api.github.com/repos/yt-dlp/yt-dlp-master-builds/releases/latest")

    companion object {
        @JvmField
        val _STABLE: STABLE = STABLE

        @JvmField
        val _NIGHTLY: NIGHTLY = NIGHTLY

        @JvmField
        val _MASTER: MASTER = MASTER
    }
}