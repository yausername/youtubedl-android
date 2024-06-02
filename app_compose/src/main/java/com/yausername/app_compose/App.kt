package com.yausername.app_compose

import android.app.Application
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        applicationScope = CoroutineScope(SupervisorJob())

        try {
            YoutubeDL.init(this@App)
            FFmpeg.init(this@App)
            Aria2c.init(this@App)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    companion object {
        lateinit var applicationScope: CoroutineScope
    }
}