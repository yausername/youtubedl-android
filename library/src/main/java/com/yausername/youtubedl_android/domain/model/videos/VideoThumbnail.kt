package com.yausername.youtubedl_android.domain.model.videos

import kotlinx.serialization.Serializable

@Serializable
data class VideoThumbnail (
    val url: String? = null,
    val id: String? = null
)