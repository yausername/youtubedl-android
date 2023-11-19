package com.yausername.youtubedl_android.util.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

object Ktor {
    val client = HttpClient(Android.create {
        connectTimeout = 10_000 //ms
        socketTimeout = 10_000 //ms
    }) {

    }
}