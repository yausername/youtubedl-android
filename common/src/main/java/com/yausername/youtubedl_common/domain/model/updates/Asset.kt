package com.yausername.youtubedl_common.domain.model.updates

import kotlinx.serialization.Serializable

@Serializable
data class Asset(
    val url: String = "",
    val id: Long = 0,
    val node_id: String = "",
    val name: String = "",
    val label: String = "",
    val uploader: Uploader = Uploader(),
    val content_type: String = "",
    val state: String = "",
    val size: Long = 0,
    val download_count: Long = 0,
    val created_at: String = "",
    val updated_at: String = "",
    val browser_download_url: String = ""
)