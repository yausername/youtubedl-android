package com.yausername.youtubedl_android_example;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/** Calls only the app's configured backend, which must enforce content entitlement. */
public interface PlaybackApi {
    @GET("api/videos/{videoId}/playback")
    Call<PlaybackResponse> requestPlayback(
            @Header("Authorization") String authorization,
            @Path("videoId") String videoId
    );
}
