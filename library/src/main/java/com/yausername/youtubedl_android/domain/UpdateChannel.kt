package com.yausername.youtubedl_android.domain

open class UpdateChannel(val apiUrl: String) {
    object STABLE :
        UpdateChannel("https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest")

    object NIGHTLY :
        UpdateChannel("https://api.github.com/repos/yt-dlp/yt-dlp-nightly-builds/releases/latest")

    object MASTER :
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