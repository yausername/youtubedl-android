package com.yausername.youtubedl_android.mapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoInfo {

    public String id;
    public String fulltitle;
    public String title;
    @JsonProperty("upload_date")
    public String uploadDate;
    @JsonProperty("display_id")
    public String displayId;
    public int duration;
    public String description;
    public String thumbnail;
    public String license;
    
    @JsonProperty("view_count")
    public String viewCount;
    @JsonProperty("like_count")
    public String likeCount;
    @JsonProperty("dislike_count")
    public String dislikeCount;
    @JsonProperty("repost_count")
    public String repostCount;
    @JsonProperty("average_rating")
    public String averageRating;
    

    @JsonProperty("uploader_id")
    public String uploaderId;
    public String uploader;

    @JsonProperty("player_url")
    public String playerUrl;
    @JsonProperty("webpage_url")
    public String webpageUrl;
    @JsonProperty("webpage_url_basename")
    public String webpageUrlBasename;

    public String resolution;
    public int width;
    public int height;
    public String format;
    public String ext;

    @JsonProperty("http_headers")
    public HttpHeader httpHeader;
    public ArrayList<String> categories;
    public ArrayList<String> tags;
    public ArrayList<VideoFormat> formats;
    public ArrayList<VideoThumbnail> thumbnails;
    //public ArrayList<VideoSubtitle> subtitles;
    
    //some useful getters
	public String getViewCount() {
		return viewCount;
	}
	public String getLikeCount() {
		return likeCount;
	}
	public String getDislikeCount() {
		return dislikeCount;
	}
	public String getRepostCount() {
		return repostCount;
	}
	public String getAverageRating() {
		return averageRating;
	}
	public String getId() {
		return id;
	}
	public String getFulltitle() {
		return fulltitle;
	}
	public String getTitle() {
		return title;
	}
	public String getUploadDate() {
		return uploadDate;
	}
	public int getDuration() {
		return duration;
	}
	public String getDescription() {
		return description;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public String getUploaderId() {
		return uploaderId;
	}
	public String getUploader() {
		return uploader;
	}
	@Override
	public String toString() {
		return "VideoInfo [id=" + id + ", fulltitle=" + fulltitle + ", title=" + title + ", uploadDate=" + uploadDate
				+ ", displayId=" + displayId + ", duration=" + duration + ", description=" + description
				+ ", thumbnail=" + thumbnail + ", license=" + license + ", viewCount=" + viewCount + ", likeCount="
				+ likeCount + ", dislikeCount=" + dislikeCount + ", repostCount=" + repostCount + ", averageRating="
				+ averageRating + ", uploaderId=" + uploaderId + ", uploader=" + uploader + ", playerUrl=" + playerUrl
				+ ", webpageUrl=" + webpageUrl + ", webpageUrlBasename=" + webpageUrlBasename + ", resolution="
				+ resolution + ", width=" + width + ", height=" + height + ", format=" + format + ", ext=" + ext
				+ ", httpHeader=" + httpHeader + ", categories=" + categories + ", tags=" + tags + ", formats="
				+ formats + ", thumbnails=" + thumbnails + "]";
	}
	
	
}
