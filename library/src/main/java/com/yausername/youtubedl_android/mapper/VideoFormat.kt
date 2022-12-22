package com.yausername.youtubedl_android.mapper

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class VideoFormat {
    val asr = 0
    val tbr = 0
    val abr = 0
    val format: String? = null

    @JsonProperty("format_id")
    val formatId: String? = null

    @JsonProperty("format_note")
    val formatNote: String? = null
    val ext: String? = null
    val preference = 0
    val vcodec: String? = null
    val acodec: String? = null
    val width = 0
    val height = 0

    @JsonProperty("filesize")
    val fileSize: Long = 0

    @JsonProperty("filesize_approx")
    val fileSizeApproximate: Long = 0
    val fps = 0
    val url: String? = null

    @JsonProperty("manifest_url")
    val manifestUrl: String? = null

    @JsonProperty("http_headers")
    val httpHeaders: Map<String, String>? = null
}