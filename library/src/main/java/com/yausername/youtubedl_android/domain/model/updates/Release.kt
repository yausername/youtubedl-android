package com.yausername.youtubedl_android.domain.model.updates

import kotlinx.serialization.Serializable

@Serializable
data class Release(
    val url: String = "",
    val assets_url: String = "",
    val upload_url: String = "",
    val html_url: String = "",
    val id: Long = 0,
    val author: Author = Author(),
    val node_id: String = "",
    val tag_name: String = "",
    val target_commitish: String = "",
    val name: String = "",
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val created_at: String = "",
    val published_at: String = "",
    val assets: List<Asset>
)