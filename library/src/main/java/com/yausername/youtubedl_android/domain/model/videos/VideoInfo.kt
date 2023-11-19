package com.yausername.youtubedl_android.domain.model.videos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoInfo(
    val id: String? = null,
    @SerialName("fulltitle") val fullTitle: String? = null,
    val title: String? = null,
    @SerialName("upload_date") val uploadDate: String? = null,
    @SerialName("display_id") val displayId: String? = null,
    val duration: Int = 0,
    val description: String? = null,
    val thumbnail: String? = null,
    val license: String? = null,
    val extractor: String? = null,
    @SerialName("extractor_key") val extractorKey: String? = null,
    @SerialName("view_count") val viewCount: String? = null,
    @SerialName("like_count") val likeCount: String? = null,
    @SerialName("dislike_count") val dislikeCount: String? = null,
    @SerialName("repost_count") val repostCount: String? = null,
    @SerialName("average_rating") val averageRating: String? = null,
    @SerialName("uploader_id") val uploaderId: String? = null,
    val uploader: String? = null,
    @SerialName("player_url") val playerUrl: String? = null,
    @SerialName("webpage_url") val webpageUrl: String? = null,
    @SerialName("webpage_url_basename") val webpageUrlBasename: String? = null,
    val resolution: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val format: String? = null,
    @SerialName("format_id") val formatId: String? = null,
    val ext: String? = null,
    @SerialName("filesize") val fileSize: Long = 0,
    @SerialName("filesize_approx") val fileSizeApproximate: Long = 0,
    @SerialName("http_headers") val httpHeaders: Map<String, String>? = null,
    val categories: ArrayList<String>? = null,
    val tags: ArrayList<String>? = null,
    @SerialName("requested_formats") val requestedFormats: ArrayList<VideoFormat>? = null,
    val formats: ArrayList<VideoFormat>? = null,
    val thumbnails: ArrayList<VideoThumbnail>? = null,
    @SerialName("manifest_url") val manifestUrl: String? = null,
    val url: String? = null
)
