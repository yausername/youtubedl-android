package com.yausername.youtubedl_android.data.remote.plugins

import com.yausername.youtubedl_android.data.remote.files.FileDownloader

class PluginsDownloaderImpl : PluginsDownloader {
    val fileDownloader by lazy { FileDownloader }
    override fun downloadPython() {
        TODO("Not yet implemented")
    }

    override fun downloadFFmpeg() {
        TODO("Not yet implemented")
    }

    override fun downloadAria2c() {
        TODO("Not yet implemented")
    }
}