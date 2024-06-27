package com.yausername.youtubedl_android.domain.model

class YoutubeDLResponse(
    val command: List<String?>,
    val exitCode: Int,
    val elapsedTime: Long,
    val out: String,
    val err: String
)