package com.yausername.youtubedl_android.mapper

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoFormat (
    val asr: Int = 0,
    val tbr: Int = 0,
    val abr: Int= 0,
    val format: String? = null,
    @SerialName("format_id") val formatId: String? = null,
    @SerialName("format_note") val formatNote: String? = null,
    val ext: String? = null,
    val preference: Int = 0,
    val vcodec: String? = null,
    val acodec: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    @SerialName("filesize") val fileSize: Long = 0,
    @SerialName("filesize_approx") val fileSizeApproximate: Long = 0,
    val fps: Int = 0,
    val url: String? = null,
    @SerialName("manifest_url") val manifestUrl: String? = null,
    @SerialName("http_headers") val httpHeaders: Map<String, String>? = null,
)