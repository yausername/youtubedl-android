package com.yausername.youtubedl_android_example;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/** Calls the app's own backend, which must check identity and video entitlement on every request. */
public interface PlaybackApi {
    @GET("api/videos/{videoId}/playback")
    Call<PlaybackResponse> requestPlayback(@Path("videoId") String videoId);
}
